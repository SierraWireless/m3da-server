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
 * Checked exception thrown when decoding incorrect data
 */
@SuppressWarnings("serial")
public class DecoderException extends Exception {

	public DecoderException(String message, Throwable cause) {
		super(message, cause);
	}

	public DecoderException(String message) {
		super(message);
	}

}
