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

/**
 * The repository for M3DA HTTP sessions.
 * <p>
 * Note that the communication ID of the system is used to identify the session.
 */
public interface M3daSessionRepository {

    /**
     * Get a session (or create it) for a given system.
     * 
     * @param communicationId the system unique OMA-DM communication identifier
     */
    M3daSession getOrCreateSession(String communicationId);

    /**
     * Store a modified session for future usage.
     * 
     * @param session the session to be stored
     */
    void storeSession(M3daSession session);

    /**
     * Destroy a previously created session
     * 
     * @param communicationId the system communication identifier
     */
    void destroySession(String communicationId);

}
