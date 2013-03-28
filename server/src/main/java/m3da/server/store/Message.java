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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * A message containing some value, received from the client
 */
public class Message implements Serializable {

	private static final long serialVersionUID = 1L;

	/** path */
	private final String path;

	/** associated data */
	private final Map<String, List<?>> data;

	public Message(String path, Map<String, List<?>> data) {
		super();
		this.path = path;
		this.data = data;
	}

	public String getPath() {
		return path;
	}

	public Map<String, List<?>> getData() {
		return data;
	}

	@Override
	public String toString() {
		return "Data [path=" + path + ", data=" + data + "]";
	}

}
