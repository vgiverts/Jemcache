package org.jemcache.server;

import org.jemcache.codec.IMemcachedMessageDecoder;
import org.jemcache.codec.IMemcachedMessageEncoder;
import org.jemcache.codec.MemcachedProtocolDecoder;
import org.jemcache.codec.MemcachedProtocolEncoder;
import org.jemcache.engine.*;
import org.jemcache.util.CumulativeByteBuffer;
import org.jemcache.util.SimpleByteBuffer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 7, 2007
 * Time: 12:34:44 PM
 * <p/>
 */
public class JemcacheNioServer implements IServerConstants {

    private final AtomicInteger numConnections = new AtomicInteger(0);
    private final IMemcachedMessageDecoder decoder = new CumulativeMessageDecoder(new MemcachedProtocolDecoder());
    private final IMemcachedMessageEncoder encoder = new MemcachedProtocolEncoder();

    private final ICommandProcessor commandProcessor;
    private final ThreadData[] threadDatas;

    private volatile boolean closed = false;
    private int bufferSize = 1024;
    private int curThreadID = 0;

    public JemcacheNioServer(final int port) throws IOException {
        this(port, DEFAULT_MEMORY_LIMIT, 1);
    }

    public JemcacheNioServer(int port, long memoryLimitBytes, int numThreads) throws IOException {
        this(numThreads, port, new LruStorageEngine(memoryLimitBytes));
    }

    public JemcacheNioServer(int numThreads, final int port, IStorageEngine storageEngine) throws IOException {
        this.commandProcessor = new JemcachedCommandProcessor(storageEngine);

        // Create the threads.
        threadDatas = new ThreadData[numThreads];
        for (int i = 0; i < numThreads; i++) {

            final int threadID = i;

            threadDatas[i] = new ThreadData(threadID);

            new Thread("SocketServer_Worker" + i) {
                public void run() {
                    try {
                        mainLoop(threadDatas[threadID]);
                    } catch (Throwable e) {
                        terminate(e);
                    }
                }
            }.start();

            new Thread("SocketServer_Acceptor") {
                public void run() {
                    try {
                        acceptLoop(port);
                    } catch (Throwable e) {
                        terminate(e);
                    }
                }
            }.start();
        }

    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    private void performRead(SocketSession session) {
        SocketChannel channel = session.getChannel();
        try {
            int bytesRead;
            ArrayList<ByteBuffer> buffers = null;
            ByteBuffer lastBuf = null;
            ByteBuffer buf = allocateByteBuffer();

            do {

                if (buf == null) {
                    buf = allocateByteBuffer();
                }

                bytesRead = channel.read(buf);
                if (bytesRead > 0) {
                    if (lastBuf == null) {
                        lastBuf = buf;
                    } else {
                        if (buffers == null) {
                            buffers = new ArrayList<ByteBuffer>(2);
                            buffers.add(lastBuf);
                        }
                        buffers.add(buf);
                    }
                    buf.flip();
                    buf = null;
                } else if (bytesRead == -1) {
                    closeSession(session);
                    return;
                }

            } while (bytesRead == bufferSize);

            if (buffers == null) {
                if (lastBuf != null)
                    session.write(new SimpleByteBuffer(lastBuf));
            } else {
                session.write(new CumulativeByteBuffer(buffers));
            }

            // If there is something to write, then do that now.
            if (!session.getWriteJob().isEmpty()) {
                session.getKey().interestOps(SelectionKey.OP_WRITE);
            }


        } catch (IOException e) {
            closeSession(session);
        }
    }

    private ByteBuffer allocateByteBuffer() {
        return ByteBuffer.allocate(bufferSize);
    }

    private int nextThreadID() {
        int nextThreadID = curThreadID;
        curThreadID = (curThreadID + 1) % threadDatas.length;
        return nextThreadID;
    }

    void closeSession(SocketSession session) {
        session.close();
        numConnections.decrementAndGet();
    }

    private void acceptLoop(int port) throws IOException, InterruptedException {
        ServerSocketChannel serverSocketChannel = getServerSocketChannel(port);
        while (!closed) {
            SocketChannel acceptedChannel = serverSocketChannel.accept();
            acceptedChannel.configureBlocking(false);
            int threadID = nextThreadID();
            ThreadData threadData = threadDatas[threadID];
            Selector s = threadData.selector;
            SocketSession session = new SocketSession(commandProcessor, new BufferWriteJob(acceptedChannel), decoder, encoder, acceptedChannel);
            SelectionKey key = acceptedChannel.register(s, SelectionKey.OP_READ, session);
            session.setKey(key);
            s.wakeup();
            numConnections.incrementAndGet();
        }
    }

    private INioHandler getHandler(final SocketSession session) {
        return new INioHandler() {
            public void handle() {
                if (session.getKey().interestOps() == SelectionKey.OP_READ)
                    performRead(session);
                else
                    performWrite(session);
            }
        };
    }

    private void terminate(Throwable e) {
        System.out.println("Unexpected exception. Stopping JemcacheNioServer.");
        e.printStackTrace();
        e.printStackTrace();
        System.exit(-1);
    }

    private ServerSocketChannel getServerSocketChannel(int port) throws IOException, InterruptedException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.setReuseAddress(true);

        // Try to bind twice
        for (int trys = 0; trys < 2; trys++) {
            try {
                serverSocket.bind(new InetSocketAddress(port), 50);
                break;
            } catch (IOException e) {
                System.out.println("IOException while binding " + this.getClass().getSimpleName() + ". Retrying...");
                //Retry on exception.
                Thread.sleep(3000);
            }
        }

        serverSocketChannel.configureBlocking(true);
        return serverSocketChannel;
    }


    private void mainLoop(ThreadData threadData) throws InterruptedException, IOException {

        Selector selector = threadData.selector;
        while (!closed) {
            try {
                // Wait until at least 1 key can be written or read
                while (selector.selectNow() == 0)
                    Thread.yield();

                Set<SelectionKey> keys = selector.selectedKeys();
                for (Iterator<SelectionKey> i = keys.iterator(); i.hasNext();) {
                    SelectionKey key = i.next();
                    i.remove();
                    SocketSession session = (SocketSession) key.attachment();
                    int readyOps = key.readyOps();
                    if ((readyOps & SelectionKey.OP_READ) > 0)
                        performRead(session);
                    else //if ((readyOps & SelectionKey.OP_WRITE) > 0)
                        performWrite(session);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void performWrite(SocketSession session) {
        try {
            if (session.getWriteJob().run())
                // If there is nothing left to write, then only do reads.
                session.getKey().interestOps(SelectionKey.OP_READ);
        } catch (WriteJobException e) {
            e.printStackTrace();
            System.out.println("Closing session due to WriteJobException.");
            closeSession(session);
        }
    }

    static class ThreadData {
        final int id;
        public Selector selector;

        public ThreadData(int id) throws IOException {
            this.id = id;
            selector = Selector.open();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = args.length >= 1 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        long memoryLimit = args.length >= 2 ? Long.parseLong(args[1]) * 1024 * 1024 : DEFAULT_MEMORY_LIMIT;
        int threads = args.length >= 3 ? Integer.parseInt(args[2]) : DEFAULT_THREADS;
        new JemcacheNioServer(port, memoryLimit, threads);
    }
}