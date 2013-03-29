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
 * the Unsigned Integer and String context
 */
public class UintStrCtxEncoding implements Encoding, UintEncoding, StringEncoding {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int tinyMax() {
		return 139;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int tinyOpeCode() {
		return 0x3B;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int smallMax() {
		return 8331;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int smallOpeCode() {
		return 0xC7;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int mediumMax() {
		return 1056907;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int mediumOpeCode() {
		return 0xE7;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int largeMax() {
		return 135274635;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int largeOpeCode() {
		return 0xF7;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int uint32OpeCode() {
		return 0xFF;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int tinyStrLimit() {
		return 47;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int tinyStrOpeCode() {
		return 0x1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int smallStrLimit() {
		return 2095;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int smallStrOpeCode() {
		return 0x31;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int largeStrLimit() {
		return 67631;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int largeStrOpeCode() {
		return 0x39;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int chunkedStrOpeCode() {
		return 0x3A;
	}
}
