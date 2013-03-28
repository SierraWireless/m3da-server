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
 * A way to encode a M3DA specialized objects, depending of the context
 */
public interface M3daEncoding {

	int getEnvelopeOpCode();

	int getMessageOpCode();

	int getResponseOpCode();

	int getDeltaVectorOpCode();

	int getQuasiPeriodicVectorOpCode();
}
