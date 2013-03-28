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
package m3da.server.codec.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * The M3DA::DeltasVector allows improving data compression for data that are similar (data are transmitted as deltas instead of absolute values).
 * This object may be used when a vector of values is quasi periodic, meaning they are periodic plus small deltas (due to sampling errors for
 * instance).
 */
public class M3daDeltasVector {

	private final Number factor;
	private final Number start;
	private final List<Number> deltas;

	public M3daDeltasVector(Number factor, Number start, List<Number> deltas) {
		super();
		this.factor = factor;
		this.start = start;
		this.deltas = deltas;
	}

	public Number getFactor() {
		return factor;
	}

	public Number getStart() {
		return start;
	}

	public List<Number> getDeltas() {
		return deltas;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((deltas == null) ? 0 : deltas.hashCode());
		result = prime * result + ((factor == null) ? 0 : factor.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		return result;
	}

	/**
	 * @return the list of value (Long or Double) for this delta vector
	 */
	public List<? extends Number> asFlatList() {

		boolean haveDouble = false;

		if ((start instanceof Float) || (start instanceof Double)) {
			haveDouble = true;
		} else {
			for (Number n : deltas) {
				if ((n instanceof Float) || (n instanceof Double)) {
					haveDouble = true;
					break;
				}
			}
		}

		if (haveDouble) {
			List<Double> res = new ArrayList<Double>(deltas.size() + 1);
			res.add(factor.doubleValue() * start.doubleValue());
			double last = res.get(0);
			for (int i = 0; i < deltas.size(); i++) {
				double value = deltas.get(i).doubleValue() * factor.doubleValue() + last;
				res.add(value);
				last = value;
			}
			return res;
		} else {
			List<Long> res = new ArrayList<Long>(deltas.size() + 1);
			res.add(factor.longValue() * start.longValue());
			long last = res.get(0);
			for (int i = 0; i < deltas.size(); i++) {
				long value = deltas.get(i).longValue() * factor.longValue() + last;
				res.add(value);
				last = value;
			}
			return res;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		M3daDeltasVector other = (M3daDeltasVector) obj;
		if (deltas == null) {
			if (other.deltas != null) {
				return false;
			}
		} else if (!deltas.equals(other.deltas)) {
			return false;
		}
		if (factor == null) {
			if (other.factor != null) {
				return false;
			}
		} else if (!factor.equals(other.factor)) {
			return false;
		}
		if (start == null) {
			if (other.start != null) {
				return false;
			}
		} else if (!start.equals(other.start)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "AwtDa3DeltasVector [factor=" + factor + ", start=" + start + ", deltas=" + deltas + "]";
	}

}
