package org.jemcache.server;

import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 8, 2007
 * Time: 11:36:07 AM
 */
public interface IWriteJob {

    // Returns true if the job completed. False if it needs to be run again at a later time.
    public boolean run() throws WriteJobException;

    public boolean add(ByteBuffer byteBuffer);

    public void close();

    public boolean isEmpty();

}
