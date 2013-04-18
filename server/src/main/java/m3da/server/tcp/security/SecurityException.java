package m3da.server.tcp.security;

/**
 * Critical security exception causing the current M3DA session to be aborted.
 */
@SuppressWarnings("serial")
public class SecurityException extends RuntimeException {

    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    public SecurityException(String message) {
        super(message);
    }

    public SecurityException(Throwable cause) {
        super(cause);
    }

}
