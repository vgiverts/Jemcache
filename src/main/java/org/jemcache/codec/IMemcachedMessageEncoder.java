package org.jemcache.codec;

import org.jemcache.api.MemcachedMessage;
import org.jemcache.util.IOutput;
import org.jemcache.util.IByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 7, 2007
 * Time: 11:48:27 AM
 */
public interface IMemcachedMessageEncoder {

    public void encode(MemcachedMessage message, IOutput<IByteBuffer> out);
}
