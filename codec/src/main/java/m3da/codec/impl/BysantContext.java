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
package m3da.codec.impl;

import m3da.codec.impl.encoding.Encoding;
import m3da.codec.impl.encoding.GlobalCtxEncoding;
import m3da.codec.impl.encoding.ListMapCtxEncoding;
import m3da.codec.impl.encoding.NumbersCtxEncoding;
import m3da.codec.impl.encoding.UintStrCtxEncoding;

/**
 * The Bysant encoding context. During the bysant stream the encoding can change so the OPECODE rules changes
 */
public enum BysantContext {
	GLOBAL(new GlobalCtxEncoding(), 0), UINTS_AND_STRS(new UintStrCtxEncoding(), 1), NUMBERS(new NumbersCtxEncoding(), 2), SIGNED_INTS32(null, 3), FLOATS32(
			null, 4), DOUBLES64(null, 5), LIST_AND_MAPS(new ListMapCtxEncoding(), 6);

	private final Encoding encoding;

	// context identifier in bysant lingua
	private final int id;

	/**
	 * @param encoding
	 */
	private BysantContext(Encoding encoding, int id) {
		this.encoding = encoding;
		this.id = id;
	}

	public Encoding getEncoding() {
		return encoding;
	}

	public static BysantContext findById(int id) {
		for (BysantContext ctx : BysantContext.values()) {
			if (ctx.id == id) {
				return ctx;
			}
		}
		return null;
	}

}
