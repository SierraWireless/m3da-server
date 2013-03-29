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
package m3da.codec.impl.encoding;

public class GlobalCtxEncoding implements MapEncoding, ListEncoding, Encoding, NumberEncoding, StringEncoding, M3daEncoding, BooleanEncoding {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getFalseOpCode() {
		return 0x02;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getTrueOpCode() {
		return 0x01;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int emptyMapOpCode() {
		return 0x41;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int tinyMapLimit() {
		return 9;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int tinyUntypedMapOpCode() {
		return 0x42;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int largeUntypedMapOpCode() {
		return 0x4B;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int nullTerminatedUntypedMapOpCode() {
		return 0x4C;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int tinyTypedMapOpCode() {
		return 0x4D;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int largeTypedMapOpCode() {
		return 0x56;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int nullTerminatedTypedMapOpCode() {
		return 0x57;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int emptyListOpCode() {
		return 0x2A;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int tinyListLimit() {
		return 9;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int tinyUntypedListOpCode() {
		return 0x2B;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int largeUntypedListOpCode() {
		return 0x34;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int nullTerminatedUntypedListOpCode() {
		return 0x35;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int tinyTypedListOpCode() {
		return 0x36;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int largeTypedListOpCode() {
		return 0x3F;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int nullTerminatedTypedListOpCode() {
		return 0x40;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int tinyMin() {
		return -31;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int tinyMax() {
		return 64;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int tinyOpCode() {
		return 0x80;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int smallMin() {
		return -2079;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int smallMax() {
		return 2112;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int smallNegativeOpCode() {
		return 0xE8;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int smallPositiveOpCode() {
		return 0xE0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int mediumMin() {
		return -264223;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int mediumMax() {
		return 264256;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int mediumNegativeOpCode() {
		return 0xF4;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int mediumPositiveOpCode() {
		return 0xF0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int largeMin() {
		return -33818655;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int largeMax() {
		return 33818688;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int largeNegativeOpCode() {
		return 0xFA;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int largePositiveOpCode() {
		return 0xF8;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int int32OpCode() {
		return 0xFC;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int tinyStrLimit() {
		return 32;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int tinyStrOpeCode() {
		return 0x03;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int smallStrLimit() {
		return 1056;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int smallStrOpeCode() {
		return 0x24;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int largeStrLimit() {
		return 66592;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int largeStrOpeCode() {
		return 0x28;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int chunkedStrOpeCode() {
		return 0x29;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int int64OpCode() {
		return 0xFD;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int float32opCode() {
		return 0xFE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int float64opCode() {
		return 0xFF;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getDeltaVectorOpCode() {
		return 0x63;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getEnvelopeOpCode() {
		return 0x60;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getMessageOpCode() {
		return 0x61;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getQuasiPeriodicVectorOpCode() {
		return 0x64;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getResponseOpCode() {
		return 0x62;
	}

}
