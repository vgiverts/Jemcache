package org.jemcache.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 7, 2007
 * Time: 1:04:37 PM
 */
class BufferWriteJob implements IWriteJob {
    private final Job head;
    private final SocketChannel channel;
    private boolean closed = false;
    private Job tail;

    public BufferWriteJob(SocketChannel channel) {
        tail = new Job(null);
        head = new Job(null);
        this.channel = channel;
    }

    // Returns true if this byteBuffer is added to an emty WriteJob.
    public boolean add(ByteBuffer byteBuffer) {
        boolean wasEmpty = false;
        if (head.next() == null) {
            wasEmpty = true;
            tail = head;
        }

        Job newJob = new Job(byteBuffer);
        tail.setNext(newJob);
        tail = newJob;
        return wasEmpty;
    }

    /**
     * @return True if all the byte buffers were written out, false otherwise.
     */
    public boolean run() throws WriteJobException {

        if (closed)
            throw new IllegalStateException();

        try {
            Job curJob;
            while ((curJob = head.next()) != null) {
                ByteBuffer buffer = curJob.getBuf();

                int bytesWritten;
                do {
                    bytesWritten = channel.write(buffer);
                } while (buffer.remaining() > 0 && bytesWritten > 0);

                // If the entire buffer wasn't written to the channel,
                // then stop writing since the channel must be busy.
                if (buffer.remaining() != 0) {
                    return false;
                }
                // Remove the buffer if it's done.
                else {
                    head.remove();
                }
            }
        } catch (IOException e) {
            throw new WriteJobException(e);
        }
        return true;
    }

    public void close() {
        closed = true;
    }

    public boolean isEmpty() {
        return head.next == null || head == tail;
    }

    static class Job {
        private final ByteBuffer buf;
        private Job next;

        Job(ByteBuffer buf) {
            this.buf = buf;
        }

        public void setNext(Job next) {
            this.next = next;
        }

        public void remove() {
            next = next.next;
        }

        public Job next() {
            return next;
        }

        public ByteBuffer getBuf() {
            return buf;
        }
    }
}