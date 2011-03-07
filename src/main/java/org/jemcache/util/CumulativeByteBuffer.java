package org.jemcache.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 7, 2007
 * Time: 2:10:31 PM
 */
public class CumulativeByteBuffer implements IByteBuffer {

    private final ArrayList<ByteBuffer> buffers = new ArrayList<ByteBuffer>();

    private int limit = 0;
    private int position = 0;

    // Mark
    private boolean marked = false;
    private int mark;
    private int markedBufIdx;
    private int lastMarkedIdx;

    // Cur buf
    private int curBufIdx = 0;
    private ByteBuffer curBuf;
    private int curBufRemaining;

    public CumulativeByteBuffer() {
    }

    public CumulativeByteBuffer(ByteBuffer buffer) {
        add(buffer);
    }

    public CumulativeByteBuffer(ArrayList<ByteBuffer> buffers) {
        addAll(buffers);
    }

    public void flip() {
        for (int i = 0; i <= curBufIdx; i++) {
            buffers.get(i).flip();
        }
        setCurBuf(buffers.get(0));
        curBufIdx = 0;
        position = 0;
    }

    private void setCurBuf(ByteBuffer newCurBuf) {
        curBuf = newCurBuf;
        curBufRemaining = curBuf.remaining();
    }

    public void mark() {
        curBuf.mark();
        mark = position;
        markedBufIdx = curBufIdx;
        lastMarkedIdx = curBufIdx;
        marked = true;
    }

    public void reset() {
        if (!marked)
            throw new IllegalStateException("Cannot reset an unmarked buffer");

        for (int i = markedBufIdx; i <= lastMarkedIdx; i++)
            buffers.get(i).reset();

        position = mark;
        marked = false;
        setCurBuf(buffers.get(markedBufIdx));
        curBufIdx = markedBufIdx;
    }

    public int remaining() {
        return limit - position;
    }

    public byte get() {
        byte b = curBuf.get();
        position++;
        if (--curBufRemaining == 0)
            nextBuf();
        return b;
    }

    private void nextBuf() {
        if (position != limit) {
            setCurBuf(buffers.get(++curBufIdx));
            if (marked) {
                curBuf.mark();
                lastMarkedIdx = curBufIdx;
            }
        }
    }

    public int position() {
        return position;
    }

    public void get(byte[] dst) {
        get(dst, 0, dst.length);
    }

    public void get(byte[] dst, int offset, int length) {

        int bytesRead = 0;
        do {
            int toRead = Math.min(dst.length - bytesRead, curBufRemaining);
            curBuf.get(dst, offset + bytesRead, toRead);
            bytesRead += toRead;
            position += toRead;
            if ((curBufRemaining -= toRead) == 0)
                nextBuf();
        } while (bytesRead < length);
    }

    public boolean hasRemaining() {
        return position < limit;
    }

    public void add(IByteBuffer buf) {
        List<? extends ByteBuffer> list = buf.getBuffers();
        addAll(list);
    }

    private void addAll(List<? extends ByteBuffer> list) {
        for (ByteBuffer buffer : list) {
            add(buffer);
        }
        if (curBuf == null)
            setCurBuf(buffers.get(0));
    }

    public void add(ByteBuffer buffer) {
        limit += buffer.remaining();
        buffers.add(buffer);
        if (curBuf == null)
            setCurBuf(buffer);
    }

    public List<? extends ByteBuffer> getBuffers() {
        return buffers;
    }

    public void replace(ByteBuffer buf) {
        clear();
        add(buf);
    }

    private void clear() {
        buffers.clear();
        limit = 0;
        position = 0;
        marked = false;
        curBufIdx = 0;
        curBuf = null;
    }
}
