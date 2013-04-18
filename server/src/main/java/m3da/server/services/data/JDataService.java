package m3da.server.services.data;

import java.util.List;
import java.util.Map;

import m3da.server.api.json.JSystemReadData;
import m3da.server.api.json.JSystemWriteSettings;
import m3da.server.api.mapping.Store2JsonDataMapper;
import m3da.server.store.Envelope;
import m3da.server.store.Message;
import m3da.server.store.Store;

/**
 * Service to get JSON Bean for data servlet.
 *
 */
public class JDataService {

	private Store2JsonDataMapper store2JsonMapper;
	private Store store;

	public JDataService(Store store, Store2JsonDataMapper store2JsonMapper) {
		this.store = store;
		this.store2JsonMapper = store2JsonMapper;
	}
	
	public Map<String, List<JSystemReadData>> lastReceivedData(String system) {
		Map<Long, Envelope> data = store.lastReceivedData(system);
		return this.store2JsonMapper.mapReceivedData(data);
	}
	
	public void enqueueReceivedData(String system, JSystemWriteSettings settings) {
		List<Message> newData = store2JsonMapper.mapDataToSend(settings);
		store.enqueueDataToSend(system, newData);
	}
	
}
