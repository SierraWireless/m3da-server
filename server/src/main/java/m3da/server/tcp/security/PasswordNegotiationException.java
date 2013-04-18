package m3da.server.tcp.security;

/**
 * Exception throw during password negociation.
 * 
 */
@SuppressWarnings("serial")
public class PasswordNegotiationException extends RuntimeException {

    public PasswordNegotiationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PasswordNegotiationException(String message) {
        super(message);
    }

    public PasswordNegotiationException(Throwable cause) {
        super(cause);
    }

}
