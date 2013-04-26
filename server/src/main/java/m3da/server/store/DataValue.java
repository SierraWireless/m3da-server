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
package m3da.server.store;

/**
 * A value received by the server
 */
public class DataValue<TYPE> {

    /** the date of the data */
    private long timestamp;

    /** the data value */
    private TYPE value;

    public DataValue() {
    }

    public DataValue(long timestamp, TYPE value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public TYPE getValue() {
        return value;
    }

    public void setValue(TYPE value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "DataValue [timestamp=" + timestamp + ", value=" + value + "]";
    }
}
