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

import m3da.codec.DecoderException;
import m3da.codec.dto.M3daEnvelope;

/**
 * 
 * M3DA client for sending and receiving {@link M3daEnvelope} from a server.
 * 
 */
public interface M3daClient {

    public void connect() throws IOException;

    public M3daEnvelope sendEnvelope(M3daEnvelope envelope) throws IOException, DecoderException;

    public void close() throws IOException;
}
