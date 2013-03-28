/*******************************************************************************
 * Copyright (c) 2012 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package m3da.server.api.json;

import java.util.List;

/**
 * TODO Comment this class
 */
public class JSystemWriteSettings {

	private List<JSystemWriteData> settings;

	/**
	 * Dummy constructor needed by jackson
	 */
	public JSystemWriteSettings() {
	}

	/**
	 * @param settings
	 */
	public JSystemWriteSettings(List<JSystemWriteData> settings) {
		super();
		this.settings = settings;
	}

	/**
	 * @return the settings
	 */
	public List<JSystemWriteData> getSettings() {
		return settings;
	}

	/**
	 * @param settings
	 *            the settings to set
	 */
	public void setSettings(List<JSystemWriteData> settings) {
		this.settings = settings;
	}

}
