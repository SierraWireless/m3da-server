package m3da.server.tcp.security;

import m3da.codec.dto.M3daEnvelope;

/**
 * The result of an envelope processing by the {@link SecurityHandler}.
 */
public class AuthenticationResult {

    private final boolean success;
    private final M3daEnvelope envelope;
    private final Boolean endSession;

    private AuthenticationResult(boolean success, M3daEnvelope envelope, Boolean endSession) {
        this.success = success;
        this.envelope = envelope;
        this.endSession = endSession;
    }

    /**
     * Generate a result for an envelope that was successfully authenticated.
     * <p>
     * The resulting envelope can be :
     * <ul>
     * <li>the original one if the targeted system does not need to authenticate</li>
     * <li>the payload of the initial envelope. The surrounding envelope was decoded and the message was previously
     * authenticated. The protected inner envelope was deciphered if needed and passed as parameter of this callback.</li>
     * </ul>
     * 
     * @param envelope the authenticated envelope
     */
    public static AuthenticationResult success(M3daEnvelope envelope) {
        return new AuthenticationResult(true, envelope, null);
    }

    /**
     * Generate a result for an incoming request failed to be authenticated.
     * <p>
     * The envelope to send can be :
     * <ul>
     * <li>a challenge requested by the server</li>
     * <li>an error message (forbidden if the retry count is exceeded, errors from the communication service)</li>
     * <li>the last server response to send again because of a challenge requested by the device</li>
     * </ul>
     * 
     * @param envelope the response to be sent to the remote system
     * @param endSession <code>true</code> if the session must be aborted, <code>false</code> if a response is expected
     *        from the device.
     */
    public static AuthenticationResult failure(M3daEnvelope envelope, boolean endSession) {
        return new AuthenticationResult(false, envelope, endSession);
    }

    /**
     * @return <code>true</code> if the envelope was successfully authenticated and <code>false</code> otherwise.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @return The resulting envelope. Can be an error to send back to the device or a protected envelope to be
     *         processed.
     */
    public M3daEnvelope getEnvelope() {
        return envelope;
    }

    /**
     * @return <code>true</code> if the session must be aborted, <code>false</code> if a response is expected from the
     *         device. Only meaningful if the authentication failed.
     */
    public Boolean getEndSession() {
        return endSession;
    }

}
