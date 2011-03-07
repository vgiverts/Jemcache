package org.jemcache;

import org.jemcache.api.*;
import org.jemcache.codec.IMemcachedMessageDecoder;
import org.jemcache.codec.IMemcachedMessageEncoder;
import org.jemcache.codec.MemcachedProtocolDecoder;
import org.jemcache.codec.MemcachedProtocolEncoder;
import org.jemcache.server.CumulativeMessageDecoder;
import org.jemcache.server.SocketSession;
import org.jemcache.util.CumulativeByteBuffer;
import org.jemcache.util.IByteBuffer;
import org.jemcache.util.IOutput;
import junit.framework.TestCase;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 5, 2007
 * Time: 10:50:41 AM
 */
public class TestMemcachedProtocol extends TestCase {

    Object latestOutput;

    IMemcachedMessageDecoder decoder2;
    IMemcachedMessageEncoder encoder2;
    private IOutput<MemcachedMessage> decoderOut2 = new IOutput<MemcachedMessage>() {
        public void write(MemcachedMessage message) {
            latestOutput = message;
        }
    };
    private SocketSession session2 = new SocketSession(null, new DummyWriteJob(), null, null, null);

    public TestMemcachedProtocol() throws Exception {
        decoder2 = new CumulativeMessageDecoder(new MemcachedProtocolDecoder());
        encoder2 = new MemcachedProtocolEncoder();
    }

    protected void setUp() throws Exception {
        latestOutput = null;
    }

    public void testOtherCommandsSocketServer() throws Exception {
        processCmdSocketServer(new StorageCommand(MemcachedMessage.Cmd.add, "myKey", 123, 456000, 789L, "my data".getBytes(), false));
        processCmdSocketServer(new StorageCommand(MemcachedMessage.Cmd.add, "myKey", 123, 456000, 789L, "my data".getBytes(), true));
        processCmdSocketServer(new DeletionCommand("myKey", 123000L, false));
        processCmdSocketServer(new DeletionCommand("myKey", 123000L, true));
        processCmdSocketServer(new DeletionCommand("myKey", null, false));
        processCmdSocketServer(new DeletionCommand("myKey", null, true));
        processCmdSocketServer(new IncrementalCommand(MemcachedMessage.Cmd.incr, "myKey", 5, false));
        processCmdSocketServer(new IncrementalCommand(MemcachedMessage.Cmd.incr, "myKey", 5, true));
        processCmdSocketServer(new RetrievalCommand(MemcachedMessage.Cmd.get, "myKey1", "myKey2", "myKey3"));
        processCmdSocketServer(new RetrievalCommand(MemcachedMessage.Cmd.gets, "myKey1", "myKey2", "myKey3"));
        processCmdSocketServer(new ValueResponse("myKey1", 123, "my data".getBytes(), true));
        processCmdSocketServer(new ValueResponse("myKey1", 123, "my data".getBytes(), false));
        processCmdSocketServer(new SimpleValueResponse(1234));
        processCmdSocketServer(new SimpleResponse(MemcachedMessage.Cmd.EXISTS));
        processCmdSocketServer(new StatResponse("asdf","qwer"));
        processCmdSocketServer(new StatusCommand("asdf"));
        processCmdSocketServer(new StatusCommand());
    }


    private void processCmdSocketServer(MemcachedMessage cmd) throws Exception {
        process2(0, cmd);
        assertEquals(cmd, latestOutput);
        process2(2, cmd);
        assertEquals(cmd, latestOutput);
        process2(1000, cmd);
        assertEquals(cmd, latestOutput);
    }


    private void process2(int bufferSize, MemcachedMessage cmd) {
        latestOutput = null;
        if (bufferSize == 0) {
            encoder2.encode(cmd, new IOutput<IByteBuffer>() {
                public void write(IByteBuffer message) {
                    decoder2.decode(message, decoderOut2, session2);
                }
            });
        } else {
            int totalLen = 0;
            for (byte[] bytes : cmd.toBytes()) {
                totalLen += bytes.length;
            }

            byte[] allBytes = new byte[totalLen];
            int idx = 0;
            for (byte[] bytes : cmd.toBytes()) {
                System.arraycopy(bytes, 0, allBytes, idx, bytes.length);
                idx += bytes.length;
            }

            ArrayList<java.nio.ByteBuffer> buffers = new ArrayList<java.nio.ByteBuffer>();

            java.nio.ByteBuffer byteBuffer = java.nio.ByteBuffer.allocate(bufferSize);
            buffers.add(byteBuffer);

            for (byte b : allBytes) {
                if (byteBuffer.remaining() > 0)
                    byteBuffer.put(b);
                else {
                    byteBuffer.flip();
                    byteBuffer = java.nio.ByteBuffer.allocate(bufferSize);
                    buffers.add(byteBuffer);
                    byteBuffer.put(b);
                }
            }
            byteBuffer.flip();
            decoder2.decode(new CumulativeByteBuffer(buffers), decoderOut2, session2);
        }

    }

}
