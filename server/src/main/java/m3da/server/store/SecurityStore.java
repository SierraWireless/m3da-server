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
package m3da.server.store;

import m3da.server.session.M3daSecurityInfo;

/**
 * Store for all the security releated informations.
 */
public interface SecurityStore {

    /**
     * Get the security related informations.
     * 
     * @param clientId
     * @return
     */
    M3daSecurityInfo getSecurityInfo(String clientId);

    /**
     * store the new nonce value after some communication
     * 
     * @param clientId the client unique identifier
     * @param newNonce the new nonce string to use for the next communication
     */
    void storeNonce(String clientId, String newNonce);

    /**
     * Store the password for a given client after the password negotiation.
     * 
     * @param clientId the client unique identifier
     * @param password the password string
     */
    void storeNewPassword(String clientId, String password);

    /**
     * add new security information for a client. It'll discard already associated security informations.
     * 
     * @param securityInfo the new security information
     */
    void addSecurityInfo(M3daSecurityInfo securityInfo);
}
