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
package m3da.codec.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * The M3DA::QuasiPeriodicVector allows improving data compression for data that are quasi periodic (only shifts from a period are sent).
 */
public class M3daQuasiPeriodicVector {
	private final Number period;
	private final Number start;
	private final List<Number> shifts;

	/**
	 * Create a Quasi periodic vector.
	 * 
	 * @param period
	 *            the period between the elements
	 * @param start
	 *            the start values
	 * @param shifts
	 *            the shift between the periodic value and the real sample
	 */
	public M3daQuasiPeriodicVector(Number period, Number start, List<Number> shifts) {
		super();
		this.period = period;
		this.start = start;
		this.shifts = shifts;
	}

	public Number getPeriod() {
		return period;
	}

	public Number getStart() {
		return start;
	}

	public List<Number> getShifts() {
		return shifts;
	}

	/**
	 * @return the uncompressed list of number of the vector, can be a list of Double or Long depending of the vector content
	 */
	public List<? extends Number> asFlatList() {
		boolean haveDouble = false;

		if ((start instanceof Float) || (start instanceof Double)) {
			haveDouble = true;
		} else {
			for (Number n : shifts) {
				if ((n instanceof Float) || (n instanceof Double)) {
					haveDouble = true;
					break;
				}
			}
		}
		if (haveDouble) {
			List<Double> res = new ArrayList<Double>();
			double last = start.doubleValue();
			res.add(last);
			for (int i = 0; i < shifts.size() / 2; i++) {
				long nbRepeat = shifts.get(i * 2).longValue();
				double shift = shifts.get(i * 2 + 1).doubleValue();
				for (int j = 0; j < nbRepeat; j++) {
					double newValue = last + period.doubleValue();
					res.add(newValue);
					last = newValue;
				}
				double newValue = last + period.doubleValue() + shift;
				res.add(newValue);
				last = newValue;
			}
			long lastRepeat = shifts.get(shifts.size() - 1).longValue();
			for (int i = 0; i < lastRepeat; i++) {
				double newValue = last + period.doubleValue();
				res.add(newValue);
				last = newValue;
			}
			return res;
		} else {
			List<Long> res = new ArrayList<Long>();
			long last = start.longValue();
			res.add(last);
			for (int i = 0; i < shifts.size() / 2; i++) {
				long nbRepeat = shifts.get(i * 2).longValue();
				long shift = shifts.get(i * 2 + 1).longValue();
				for (int j = 0; j < nbRepeat; j++) {
					long newValue = last + period.longValue();
					res.add(newValue);
					last = newValue;
				}
				long newValue = last + period.longValue() + shift;
				res.add(newValue);
				last = newValue;
			}
			long lastRepeat = shifts.get(shifts.size() - 1).longValue();
			for (int i = 0; i < lastRepeat; i++) {
				long newValue = last + period.longValue();
				res.add(newValue);
				last = newValue;
			}
			return res;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AwtDa3QuasiPeriodicVector [period=");
		builder.append(period);
		builder.append(", start=");
		builder.append(start);
		builder.append(", shifts=");
		builder.append(shifts);
		builder.append("]");
		return builder.toString();
	}
}
