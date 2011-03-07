package org.jemcache;

import org.jemcache.api.*;
import org.jemcache.engine.*;
import org.jemcache.util.IOutput;
import org.jemcache.util.MemcachedUtil;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 5, 2007
 * Time: 3:33:08 PM
 */
public class TestMemcachedServer extends TestCase {
    ICommandProcessor server;
    final LinkedList<MemcachedMessage> received = new LinkedList<MemcachedMessage>();
    final String str1 = "myData1";
    final byte[] data1 = str1.getBytes();
    final String str2 = "myData2";
    final byte[] data2 = str2.getBytes();
    final String str3 = "myData3";
    final byte[] data3 = str3.getBytes();
    final byte[] data32 = MemcachedUtil.concat(data3, data2);
    final String key1 = "key1";
    final String key2 = "key2";
    final String key4 = "key4";
    final int expInterval = 100000;
    final int flags = 123;

    IOutput<MemcachedMessage> output = new IOutput<MemcachedMessage>() {
        public void write(MemcachedMessage message) {
            received.addFirst(message);
        }
    };

    public void test_Store_and_Retrieve_Concurrent() throws Exception {
        server = new JemcachedCommandProcessor(new ConcurrentStorageEngine());
        storeAndRetrieveTest(false);
    }

    public void test_Store_and_Retrieve_Concurrent_noreply() throws Exception {
        server = new JemcachedCommandProcessor(new ConcurrentStorageEngine());
        storeAndRetrieveTest(true);
    }

    public void test_Store_and_Retrieve_Simple() throws Exception {
        server = new JemcachedCommandProcessor(new SimpleStorageEngine());
        storeAndRetrieveTest(false);
    }

    public void test_Store_and_Retrieve_Lru() throws Exception {
        server = new JemcachedCommandProcessor(new LruStorageEngine(1024 * 1024));
        storeAndRetrieveTest(false);
    }

    private void storeAndRetrieveTest(boolean noreply) throws Exception {

        // Test set
        received.clear();
        server.processMessage(new StorageCommand(MemcachedMessage.Cmd.set, key1, flags, expInterval, null, data1, noreply), output);
        MemcachedMessage expected = new SimpleResponse(MemcachedMessage.Cmd.STORED);
        checkReply(expected, noreply, 0);
        server.processMessage(new RetrievalCommand(MemcachedMessage.Cmd.get, key1), output);
        assertEquals(str1, new String(((ValueResponse) received.get(1)).getData()));
        assertEquals(new SimpleResponse(MemcachedMessage.Cmd.END), received.get(0));

        // Test add (failure)
        received.clear();
        server.processMessage(new StorageCommand(MemcachedMessage.Cmd.add, key1, flags, expInterval, null, data2, noreply), output);
        checkReply(new SimpleResponse(MemcachedMessage.Cmd.NOT_STORED), noreply, 0);
        server.processMessage(new RetrievalCommand(MemcachedMessage.Cmd.get, key1), output);
        assertEquals(str1, new String(((ValueResponse) received.get(1)).getData()));
        assertEquals(new SimpleResponse(MemcachedMessage.Cmd.END), received.get(0));

        // Test add (succeed)
        received.clear();
        server.processMessage(new StorageCommand(MemcachedMessage.Cmd.add, key2, flags, expInterval, null, data2, noreply), output);
        checkReply(new SimpleResponse(MemcachedMessage.Cmd.STORED), noreply, 0);
        server.processMessage(new RetrievalCommand(MemcachedMessage.Cmd.get, key2), output);
        assertEquals(str2, new String(((ValueResponse) received.get(1)).getData()));
        assertEquals(new SimpleResponse(MemcachedMessage.Cmd.END), received.get(0));

        // Test replace (failure)
        received.clear();
        server.processMessage(new StorageCommand(MemcachedMessage.Cmd.replace, "key4", 123, 100000, null, data3, noreply), output);
        checkReply(new SimpleResponse(MemcachedMessage.Cmd.NOT_STORED), noreply, 0);
        server.processMessage(new RetrievalCommand(MemcachedMessage.Cmd.get, "key4"), output);
        assertEquals(new SimpleResponse(MemcachedMessage.Cmd.END), received.get(0));

        // Test replace (success)
        received.clear();
        server.processMessage(new StorageCommand(MemcachedMessage.Cmd.replace, key1, flags, expInterval, null, data3, noreply), output);
        checkReply(new SimpleResponse(MemcachedMessage.Cmd.STORED), noreply, 0);
        server.processMessage(new RetrievalCommand(MemcachedMessage.Cmd.get, key1), output);
        assertEquals(str3, new String(((ValueResponse) received.get(1)).getData()));
        assertEquals(new SimpleResponse(MemcachedMessage.Cmd.END), received.get(0));

        // Test append (success)
        received.clear();
        server.processMessage(new StorageCommand(MemcachedMessage.Cmd.append, key1, flags, expInterval, null, data2, noreply), output);
        checkReply(new SimpleResponse(MemcachedMessage.Cmd.STORED), noreply, 0);
        server.processMessage(new RetrievalCommand(MemcachedMessage.Cmd.get, key1), output);
        assertTrue(Arrays.equals(data32, ((ValueResponse) received.get(1)).getData()));
        assertEquals(new SimpleResponse(MemcachedMessage.Cmd.END), received.get(0));

        // Test append (failure)
        received.clear();
        server.processMessage(new StorageCommand(MemcachedMessage.Cmd.append, key4, flags, expInterval, null, data2, noreply), output);
        checkReply(new SimpleResponse(MemcachedMessage.Cmd.NOT_STORED), noreply, 0);
        server.processMessage(new RetrievalCommand(MemcachedMessage.Cmd.get, key4), output);
        assertEquals(new SimpleResponse(MemcachedMessage.Cmd.END), received.get(0));

        // Test prepend (success)
        received.clear();
        server.processMessage(new StorageCommand(MemcachedMessage.Cmd.prepend, key2, flags, expInterval, null, data3, noreply), output);
        checkReply(new SimpleResponse(MemcachedMessage.Cmd.STORED), noreply, 0);
        server.processMessage(new RetrievalCommand(MemcachedMessage.Cmd.get, key2), output);
        assertTrue(Arrays.equals(data32, ((ValueResponse) received.get(1)).getData()));
        assertEquals(new SimpleResponse(MemcachedMessage.Cmd.END), received.get(0));

        // Test prepend (failure)
        received.clear();
        server.processMessage(new StorageCommand(MemcachedMessage.Cmd.prepend, key4, flags, expInterval, null, data3, noreply), output);
        checkReply(new SimpleResponse(MemcachedMessage.Cmd.NOT_STORED), noreply, 0);
        server.processMessage(new RetrievalCommand(MemcachedMessage.Cmd.get, key4), output);
        assertEquals(new SimpleResponse(MemcachedMessage.Cmd.END), received.get(0));

        // Test cas (failure)
        received.clear();
        server.processMessage(new StorageCommand(MemcachedMessage.Cmd.cas, key1, flags, expInterval, 1234L, data1, noreply), output);
        checkReply(new SimpleResponse(MemcachedMessage.Cmd.EXISTS), noreply, 0);
        server.processMessage(new RetrievalCommand(MemcachedMessage.Cmd.get, key1), output);
        assertTrue(Arrays.equals(data32, ((ValueResponse) received.get(1)).getData()));
        assertEquals(new SimpleResponse(MemcachedMessage.Cmd.END), received.get(0));

        // Test cas (success)
        received.clear();
        server.processMessage(new RetrievalCommand(MemcachedMessage.Cmd.gets, key1), output);
        Long cas = ((ValueResponse) received.get(1)).getCasUnique();
        received.clear();
        server.processMessage(new StorageCommand(MemcachedMessage.Cmd.cas, key1, flags, expInterval, cas, data1, noreply), output);
        checkReply(new SimpleResponse(MemcachedMessage.Cmd.STORED), noreply, 0);
        server.processMessage(new RetrievalCommand(MemcachedMessage.Cmd.get, key1), output);
        assertEquals(str1, new String(((ValueResponse) received.get(1)).getData()));
        assertEquals(new SimpleResponse(MemcachedMessage.Cmd.END), received.get(0));

        // Test multi-get
        received.clear();
        server.processMessage(new RetrievalCommand(MemcachedMessage.Cmd.get, key1, key2), output);
        assertEquals(new SimpleResponse(MemcachedMessage.Cmd.END), received.get(0));
        assertTrue(Arrays.equals(data32, ((ValueResponse) received.get(1)).getData()));
        assertEquals(str1, new String(((ValueResponse) received.get(2)).getData()));

        // Test increment (success)
        received.clear();
        server.processMessage(new StorageCommand(MemcachedMessage.Cmd.set, key1, flags, expInterval, null, "123".getBytes(), noreply), output);
        checkReply(new SimpleResponse(MemcachedMessage.Cmd.STORED), noreply, 0);
        received.clear();
        server.processMessage(new IncrementalCommand(IJemcacheConstants.Cmd.incr, key1, 5, noreply), output);
        checkReply(new SimpleValueResponse(128), noreply, 0);
        server.processMessage(new RetrievalCommand(MemcachedMessage.Cmd.get, key1), output);
        assertEquals("128", new String(((ValueResponse) received.get(1)).getData()));
        assertEquals(new SimpleResponse(MemcachedMessage.Cmd.END), received.get(0));

        // Test increment (failure - not a number)
        received.clear();
        server.processMessage(new StorageCommand(MemcachedMessage.Cmd.set, key1, flags, expInterval, null, "abc".getBytes(), noreply), output);
        checkReply(new SimpleResponse(MemcachedMessage.Cmd.STORED), noreply, 0);
        received.clear();
        server.processMessage(new IncrementalCommand(IJemcacheConstants.Cmd.incr, key1, 5, noreply), output);
        checkReply(new StringResponse(IJemcacheConstants.Cmd.SERVER_ERROR, IJemcacheConstants.NUMBER_PARSE_FAILURE), noreply, 0);

        // Test increment (failure - doesn't exist)
        received.clear();
        server.processMessage(new IncrementalCommand(IJemcacheConstants.Cmd.incr, "unused-key", 5, noreply), output);
        checkReply(new SimpleResponse(IJemcacheConstants.Cmd.NOT_FOUND), noreply, 0);

        // Test decrement (success)
        received.clear();
        server.processMessage(new StorageCommand(MemcachedMessage.Cmd.set, key1, flags, expInterval, null, "10".getBytes(), noreply), output);
        checkReply(new SimpleResponse(MemcachedMessage.Cmd.STORED), noreply, 0);
        received.clear();
        server.processMessage(new IncrementalCommand(IJemcacheConstants.Cmd.decr, key1, 3, noreply), output);
        checkReply(new SimpleValueResponse(7), noreply, 0);
        server.processMessage(new RetrievalCommand(MemcachedMessage.Cmd.get, key1), output);
        assertEquals("7", new String(((ValueResponse) received.get(1)).getData()));
        assertEquals(new SimpleResponse(MemcachedMessage.Cmd.END), received.get(0));
    }

    private void checkReply(MemcachedMessage expected, boolean noreply, int idx) {
        if (noreply) {
            assertEquals(0, received.size());
        } else {
            assertEquals(expected, received.get(idx));
        }
    }
}
