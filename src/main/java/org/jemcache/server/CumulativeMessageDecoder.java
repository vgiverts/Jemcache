package org.jemcache.server;

import org.jemcache.codec.IMemcachedMessageDecoder;
import org.jemcache.api.MemcachedMessage;
import org.jemcache.util.CumulativeByteBuffer;
import org.jemcache.util.IByteBuffer;
import org.jemcache.util.IOutput;
import org.jemcache.util.SimpleByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 7, 2007
 * Time: 1:32:08 PM
 */
public class CumulativeMessageDecoder implements IMemcachedMessageDecoder {
    private static final String BUFFER = CumulativeMessageDecoder.class.getName() + ".Buffer";

    private IMemcachedMessageDecoder decoder;

    /**
     * Creates a new instance.
     */
    public CumulativeMessageDecoder(IMemcachedMessageDecoder decoder) {
        this.decoder = decoder;
    }

    public boolean decode(IByteBuffer in, IOutput<MemcachedMessage> out, ISocketSession session) {
        boolean usingSessionBuffer = true;
        IByteBuffer buf = (IByteBuffer) session.getAttribute(BUFFER);
        // If we have a session buffer, append data to that; otherwise
        // use the buffer read from the network directly.
        if (buf != null) {
            ((CumulativeByteBuffer) buf).add(in);
        } else {
            buf = in;
            usingSessionBuffer = false;
        }

        boolean outerDecoded = false;

        while (true) {
            boolean decoded = decoder.decode(buf, out, session);
            if (decoded) {
                outerDecoded = true;
                if (!buf.hasRemaining()) {
                    break;
                }
            } else {
                break;
            }
        }

        // if there is any data left that cannot be decoded, we store
        // it in a buffer in the session and next time this decoder is
        // invoked the session buffer gets appended to
        if (buf.hasRemaining()) {
            if (!usingSessionBuffer) {
                storeRemainingInSession(buf, session);
            }
        } else {
            if (usingSessionBuffer)
                removeSessionBuffer(session);
        }

        return outerDecoded;
    }

    private void removeSessionBuffer(ISocketSession session) {
        session.removeAttribute(BUFFER);
    }

    private void storeRemainingInSession(IByteBuffer buf, ISocketSession session) {
        if (buf instanceof CumulativeByteBuffer)
            session.setAttribute(BUFFER, buf);
        else {
            session.setAttribute(BUFFER, new CumulativeByteBuffer(((SimpleByteBuffer) buf).getBuf()));
        }
    }
}
