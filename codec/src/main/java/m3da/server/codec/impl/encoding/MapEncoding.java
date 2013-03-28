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
 * a Bysant context which can encode Map
 */
public interface MapEncoding {

	int emptyMapOpCode();

	int tinyMapLimit();

	int tinyUntypedMapOpCode();

	int largeUntypedMapOpCode();

	int nullTerminatedUntypedMapOpCode();

	int tinyTypedMapOpCode();

	int largeTypedMapOpCode();

	int nullTerminatedTypedMapOpCode();

}
