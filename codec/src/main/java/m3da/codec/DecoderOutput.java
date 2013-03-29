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
package m3da.codec;

/**
 * Output sink for decoded messages. Will receive DTO of type T resulting of the dbyte buffer decoding.
 */
public interface DecoderOutput<T> {
	/**
	 * A DTO was decoded in the byte buffer stream
	 * 
	 * @param pdu
	 *            the decoded DTO
	 */
	void decoded(T pdu);
}
