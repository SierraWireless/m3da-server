package m3da.server.tcp;

import java.nio.ByteBuffer;

import m3da.codec.HeaderKey;
import m3da.codec.dto.M3daEnvelope;
import m3da.server.session.M3daSecurityInfo;
import m3da.server.session.M3daSession;
import m3da.server.store.SecurityStore;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A filter for loading the communication information in the {@link M3daSession}.
 */
public class ComInfoFilter extends IoFilterAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(ComInfoFilter.class);

    private final SecurityStore securityStore;

    /** session attachment key for m3da session */
    public static final String M3DA_SESSION = "M3daSession";

    public ComInfoFilter(SecurityStore securityStore) {
        this.securityStore = securityStore;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
        if (message instanceof M3daEnvelope) {
            M3daEnvelope env = (M3daEnvelope) message;

            // retrieve communication id
            final Object objComId = env.getHeader().get(HeaderKey.ID);
            if (objComId == null || !(objComId instanceof ByteBuffer)) {
                LOG.debug("m3da envelope without communication id");
                session.close(true);
            } else {

                final String communicationId = new String(((ByteBuffer) objComId).array(), "UTF-8");

                M3daSession m3daSession = getSession(session, communicationId);

                // is the security info already loaded in the session ?
                M3daSecurityInfo secInfo = m3daSession.getCommunicationInfo();
                if (secInfo == null) {
                    LOG.debug("loading communication info for system {}", communicationId);

                    secInfo = securityStore.getSecurityInfo(communicationId);

                    m3daSession.setCommunicationInfo(secInfo);
                    m3daSession.setCommunicationId(communicationId);
                }
                // call the next filter
                nextFilter.messageReceived(session, message);
            }
        } else {
            LOG.warn(message.getClass() + " is not a M3daEnvelope, correct filter chain setup ?");
            session.close(true);
        }
    }

    /** create the M3daSession from the MINA IoSession attribute */
    private M3daSession getSession(IoSession ioSession, String communicationId) {
        M3daSession session = (M3daSession) ioSession.getAttribute(M3DA_SESSION);
        if (session == null) {
            session = new M3daSession();
            ioSession.setAttribute(M3DA_SESSION, session);
        }
        return session;
    }
}
