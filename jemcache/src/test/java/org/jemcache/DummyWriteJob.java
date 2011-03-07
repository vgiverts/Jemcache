package org.jemcache;

import org.jemcache.server.IWriteJob;
import org.jemcache.server.WriteJobException;

import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Aug 28, 2008
 * Time: 10:20:52 AM
 */
public class DummyWriteJob implements IWriteJob {

    public boolean run() throws WriteJobException {
        return false;
    }

    public boolean add(ByteBuffer byteBuffer) {
        return false;
    }

    public void close() {
    }

    public boolean isEmpty() {
        return false;
    }

}
