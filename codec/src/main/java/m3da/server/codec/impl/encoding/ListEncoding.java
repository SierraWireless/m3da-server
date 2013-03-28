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
package m3da.server.codec.impl.encoding;

/**
 * a Bysant context which can encode List
 */
public interface ListEncoding {
	int emptyListOpCode();

	int tinyListLimit();

	int tinyUntypedListOpCode();

	int largeUntypedListOpCode();

	int nullTerminatedUntypedListOpCode();

	int tinyTypedListOpCode();

	int largeTypedListOpCode();

	int nullTerminatedTypedListOpCode();

}
