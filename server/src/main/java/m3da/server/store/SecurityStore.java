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

    void storeNonce(String clientId, String newNonce);

    void storeNewPassword(String clientId, String password);
}
