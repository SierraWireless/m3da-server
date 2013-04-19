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
import java.util.Scanner;
import java.util.regex.MatchResult;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import m3da.server.api.json.JClients;
import m3da.server.api.json.JM3daSecurityInfo;
import m3da.server.api.json.JSystemReadData;
import m3da.server.api.json.JSystemWriteSettings;
import m3da.server.services.clients.JClientsService;
import m3da.server.services.data.JDataService;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet for "clients" API
 * 
 * <ul>
 * <li>GET /clients (list of clients)</li>
 * <li>GET /clients/1/data (last received data)</li>
 * <li>POST /clients/1/data (send data)</li>
 * </ul>
 * 
 */
@SuppressWarnings("serial")
public class ClientsServlet extends JsonServlet {

    public static final String ERROR_NO_CLIENT = "No client in path";

    private static final Logger LOG = LoggerFactory.getLogger(ClientsServlet.class);

    private JDataService dataService;
    private JClientsService clientsService;

    public ClientsServlet(JDataService dataService, JClientsService clientsService, ObjectMapper jacksonMapper) {
        super(jacksonMapper);
        this.dataService = dataService;
        this.clientsService = clientsService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.setResponseContentType(resp);
        String pathInfo = req.getPathInfo();
        if (StringUtils.isBlank(pathInfo)) {
            JClients jClients = this.clientsService.getClients();
            this.jacksonMapper.writeValue(resp.getWriter(), jClients);
        } else {
            String clientId = getClientId(pathInfo, "data");
            if (clientId == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ERROR_NO_CLIENT);
            } else {
                Map<String, List<JSystemReadData>> jData = this.dataService.lastReceivedData(clientId);
                this.jacksonMapper.writeValue(resp.getWriter(), jData);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.setResponseContentType(resp);

        String pathInfo = req.getPathInfo();
        if (StringUtils.isBlank(pathInfo)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ERROR_NO_CLIENT);
        } else if (pathInfo.endsWith("/data")) {
            String clientId = getClientId(req.getPathInfo(), "data");
            if (clientId == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ERROR_NO_CLIENT);
            } else {
                JSystemWriteSettings settings = this.jacksonMapper.readValue(req.getInputStream(),
                        JSystemWriteSettings.class);
                this.dataService.enqueueReceivedData(clientId, settings);
            }
        } else if (pathInfo.endsWith("/security")) {
            String clientId = getClientId(req.getPathInfo(), "security");
            if (clientId == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ERROR_NO_CLIENT);
            } else {
                JM3daSecurityInfo security = this.jacksonMapper
                        .readValue(req.getInputStream(), JM3daSecurityInfo.class);

                LOG.info("new security : {} => {}", clientId, security);
                this.clientsService.setSecurity(clientId, security);
            }
        }

    }

    private String getClientId(String pathInfo, String endPath) {
        String res = null;
        Scanner scanner = new Scanner(pathInfo);
        String match = scanner.findInLine("/(\\w+)/" + endPath);
        if (match != null) {
            MatchResult result = scanner.match();
            res = result.group(1);
        }
        return res;
    }

}
