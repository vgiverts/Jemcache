package org.jemcache.server;

import org.jemcache.codec.IMemcachedMessageDecoder;
import org.jemcache.codec.IMemcachedMessageEncoder;
import org.jemcache.codec.MemcachedProtocolDecoder;
import org.jemcache.codec.MemcachedProtocolEncoder;
import org.jemcache.engine.*;
import org.jemcache.engine.IStorageEngine;
import org.jemcache.util.SimpleByteBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 7, 2007
 * Time: 12:34:44 PM
 * <p/>
 */
public class JemcacheServer implements IServerConstants {

    private final AtomicInteger numConnections = new AtomicInteger(0);
    private final ICommandProcessor commandProcessor;
    private final IMemcachedMessageDecoder decoder = new CumulativeMessageDecoder(new MemcachedProtocolDecoder());
    private final IMemcachedMessageEncoder encoder = new MemcachedProtocolEncoder();
    private final ServerSocket serverSocket;
    private volatile boolean closed = false;
    private boolean debug = false;

    public JemcacheServer(int port, long memoryLimitBytes) throws Exception {
        this(port, new LruStorageEngine(memoryLimitBytes));
    }

    public JemcacheServer(int port, IStorageEngine storageEngine) throws Exception {
        this.commandProcessor = new JemcachedCommandProcessor(storageEngine);
        this.serverSocket = new ServerSocket(port);
        startAcceptThread(0);
    }

    private void process(SocketSession session, Socket socket) {
        try {
            while (!closed) {


                InputStream in = socket.getInputStream();
                int a = in.available();
                byte[] bytes = new byte[Math.max(a, 128)];
                long start = System.currentTimeMillis();
                int r = in.read(bytes);
                long end = System.currentTimeMillis();
                
                if (debug) {
                    System.out.println("read time: " + (end - start) + "    bytes read: " + r);
                }

                start = System.currentTimeMillis();
                session.write(new SimpleByteBuffer(ByteBuffer.wrap(bytes, 0, r)));
                socket.getOutputStream().flush();
                end = System.currentTimeMillis();

                if (debug) {
                    System.out.println("process+write time: " + (end - start));
                }
            }
        } catch (IOException e) {
            closeSession(session);
        }
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    void closeSession(SocketSession session) {
        session.close();
        numConnections.decrementAndGet();
    }

    public void error(SocketSession session, Throwable e) {
        closeSession(session);
    }

    private void accept() throws IOException {

        Socket socket = serverSocket.accept();

        // Start another thread to do accepting.
        startAcceptThread(numConnections.incrementAndGet());

        final SocketSession session = new SocketSession(commandProcessor, new StreamWriteJob(socket.getOutputStream()), decoder, encoder, null);

        try {
            while (!closed) {
                process(session, socket);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    private void startAcceptThread(int id) {
        new Thread("Jemcache-" + id) {
            public void run() {
                try {
                    accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Exception in accept thread. Terminating server.");
                    System.exit(1);
                }
            }
        }.start();
    }

    public static void main(String[] args) throws Exception {
        int port = args.length >= 1 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        long memoryLimit = args.length >= 2 ? Long.parseLong(args[1]) * 1024 * 1024 : DEFAULT_MEMORY_LIMIT;
        new JemcacheServer(port, memoryLimit);
    }

    public void stop() {
        closed = true;
    }
}