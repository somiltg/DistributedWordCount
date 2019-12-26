package resource;

import java.util.Map;
import java.util.Observer;

/**
 * Interface for component managing worker processes (resource). Each worker is a separate process spawned from the
 * master process.
 * Master must maintain the state of these processes and de-register if a process dies. It also kills them when their
 * duty is done.
 * A WorkerId uniquely identifies each worker and it is the same as the heartbeat server port.
 * Resource manager is bounded and must throw an exception is capacity is exceeded. Uses Observer pattern to
 * subscribe to heartbeat failure notifications.
 * <p>
 * The caller should ensure that the ports in the {{@link #WORKER_ID_PREFIX}} range are free to be used by the resource
 * manager. Manager does not hold this duty.
 *
 * @author somilgupta
 */
public interface IResourceManager extends Observer {
    /*
     * Turn this to true to see resource manager logs.
     */
    boolean DEBUG = true;
    /**
     * All the workers should be allocated IDs in the range prefixed by the given integer.
     */
    int WORKER_ID_PREFIX = 1200; //Worker Id would be same as the heartbeat server id of worker.

    /**
     * Create worker process and register it. Master must notify the worker of its workerId which it would use in future
     * communications with the task allocation server.
     */
    void createWorker();

    /**
     * Stop the worker process with the given worker Id. This should also close the heartbeat client for that
     * worker before killing the process to prevent redundant failure notifications.
     *
     * @param workerId Id of the worker to be stopped.
     */
    void stopWorker(int workerId);

    /**
     * Stop all the workers currently active. This function should implicity call {{@link #stopWorker(int)}} to kill
     * the workers. Called by task allocator when all the tasks are completed to kill the workers waiting for tasks.
     */
    void stopAllWorkers();

    /**
     * Obtain all the workers active at this moment. This list may be transitory and additional check must be
     * employed before using this as a process may have died after receiving this list.
     *
     * @return Map of workerIds to {@link Process} for active workers.
     */
    Map<Integer, Process> getActiveWorkers();

    /**
     * Specific log message for resource manager component.
     *
     * @param message Specific message.
     */
    static void log(final String message) {
        if (DEBUG)
            System.out.println(String.format("[Resource][Time: %d] %s", System.nanoTime(), message));
    }
}
