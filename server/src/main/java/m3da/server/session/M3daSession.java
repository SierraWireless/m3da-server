/*******************************************************************************
 * Copyright (c) 2013 Sierra Wireless.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 ******************************************************************************/
package m3da.server.session;

import java.util.Arrays;

import m3da.codec.dto.M3daEnvelope;
import m3da.server.tcp.security.PasswordNegoState;

/**
 * A M3DA session
 * <p>
 * The session lifecycle is managed using the {@link M3daSessionRepository} for HTTP.
 */
public class M3daSession {

    private String communicationId;

    /** Return the security informations stored in this session. */
    private M3daSecurityInfo communicationInfo;

    /** The last envelope sent by the server (to be used in case of security challenge */
    private M3daEnvelope lastServerResponse;

    /** Current state for the password negotiation algorithm */
    private PasswordNegoState passNegoState = PasswordNegoState.NONE;

    /** Return the security informations stored in this session. */
    private int clientAuthenticationAttemptCount = 0;

    private byte[] passNegoClientSalt;

    private byte[] passNegoServerSalt;

    private String newPassword;

    public String getCommunicationId() {
        return communicationId;
    }

    public void setCommunicationId(String communicationId) {
        this.communicationId = communicationId;
    }

    public int getClientAuthenticationAttemptCount() {
        return clientAuthenticationAttemptCount;
    }

    public void setClientAuthenticationAttemptCount(int clientAuthenticationAttemptCount) {
        this.clientAuthenticationAttemptCount = clientAuthenticationAttemptCount;
    }

    public M3daEnvelope getLastServerResponse() {
        return lastServerResponse;
    }

    public void setLastServerResponse(M3daEnvelope lastServerResponse) {
        this.lastServerResponse = lastServerResponse;
    }

    public M3daSecurityInfo getCommunicationInfo() {
        return communicationInfo;
    }

    public void setCommunicationInfo(M3daSecurityInfo communicationInfo) {
        this.communicationInfo = communicationInfo;
    }

    public PasswordNegoState getPassNegoState() {
        return passNegoState;
    }

    public void setPassNegoState(PasswordNegoState passNegoState) {
        this.passNegoState = passNegoState;
    }

    public byte[] getPassNegoClientSalt() {
        return passNegoClientSalt;
    }

    public void setPassNegoClientSalt(byte[] passNegoClientSalt) {
        this.passNegoClientSalt = passNegoClientSalt;
    }

    public byte[] getPassNegoServerSalt() {
        return passNegoServerSalt;
    }

    public void setPassNegoServerSalt(byte[] passNegoServerSalt) {
        this.passNegoServerSalt = passNegoServerSalt;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "M3daSession [communicationId=" + communicationId + ", communicationInfo=" + communicationInfo
                + ", lastServerResponse=" + lastServerResponse + ", passNegoState=" + passNegoState
                + ", clientAuthenticationAttemptCount=" + clientAuthenticationAttemptCount + ", passNegoClientSalt="
                + Arrays.toString(passNegoClientSalt) + ", passNegoServerSalt=" + Arrays.toString(passNegoServerSalt)
                + ", newPassword=" + newPassword + "]";
    }
}
