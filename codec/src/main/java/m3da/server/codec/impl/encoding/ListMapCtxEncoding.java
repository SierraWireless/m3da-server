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
 * The LIST_AND_MAPS context specialized in encoding list and maps of various size
 */
public class ListMapCtxEncoding implements MapEncoding, ListEncoding, Encoding {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int emptyMapOpCode() {
		return 0x83;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int tinyMapLimit() {
		return 60;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int tinyUntypedMapOpCode() {
		return 0x84;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int largeUntypedMapOpCode() {
		return 0xC0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int nullTerminatedUntypedMapOpCode() {
		return 0xC1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int tinyTypedMapOpCode() {
		return 0xC2;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int largeTypedMapOpCode() {
		return 0xFE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int nullTerminatedTypedMapOpCode() {
		return 0xFF;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int emptyListOpCode() {
		return 0x01;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int tinyListLimit() {
		return 60;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int tinyUntypedListOpCode() {
		return 0x02;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int largeUntypedListOpCode() {
		return 0x3E;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int nullTerminatedUntypedListOpCode() {
		return 0x3F;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int tinyTypedListOpCode() {
		return 0x40;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int largeTypedListOpCode() {
		return 0x7C;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int nullTerminatedTypedListOpCode() {
		return 0x7D;
	}

}
