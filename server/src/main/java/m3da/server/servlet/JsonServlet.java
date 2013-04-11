package m3da.server.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

public abstract class JsonServlet extends HttpServlet {

	protected ObjectMapper jacksonMapper;

	/**
	 * @param jacksonMapper
	 */
	public JsonServlet(ObjectMapper jacksonMapper) {
		super();
		this.jacksonMapper = jacksonMapper;
	}

	protected void setResponseContentType(HttpServletResponse resp) {
		resp.setContentType("application/json;charset=utf-8");
	}

}