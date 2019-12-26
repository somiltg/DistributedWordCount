package resource;

/**
 * Exception thrown when more workers are created than the assigned capacity to the resource manager.
 *
 * @author somilgupta
 */
public class CapacityFullException extends RuntimeException {
    public CapacityFullException(String message) {
        super(message);
    }
}
