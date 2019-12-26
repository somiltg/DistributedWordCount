package taskallocation;

/**
 * Exception thrown when there is no task available to allocate. This should be a checked exception.
 *
 * @author somilgupta
 */
public class NoTaskAvailableException extends Exception {
    public NoTaskAvailableException() {
        super();
    }
}
