package org.jemcache.api;

import org.jemcache.IJemcacheConstants;
import org.jemcache.util.IByteBuffer;
import org.jemcache.util.CumulativeByteBuffer;

import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 3, 2007
 * Time: 3:14:49 PM
 */
public abstract class MemcachedMessage implements IJemcacheConstants {

    public void parse(String args) {
    }

    private Cmd cmd;

    protected MemcachedMessage() {
    }

    protected MemcachedMessage(Cmd cmd) {
        this.cmd = cmd;
    }

    public Cmd getType() {
        return cmd;
    }

    public Category getCategory() {
        return cmd.getCategory();
    }

    public void setCmd(Cmd cmd) {
        this.cmd = cmd;
    }

    public byte[][] toBytes() {
        return new byte[][]{toString().getBytes()};
    }

    public String toString() {
        return getType() + "\r\n";
    }

    public String getServerKey() {
        throw new UnsupportedOperationException("This command does not use a key: " + toString());
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MemcachedMessage)) return false;

        MemcachedMessage message = (MemcachedMessage) o;

        return cmd == message.cmd;
    }

    public int hashCode() {
        int result;
        result = (cmd != null ? cmd.hashCode() : 0);
        return result;
    }

    public IByteBuffer toBuffer() {
        CumulativeByteBuffer buffers = new CumulativeByteBuffer();
        for (byte[] bytes : toBytes()) {
            buffers.add(ByteBuffer.wrap(bytes));
        }
        return buffers;
    }
}
