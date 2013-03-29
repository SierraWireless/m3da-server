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

/**
 * the NUMBER context specialized in signed number
 */
public class NumbersCtxEncoding implements NumberEncoding, Encoding {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int tinyMin() {
		return -97;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int tinyMax() {
		return +97;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int tinyOpCode() {
		return 0x01;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int smallMin() {
		return -4193;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int smallMax() {
		return +4193;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int smallNegativeOpCode() {
		return 0xD4;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int smallPositiveOpCode() {
		return 0xC4;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int mediumMin() {
		return -528481;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int mediumMax() {
		return +528481;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int mediumNegativeOpCode() {
		return 0xEC;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int mediumPositiveOpCode() {
		return 0xE4;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int largeMin() {
		return -67637345;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int largeMax() {
		return 67637345;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int largeNegativeOpCode() {
		return 0xF8;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int largePositiveOpCode() {
		return 0xF4;
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

}
