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
package m3da.server.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import m3da.server.api.json.JSystemReadData;
import m3da.server.api.json.JSystemWriteSettings;
import m3da.server.api.mapping.Store2JsonDataMapper;
import m3da.server.services.data.JDataService;
import m3da.server.store.Envelope;
import m3da.server.store.Message;
import m3da.server.store.StoreService;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet for legacy "data" servlet. "clients" API should be used instead.
 *
 */
@SuppressWarnings("serial")
public class DataServlet extends JsonServlet {

	private static final Logger LOG = LoggerFactory.getLogger(DataServlet.class);

	private JDataService dataService; 
	
	public DataServlet(JDataService dataService, ObjectMapper jasksonMapper) {
		super(jasksonMapper);
		this.dataService = dataService;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String system = req.getPathInfo();
		this.setResponseContentType(resp);
		if (system == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "no system id in the path");
			return;
		}
		system = system.substring(1);
		LOG.info("system " + system);

		Map<String, List<JSystemReadData>> json = this.dataService.lastReceivedData(system);

		this.jacksonMapper.writeValue(resp.getWriter(), json);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.setResponseContentType(resp);
		String system = req.getPathInfo();
		if (system == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "no system id in the path");
			return;
		}
		system = system.substring(1);

		JSystemWriteSettings settings = this.jacksonMapper.readValue(req.getInputStream(), JSystemWriteSettings.class);
		this.dataService.enqueueReceivedData(system, settings);
	}

}
