package org.jemcache.util;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 7, 2007
 * Time: 2:01:13 PM
 */
public interface IByteBuffer {
    public void flip();
    public void mark();
    public void reset();
    public int remaining();
    public byte get();
    public int position();

    public void get(byte[] dst);
    public void get(byte[] dst, int offset, int length);
    public boolean hasRemaining();
    public List<? extends ByteBuffer> getBuffers();
}
