package org.jemcache.codec;

import org.jemcache.api.MemcachedMessage;
import org.jemcache.server.ISocketSession;
import org.jemcache.util.IOutput;
import org.jemcache.util.IByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 7, 2007
 * Time: 11:52:12 AM
 */
public interface IMemcachedMessageDecoder {
    /**
     *
     * @param buf The incoming bytes.
     * @return True is the message was fully decoded, false if more bytes are needed.
     */
    public boolean decode(IByteBuffer buf, IOutput<MemcachedMessage> out, ISocketSession session);
}
