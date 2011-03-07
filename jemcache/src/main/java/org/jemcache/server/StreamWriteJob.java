package org.jemcache.server;

import java.io.OutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 18, 2007
 * Time: 5:09:06 PM
 */
public class StreamWriteJob implements IWriteJob {

    private final OutputStream out;

    public StreamWriteJob(OutputStream out) {
        this.out = out;
    }

    public boolean run() throws WriteJobException {
        throw new UnsupportedOperationException();
    }

    public boolean add(ByteBuffer byteBuffer) {
        try {
            int r = byteBuffer.remaining();
            byte[] bytes = new byte[r];
            byteBuffer.get(bytes);
            out.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false; 
    }

    public void close() {
        try {
            out.close();
        } catch (IOException e) {
            //todo: log this
        }
    }

    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

}
