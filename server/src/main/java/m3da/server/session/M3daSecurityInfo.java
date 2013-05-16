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

import java.io.Serializable;

public class M3daSecurityInfo implements Serializable {

    /**
     * for serialization
     */
    private static final long serialVersionUID = 1L;

    /** M3DA : communication identifier */
    private String m3daCommId;

    /** M3DA : security type */
    private M3daAuthentication m3daSecurityType = M3daAuthentication.NONE;

    /** M3DA : the server nonce for salting the hashes */
    private String m3daNonce;

    /** M3DA : the server cipher to be used for communication */
    private M3daCipher m3daCipher = M3daCipher.NONE;

    /** M3DA : Pre-shared key */
    private String m3daSharedKey;

    /** M3DA : Credential */
    private String m3daCredential;

    public void setM3daCommId(String m3daCommId) {
        this.m3daCommId = m3daCommId;
    }

    public void setM3daSecurityType(M3daAuthentication m3daSecurityType) {
        this.m3daSecurityType = m3daSecurityType;
    }

    public void setM3daNonce(String m3daNonce) {
        this.m3daNonce = m3daNonce;
    }

    public void setM3daCipher(M3daCipher m3daCipher) {
        this.m3daCipher = m3daCipher;
    }

    public void setM3daSharedKey(String m3daSharedKey) {
        this.m3daSharedKey = m3daSharedKey;
    }

    public void setM3daCredential(String m3daCredential) {
        this.m3daCredential = m3daCredential;
    }

    public String getM3daCommId() {
        return m3daCommId;
    }

    public M3daAuthentication getM3daSecurityType() {
        return m3daSecurityType;
    }

    public String getM3daNonce() {
        return m3daNonce;
    }

    public M3daCipher getM3daCipher() {
        return m3daCipher;
    }

    public String getM3daSharedKey() {
        return m3daSharedKey;
    }

    public String getM3daCredential() {
        return m3daCredential;
    }

}
