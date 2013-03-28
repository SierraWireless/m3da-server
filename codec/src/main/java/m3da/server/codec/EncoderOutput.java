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

/**
 * Output sink for encoded messages. Will receive byte buffer resulting of DTO encoding
 */
public interface EncoderOutput {

	/**
	 * write the received buffer to the next stage.
	 * 
	 * @param encodedBuffer
	 *            the buffer resulting of the DTO encoding
	 */
	void write(ByteBuffer encodedBuffer);
}
