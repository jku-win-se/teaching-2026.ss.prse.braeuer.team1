package at.jku.se.iot;

public class IoTException extends Exception {

    public IoTException(String message) {
        super(message);
    }

    public IoTException(String message, Throwable cause) {
        super(message, cause);
    }
}
