package m3da.server.tcp.security;

/**
 * State , for a given session of the password negotiation algorithm.
 */
public enum PasswordNegoState {
    NONE, // no negotiation stated
    WAIT_PUB_KEY, // our salt was sent, we wait for the remote system public ECCDH key
    WAIT_ACK, // we sent the new password hash, we wait for remote system acknowledge
    DONE; // done successfully !
}
