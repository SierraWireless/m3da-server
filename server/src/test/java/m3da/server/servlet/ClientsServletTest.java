package m3da.server.servlet;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import m3da.server.api.json.JClients;
import m3da.server.api.json.JSystemReadData;
import m3da.server.api.json.JSystemWriteSettings;
import m3da.server.services.clients.JClientsService;
import m3da.server.services.data.JDataService;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ClientsServletTest {
	@Mock private JDataService dataService;
	@Mock private JClientsService clientsService;
	@Mock private ObjectMapper mapper;
	@Mock private HttpServletRequest request;
	@Mock private HttpServletResponse response;
	@Mock private PrintWriter writer;
	@Mock private ServletInputStream input;
	
	private ClientsServlet servlet;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		servlet = new ClientsServlet(dataService, clientsService,  mapper);
	}
	
	@Test
	public void get_client_data() throws Exception {
		
		when(request.getPathInfo()).thenReturn("/1/data");
		when(response.getWriter()).thenReturn(writer);
		
		Map<String, List<JSystemReadData>> json = new HashMap<String, List<JSystemReadData>>();
		when(dataService.lastReceivedData("1")).thenReturn(json);
		
		servlet.doGet(request, response);
		
		verify(mapper).writeValue(writer, json);
	}
	
	@Test
	public void get_client_data_without_client_id() throws Exception {
		when(request.getPathInfo()).thenReturn("/prout/toto");
		servlet.doGet(request, response);
		verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, ClientsServlet.ERROR_NO_CLIENT);
	}
	
	@Test
	public void get_client_list() throws Exception {
		
		when(request.getPathInfo()).thenReturn(null);
		when(response.getWriter()).thenReturn(writer);
		
		JClients jClients = new JClients();
		when(clientsService.getClients()).thenReturn(jClients);
		
		servlet.doGet(request, response);
		
		verify(mapper).writeValue(writer, jClients);
		
	}
	
	
	@Test
	public void post_client_data() throws Exception {
		
		when(request.getPathInfo()).thenReturn("/1/data");
		when(request.getInputStream()).thenReturn(input);
		when(response.getWriter()).thenReturn(writer);
		
		JSystemWriteSettings settings = new JSystemWriteSettings();
		when(mapper.readValue(input, JSystemWriteSettings.class)).thenReturn(settings);
		
		dataService.enqueueReceivedData("1", settings);
		servlet.doPost(request, response);
		
	}
	
	@Test
	public void post_client_data_without_client_id() throws Exception {
		when(request.getPathInfo()).thenReturn("/prout/toto");
		servlet.doPost(request, response);
		verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, ClientsServlet.ERROR_NO_CLIENT);
	}
	
}
