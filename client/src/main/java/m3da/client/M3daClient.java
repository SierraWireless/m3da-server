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
package m3da.client;

import java.io.IOException;

import m3da.codec.dto.M3daBodyMessage;
import m3da.codec.dto.M3daEnvelope;

/**
 * 
 * M3DA client for sending and receiving {@link M3daEnvelope} from a server.
 * 
 * Multiple sub-implementations for different kind of transport (e.g : TCP, HTTP)
 */
public interface M3daClient {

    /**
     * Connect to the remote server.
     * 
     * @throws IOException in case on network failure
     */
    public void connect() throws IOException;

    /**
     * Send a list of message in an M3DA envelope to the connected server.
     * 
     * @param messages the list of message to send
     * @return received messages (if any) from the server
     * @throws IOException in case of network failure
     * @throws M3daServerException when the server return a status code different of 200
     */
    public M3daBodyMessage[] sendEnvelope(M3daBodyMessage[] messages) throws IOException, M3daServerException;

    /**
     * Close the connection with the server.
     * 
     * @throws IOException in case of network error.
     */
    public void close() throws IOException;
}
