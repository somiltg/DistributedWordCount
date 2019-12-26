package taskallocation;

import java.util.Observer;

/**
 * Interface for the component that manages task allocation and availability. The implementation must maintain a
 * mapping of {@link Task} allocated to each worker and a record of available tasks which is constantly  updated
 * when the task is allocated or de-allocated. Tasks should be given in FIFO manner to prevent starvation. The
 * implementation must listen to heartbeat manager notifications and deallocate the task if assigned to the failed
 * worker.
 * Should allocate one task to the worker at a time. Does not work for scenarios where multiple tasks are being
 * handled by single worker.
 * <p>
 * Note: All the functions and data-structures should be thread safe as there would be multiple allocation request
 * waiting for a task to be available or allocated.
 *
 * @author somilgupta
 */
public interface ITaskManager extends Observer {
    /*
     * Turn this to true to see task manager logs.
     */
    boolean DEBUG = true;
    /**
     * Port at which the master would listen to task allocation requests from the workers.
     */
    int TASK_ALLOCATION_SERVER_PORT = 11999;

    /**
     * Allocate the task if any available. If a task is already allocated to the worker, then return back the same
     * task. This function should ideally be called after querying availability at {{@link #isAnyTaskAvailable()}}.
     *
     * @param workerId Identifier of the worker.
     * @return {@link Task} denoting the allocated task to be done.
     * @throws NoTaskAvailableException Race condition may cause the task to get allocated before request. In that
     *                                  case no task would be available and this exception would be thrown. Upstream
     *                                  should catch this exception and take action.
     */
    Task allocateTask(final int workerId) throws NoTaskAvailableException;

    /**
     * Deallocate the task assigned to the worker. This may happen when the worker dies unexpectedly and task needs
     * to be reallocated. Should do nothing if already de-allocated.
     *
     * @param workerId worker identifier.
     */
    void deallocateTask(final int workerId);

    /**
     * Checks if any task is available for allocation. Please note that even though this shows true at the moment of
     * call, the task may get allocated when allocation request made. Caller must treat both calls as atomic to
     * prevent race conditions.
     *
     * @return True if available else false.
     */
    boolean isAnyTaskAvailable();

    /**
     * Notifies true when there is no task left to allocate and no ongoing active task.
     *
     * @return true when all tasks are notified by the workers to be completed.
     */
    boolean areAllTasksCompleted();

    /**
     * Marks the task to be completed. This should ensure that the task does not appear in the available/ongoing task
     * pool.
     *
     * @param workerId workerId that completed the task. The task allocated to this worker is completed.
     */
    void markTaskCompleted(final int workerId);

    /**
     * Specific log message for task manager component.
     *
     * @param message Specific message.
     */
    static void log(final String message) {
        if (DEBUG)
            System.out.println(String.format("[Task Allocation][Time: %d] %s", System.nanoTime(), message));
    }
}
