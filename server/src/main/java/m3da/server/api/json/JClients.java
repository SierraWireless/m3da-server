package m3da.server.api.json;

import java.util.List;

/**
 * JSON Bean for list of connected clients.
 */
public class JClients {

	List<String> in;
	List<String> out;

	/**
	 * @return the ids of clients that have recently sent data.
	 */
	public List<String> getIn() {
		return in;
	}
	
	public void setIn(List<String> in) {
		this.in = in;
	}
	
	/**
	 * @return the ids of client for which there is still some
	 * data to send. 
	 */
	public List<String> getOut() {
		return out;
	}
	
	public void setOut(List<String> out) {
		this.out = out;
	}
	
	
	
}
