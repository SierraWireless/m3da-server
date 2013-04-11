package m3da.server.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.MatchResult;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import m3da.server.api.json.JClients;
import m3da.server.api.json.JSystemReadData;
import m3da.server.api.json.JSystemWriteSettings;
import m3da.server.services.clients.JClientsService;
import m3da.server.services.data.JDataService;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet for "clients" API
 * 
 * <ul>
 * <li>GET  /clients (list of clients)</li>
 * <li>GET  /clients/1/data (last received data)</li>
 * <li>POST /clients/1/data (send data)</li>
 * </ul>
 *
 */
@SuppressWarnings("serial")
public class ClientsServlet extends JsonServlet {

	public static final String ERROR_NO_CLIENT = "No client in path";
	
	private static final Logger LOG = LoggerFactory
			.getLogger(ClientsServlet.class);
	private JDataService dataService;
	private JClientsService clientsService;

	public ClientsServlet(JDataService dataService,
			JClientsService clientsService, ObjectMapper jacksonMapper) {
		super(jacksonMapper);
		this.dataService = dataService;
		this.clientsService = clientsService;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.setResponseContentType(resp);
		String pathInfo = req.getPathInfo();
		if (StringUtils.isBlank(pathInfo)) {
			JClients jClients = this.clientsService.getClients();
			this.jacksonMapper.writeValue(resp.getWriter(), jClients);
		} else {
			String clientId = getClientId(pathInfo);
			if (clientId == null) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ERROR_NO_CLIENT);
			} else {
				Map<String, List<JSystemReadData>> jData = this.dataService
						.lastReceivedData(clientId);
				this.jacksonMapper.writeValue(resp.getWriter(), jData);
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.setResponseContentType(resp);
		
		String clientId = getClientId(req.getPathInfo());
		if (clientId == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ERROR_NO_CLIENT);
		} else {
			JSystemWriteSettings settings = this.jacksonMapper.readValue(
					req.getInputStream(), JSystemWriteSettings.class);
			this.dataService.enqueueReceivedData(clientId, settings);
		}

	}

	private String getClientId(String pathInfo) {
		String res = null;
		Scanner scanner = new Scanner(pathInfo);
		String match = scanner.findInLine("/(\\w+)/data");
		if (match != null) {
			MatchResult result = scanner.match();
			res = result.group(1);
		}
		return res;
	}
	
}
