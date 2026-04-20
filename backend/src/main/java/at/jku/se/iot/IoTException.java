package at.jku.se.iot;

/** Checked exception raised for errors from IoT protocol adapters. */
public class IoTException extends Exception {

    /**
     * Creates an exception with a message.
     *
     * @param message the detail message
     */
    public IoTException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and root cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public IoTException(String message, Throwable cause) {
        super(message, cause);
    }
}
