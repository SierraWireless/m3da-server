package m3da.server.services.clients;

import java.util.ArrayList;
import java.util.List;

import m3da.server.api.json.JClients;
import m3da.server.api.json.JM3daSecurityInfo;
import m3da.server.session.M3daSecurityInfo;
import m3da.server.store.SecurityStore;
import m3da.server.store.Store;

/**
 * Service to get JSON bean for the Clients servlet.
 */
public class JClientsService {

    private final Store store;
    private final SecurityStore securityStore;

    public JClientsService(Store storeService, SecurityStore securityStore) {
        this.store = storeService;
        this.securityStore = securityStore;
    }

    public JClients getClients() {
        JClients res = new JClients();

        List<String> in = new ArrayList<String>(store.incomingClientIds());
        List<String> out = new ArrayList<String>(store.outgoingClientIds());

        res.setIn(in);
        res.setOut(out);

        return res;
    }

    public void setSecurity(String clientId, JM3daSecurityInfo jSecurityInfo) {
        M3daSecurityInfo securityInfo = new M3daSecurityInfo();
        securityInfo.setM3daCommId(clientId);
        securityInfo.setM3daCipher(jSecurityInfo.getEncryption());
        securityInfo.setM3daSecurityType(jSecurityInfo.getAuthentication());
        securityInfo.setM3daSharedKey(jSecurityInfo.getPassword());
        securityInfo.setM3daNonce("000000");

        securityStore.addSecurityInfo(securityInfo);
    }

}
