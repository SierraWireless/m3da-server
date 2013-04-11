package m3da.server.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import m3da.server.api.json.JSystemReadData;
import m3da.server.api.json.JSystemWriteSettings;
import m3da.server.services.data.JDataService;

import org.codehaus.jackson.impl.WriterBasedGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class DataServletTest {

	@Mock private JDataService dataService;
	@Mock private ObjectMapper mapper;
	@Mock private HttpServletRequest request;
	@Mock private HttpServletResponse response;
	@Mock private PrintWriter writer;
	@Mock private ServletInputStream input;
	
	private DataServlet servlet;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		servlet = new DataServlet(dataService, mapper);
	}
	
	@Test
	public void get_data() throws IOException, ServletException {
		
		when(request.getPathInfo()).thenReturn("/1");
		when(response.getWriter()).thenReturn(writer);
		
		Map<String, List<JSystemReadData>> json = new HashMap<String, List<JSystemReadData>>();
		when(dataService.lastReceivedData("1")).thenReturn(json);
		
		mapper.writeValue(writer, json);
		servlet.doGet(request, response);
		
	}
	
	@Test
	public void post_data() throws IOException, ServletException {
		
		when(request.getPathInfo()).thenReturn("/1");
		when(request.getInputStream()).thenReturn(input);
		when(response.getWriter()).thenReturn(writer);
		
		JSystemWriteSettings settings = new JSystemWriteSettings();
		when(mapper.readValue(input, JSystemWriteSettings.class)).thenReturn(settings);
		
		dataService.enqueueReceivedData("1", settings);
		servlet.doPost(request, response);
		
	}
	
}
