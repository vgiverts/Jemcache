package org.jemcache.util;

import java.nio.ByteBuffer;
import java.sql.Date;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 7, 2007
 * Time: 2:09:06 PM
 */
public class SimpleByteBuffer implements IByteBuffer {
    private final ByteBuffer buf;

    public SimpleByteBuffer(ByteBuffer buf) {
        this.buf = buf;
    }

    public void flip() {
        buf.flip();
    }

    public void mark() {
        buf.mark();
    }

    public void reset() {
        buf.reset();
    }

    public int remaining() {
        return buf.remaining();
    }

    public byte get() {
        return buf.get();
    }

    public int position() {
        return buf.position();
    }

    public void position(int pos) {
        buf.position(pos);
    }

    public void get(byte[] dst) {
        buf.get(dst);
    }

    public void get(byte[] dst, int offset, int length) {
        buf.get(dst, offset, length);
    }

    public boolean hasRemaining() {
        return buf.hasRemaining();
    }

    public List<? extends ByteBuffer> getBuffers() {
        return Collections.singletonList(buf);
    }

    public ByteBuffer getBuf() {
        return buf;
    }
}