package m3da.server.services.clients;

import java.util.ArrayList;
import java.util.List;

import m3da.server.api.json.JClients;
import m3da.server.store.StoreService;

/**
 * Service to get JSON bean for the Clients servlet.
 */
public class JClientsService {

	private StoreService storeService;

	public JClientsService(StoreService storeService) {
		this.storeService = storeService;
	}
	
	public JClients getClients() {
		JClients res = new JClients();
		
		List<String> in = new ArrayList<String>(storeService.incomingClientIds());
		List<String> out = new ArrayList<String>(storeService.outgoingClientIds());
		
		res.setIn(in);
		res.setOut(out);
		
		return res;
	}
	
}
