package org.jemcache.codec;

import org.jemcache.api.MemcachedMessage;
import org.jemcache.util.IByteBuffer;
import org.jemcache.util.IOutput;


/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 5, 2007
 * Time: 6:01:23 PM
 */
public class MemcachedProtocolEncoder implements IMemcachedMessageEncoder {

    public void encode(final MemcachedMessage message, final IOutput<IByteBuffer> out) {
        out.write(message.toBuffer());
    }
}
