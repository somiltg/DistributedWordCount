package resource;

import static resource.IResourceManager.log;
import static util.ApplicationUtils.processId;

import heartbeat.IHeartBeatManager;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.IntStream;

/**
 * Implementation of {@link IResourceManager}. Use Observer design pattern obtain notifications of the failures.
 * De-registers worker process on receiving the failure notification. Follows a bounded resource approach and
 * allocates workerIds from a fixed pool. Uses {@link ProcessBuilder} for building the worker.
 *
 * @author somilgupta
 */
public class ResourceManager implements IResourceManager {

    private final Map<Integer, Process> activeWorkerPool;
    private final Queue<Integer> vacantIds;
    private final IHeartBeatManager heartBeatManager;
    private final int capacity;
    private final String workerAppClasspath;
    private final String workerAppName;

    /**
     * Manages the resources (workers) for the master in a bounded fashion. Uses processes to represent workers.
     *
     * @param capacity              Maximum number of workers. Throws {@link CapacityFullException} if the capacity is exceeded.
     * @param workerApplicationName Name of the worker application in the given classPath. Throws
     *                              {@link WorkerInstantiationException} if the process build fails.
     * @param classpath             Folder where the application would be found. Please note that this should be the location of
     *                              class file and not the source file.
     * @param heartBeatManager      Implementation of {@link IHeartBeatManager} which would be called for maintaining
     *                              heartbeat for new or killed workers.
     */
    public ResourceManager(final int capacity, final String classpath, final String workerApplicationName,
                           final IHeartBeatManager heartBeatManager) {
        assert heartBeatManager != null;
        assert capacity >= 0;
        activeWorkerPool = new ConcurrentHashMap<>();
        vacantIds = new ConcurrentLinkedQueue<>();
        IntStream.range(0, capacity).forEach(vacantIds::add);
        this.heartBeatManager = heartBeatManager;
        this.capacity = capacity;
        workerAppClasspath = classpath;
        workerAppName = workerApplicationName;
    }

    @Override
    public synchronized void createWorker() {
        if (vacantIds.isEmpty()) {
            throw new CapacityFullException("No capacity left for worker to create.");
        }
        int vacantIdx = vacantIds.poll();
        int workerId = processId(vacantIdx);
        log("Creating worker process with worker Id " + workerId);
        activeWorkerPool.put(vacantIdx, buildWorkerProcess(workerId));
        log("Created worker process " + activeWorkerPool.get(vacantIdx) + " with worker Id " + workerId);
        heartBeatManager.addNewHeartBeat(workerId);
    }

    @Override
    public synchronized void stopWorker(int workerId) {
        int idx = workerId - WORKER_ID_PREFIX * 10;
        assert idx >= 0 && idx < capacity;
        if (!activeWorkerPool.containsKey(idx)) {
            log("Warn: Worker Id " + workerId + " is already stopped. ");
            return;
        }
        log("Stopping worker process with worker Id " + workerId);

        heartBeatManager.stopHeartBeat(workerId);
        try {
            // To ensure heartbeat thread is stopped before worker process is stopped.
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            log("Thread could not be slept. Reason: " + e.getMessage());
        }
        try {
            log("Destroying worker process " + activeWorkerPool.get(idx) + " with worker Id " + workerId);
            activeWorkerPool.get(idx).destroyForcibly().waitFor();
            log("Destroyed worker process with worker Id " + workerId);
        } catch (InterruptedException e) {
            //This would not happen.
            e.printStackTrace();
        }
        log("Worker process with worker Id " + workerId + " destroyed with exit value:" + activeWorkerPool.get(idx).exitValue());
        activeWorkerPool.remove(idx);
        vacantIds.add(idx);
    }

    @Override
    public void stopAllWorkers() {
        activeWorkerPool.keySet().forEach(id -> stopWorker(processId(id)));
    }

    @Override
    public Map<Integer, Process> getActiveWorkers() {
        return new HashMap<>(activeWorkerPool);
    }

    @Override
    public void update(Observable o, Object arg) {
        int workerId = (int) arg;
        log("Received notification from heartbeat manager for workerId: " + workerId);
        stopWorker(workerId);
        createWorker();
    }

    private Process buildWorkerProcess(final int workerId) {
        try {
            return new ProcessBuilder("java", "-cp", workerAppClasspath, workerAppName, Integer.toString(workerId))
                    .inheritIO().start();
        } catch (IOException e) {
            log("Error in building worker process " + workerId + ". Error Message: " + e.getMessage());
            e.printStackTrace();
            throw new WorkerInstantiationException("Error in building worker process " + workerId + ". Error Message: " + e.getMessage());
        }
    }
}
