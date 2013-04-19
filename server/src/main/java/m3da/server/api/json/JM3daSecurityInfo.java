package m3da.server.api.json;

import m3da.server.session.M3daAuthentication;
import m3da.server.session.M3daCipher;

/**
 * API bean for M3DA client security informations.
 */
public class JM3daSecurityInfo {

    private M3daAuthentication authentication;

    private M3daCipher encryption;

    private String password;

    public M3daAuthentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(M3daAuthentication authentication) {
        this.authentication = authentication;
    }

    public M3daCipher getEncryption() {
        return encryption;
    }

    public void setEncryption(M3daCipher cipher) {
        this.encryption = cipher;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "JM3daSecurityInfo [authentication=" + authentication + ", encryption=" + encryption + ", password="
                + password + "]";
    }
}
