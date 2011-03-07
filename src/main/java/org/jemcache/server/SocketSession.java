package org.jemcache.server;

import org.jemcache.api.MemcachedMessage;
import org.jemcache.codec.IMemcachedMessageDecoder;
import org.jemcache.codec.IMemcachedMessageEncoder;
import org.jemcache.engine.ICommandProcessor;
import org.jemcache.util.IByteBuffer;
import org.jemcache.util.IOutput;

import java.util.HashMap;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectionKey;
import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 7, 2007
 * Time: 1:25:20 PM
 */
public class SocketSession implements ISocketSession, IOutput<IByteBuffer> {

    final HashMap<String, Object> attrMap = new HashMap<String, Object>();

    final ICommandProcessor commandProcessor;
    final IWriteJob writeJob;
    final IMemcachedMessageDecoder decoder;
    final IMemcachedMessageEncoder encoder;
    final OutputToWriteJob outputToWriteJob = new OutputToWriteJob();
    final OutputToEncoder outputToEncoder = new OutputToEncoder();
    final OutputToCommandProcessor outputToCommandProcessor = new OutputToCommandProcessor();
    boolean closed = false;
    SocketChannel channel;
    private SelectionKey key;

    public SocketSession(ICommandProcessor commandProcessor, IWriteJob writeJob, IMemcachedMessageDecoder decoder, IMemcachedMessageEncoder encoder, SocketChannel channel) {
        this.commandProcessor = commandProcessor;
        this.writeJob = writeJob;
        this.decoder = decoder;
        this.encoder = encoder;
        this.channel = channel;
    }

    public void setKey(SelectionKey key) {
        this.key = key;
    }

    public Object setAttribute(String key, Object value) {
        return attrMap.put(key, value);
    }

    public Object getAttribute(String key) {
        return attrMap.get(key);
    }

    public Object removeAttribute(String key) {
        return attrMap.remove(key);
    }

    public IWriteJob getWriteJob() {
        return writeJob;
    }

    public void write(IByteBuffer buf) {
        decoder.decode(buf, outputToCommandProcessor, this);
    }

    public void close() {
        closed = true;
        writeJob.close();
    }

    public boolean isClosed() {
        return closed;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public SelectionKey getKey() {
        return key;
    }

    private class OutputToWriteJob implements IOutput<IByteBuffer> {
        public void write(IByteBuffer message) {
            for (ByteBuffer buffer : message.getBuffers())
                writeJob.add(buffer);
        }
    }

    private class OutputToEncoder implements IOutput<MemcachedMessage> {
        public void write(final MemcachedMessage message) {
            encoder.encode(message, outputToWriteJob);
        }
    }

    private class OutputToCommandProcessor implements IOutput<MemcachedMessage> {
        public void write(MemcachedMessage message) {
            commandProcessor.processMessage(message, outputToEncoder);
        }
    }


}
