package org.jemcache.api;

import org.jemcache.util.IByteBuffer;
import org.jemcache.util.CumulativeByteBuffer;

import java.util.Arrays;
import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 5, 2007
 * Time: 9:59:27 AM
 */
public abstract class DataMessage extends ModificationCommand {

    private static final byte[] EOL_BYTES = "\r\n".getBytes();

    private final ByteBuffer buf;
    private byte[] data;
    private int bytes;
    private int bytesRead;

    public DataMessage() {
        super();
        this.buf = null;
    }

    public DataMessage(Cmd cmd, String key, byte[] data, boolean noreply) {
        super(cmd, key, noreply);
        this.bytes = data.length;
        this.data = data;
        this.buf = null;
    }

    public DataMessage(Cmd cmd, String key, ByteBuffer data, boolean noreply) {
        super(cmd, key, noreply);
        this.bytes = data.remaining();
        this.buf = data;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getBytes() {
        return bytes;
    }

    public void setBytes(int bytes) {
        this.bytes = bytes;
        data = new byte[bytes];
    }

    public int getBytesRead() {
        return bytesRead;
    }

    public void setBytesRead(int bytesRead) {
        this.bytesRead = bytesRead;
    }

    public byte[][] toBytes() {
        return new byte[][]{toString().getBytes(), getData(), EOL_BYTES};
    }

    public IByteBuffer toBuffer() {
        if (data != null)
            return super.toBuffer();
        else {
            CumulativeByteBuffer buffers = new CumulativeByteBuffer();
            buffers.add(ByteBuffer.wrap(toString().getBytes()));
            buffers.add(buf);
            buffers.add(ByteBuffer.wrap(EOL_BYTES));
            return buffers;
        }
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataMessage)) return false;
        if (!super.equals(o)) return false;

        DataMessage that = (DataMessage) o;

        return super.equals(o) && bytes == that.bytes && Arrays.equals(data, that.data);

    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (data != null ? Arrays.hashCode(data) : 0);
        result = 31 * result + bytes;
        result = 31 * result + bytesRead;
        result = 31 * result + super.hashCode();
        return result;
    }


}
