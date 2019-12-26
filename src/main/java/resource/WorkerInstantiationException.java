package resource;

/**
 * Exception thrown when there is failure in creating worker process.
 *
 * @author somilgupta
 */
public class WorkerInstantiationException extends RuntimeException {
    public WorkerInstantiationException(String message) {
        super(message);
    }
}
