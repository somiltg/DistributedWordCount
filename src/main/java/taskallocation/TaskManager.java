package taskallocation;

import java.util.Arrays;
import java.util.Map;
import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static util.ApplicationUtils.getOutputFile;

/**
 * Implementation of {@link ITaskManager}. Use Observer design pattern obtain notifications of the failures.
 * De-allocates task assigned to the worker process on receiving the failure notification. Creates {@link Task} for
 * each allocation. All the implementations and data structures are thread-safe. Does not handle task requests
 * directly, for request management, check {@link TaskAllocationServer}.
 *
 * @author somilgupta
 */
public class TaskManager implements ITaskManager {
    private final Map<Integer, String> ongoingTaskAllocationMap;
    private final String outputLocation;
    private final String outputExtension;
    private final Queue<String> incompleteTasks;

    public TaskManager(final String[] inputFiles, final String outputLocation, String outputFileExtension) {
        this.outputLocation = outputLocation;
        this.outputExtension = outputFileExtension;
        this.incompleteTasks = new ConcurrentLinkedQueue<>();
        this.ongoingTaskAllocationMap = new ConcurrentHashMap<>();
        incompleteTasks.addAll(Arrays.asList(inputFiles));
    }

    @Override
    public synchronized Task allocateTask(final int workerId) throws NoTaskAvailableException {
        String inputFile;
        if (ongoingTaskAllocationMap.containsKey(workerId)) {
            log("There is already an active task allocated to workerId " + workerId + ". Returning the same task.");
            inputFile = ongoingTaskAllocationMap.get(workerId);
            return modelTaskFor(inputFile);
        }
        if (!isAnyTaskAvailable()) {
            log("[WARN] isAnyTaskAvailable should be called before allocateTask.");
            throw new NoTaskAvailableException();
        }
        inputFile = incompleteTasks.poll();
        log("Assigning input file " + inputFile + " to worker " + workerId);
        ongoingTaskAllocationMap.put(workerId, inputFile);
        return modelTaskFor(inputFile);
    }

    @Override
    public synchronized void deallocateTask(final int workerId) {
        if (!ongoingTaskAllocationMap.containsKey(workerId)) {
            log("[WARN] No task allocated for workerId: " + workerId);
            return;
        }
        String inputFile = ongoingTaskAllocationMap.get(workerId);
        log("Deallocating worker " + workerId + " from input file: " + inputFile);
        ongoingTaskAllocationMap.remove(workerId);
        incompleteTasks.add(inputFile);
    }

    @Override
    public boolean isAnyTaskAvailable() {
        return !incompleteTasks.isEmpty();
    }

    @Override
    public boolean areAllTasksCompleted() {
        return incompleteTasks.isEmpty() && ongoingTaskAllocationMap.isEmpty();
    }

    @Override
    public synchronized void markTaskCompleted(final int workerId) {
        if (!ongoingTaskAllocationMap.containsKey(workerId)) {
            log("[WARN] No task allocated for workerId: " + workerId);
            return;
        }
        String inputFile = ongoingTaskAllocationMap.get(workerId);
        log("Marking input file: " + inputFile + " as completed. Worker " + workerId + " completed it.");
        ongoingTaskAllocationMap.remove(workerId);
    }

    @Override
    public void update(Observable o, Object arg) {
        int workerId = (int) arg;
        log("Received notification from heartbeat manager for workerId: " + workerId);
        deallocateTask(workerId);
    }

    private Task modelTaskFor(final String inputFile) {
        return new Task(inputFile, getOutputFile(inputFile, outputLocation, outputExtension));
    }

    private static void log(final String message) {
        ITaskManager.log("[Manager]" + message);
    }
}