package taskallocation;

/**
 * Interface for the execution to be done by the worker once task is allocated to it by the master.
 */
public interface ITaskExecutor {
    boolean DEBUG = true;

    /**
     * Implementations must add custom execution logic for the obtained task. The implementation must input from the
     * file given in {@link Task} and must output to the file given in the same task.
     *
     * @param task {@link Task} to be accomplished.
     */
    void execute(final Task task);

    /**
     * Specific log message for task executor component.
     *
     * @param message     Specific message.
     * @param application Name of the application executing the executor.
     */
    static void log(final String message, final String application) {
        if (DEBUG)
            System.out.println(String.format("[Task Executor] [%s] [Time: %d] %s", application, System.nanoTime(), message));
    }
}
