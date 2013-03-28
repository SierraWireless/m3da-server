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
package m3da.server.codec;

import java.nio.ByteBuffer;

import m3da.server.codec.dto.M3daPdu;

/**
 * A state-less bysant encoder, encode the given object in the bysant format.
 */
public interface BysantEncoder {

	/**
	 * Encodes a bunch of message as a ByteBuffer in the bysant format.
	 * 
	 * @param toEncode
	 *            the list of AwtDa3 pdu to be encoded
	 */
	ByteBuffer encode(M3daPdu... toEncode);

}
