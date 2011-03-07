package org.jemcache.engine;

import org.jemcache.api.MemcachedMessage;
import org.jemcache.util.IOutput;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 7, 2007
 * Time: 12:21:55 PM
 */
public interface ICommandProcessor {
    public void processMessage(MemcachedMessage message, IOutput<MemcachedMessage> out);
}
