package org.jemcache.api;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 5, 2007
 * Time: 3:16:01 PM
 */
public class SimpleResponse extends MemcachedMessage {

    public SimpleResponse() {
        super();
    }

    public SimpleResponse(Cmd cmd) {
        super(cmd);
    }

}
