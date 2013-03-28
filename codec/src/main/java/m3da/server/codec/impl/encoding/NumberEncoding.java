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
 * A way to encode a SIGNED number, depending of the context
 */
public interface NumberEncoding {

	int tinyMin();

	int tinyMax();

	int tinyOpCode();

	int smallMin();

	int smallMax();

	int smallNegativeOpCode();

	int smallPositiveOpCode();

	int mediumMin();

	int mediumMax();

	int mediumNegativeOpCode();

	int mediumPositiveOpCode();

	int largeMin();

	int largeMax();

	int largeNegativeOpCode();

	int largePositiveOpCode();

	int int32OpCode();

	int int64OpCode();

	int float32opCode();

	int float64opCode();
}
