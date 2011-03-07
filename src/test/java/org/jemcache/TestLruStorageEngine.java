package org.jemcache;

import junit.framework.TestCase;
import org.jemcache.engine.DataContainer;
import org.jemcache.engine.LruStorageEngine;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: 1/5/11
 * Time: 1:23 PM
 */
public class TestLruStorageEngine extends TestCase {

    static int MEMORY_LIMIT = 2;
    LruStorageEngine lru;
    int numEntries = 256;
    String[] keys = new String[numEntries];
    DataContainer[] values = new DataContainer[numEntries];
    long curTime;
    protected void setUp() throws Exception {
        curTime = 0;
        lru = new LruStorageEngine(MEMORY_LIMIT) {
            protected long getTime() {
                return curTime;
            }
        };
        for (int i = 0; i < numEntries; i++) {
            keys[i] = String.valueOf(i);
            values[i] = new DataContainer(new byte[]{(byte) i}, 0, 5, 0);
        }
    }

    public void testLruRemoval() throws Exception {

        lru.put(keys[0], values[0]);
        lru.put(keys[1], values[1]);

        // Check that the initial values are present
        assertEquals(values[0], lru.get(keys[0]));
        assertEquals(values[1], lru.get(keys[1]));

        // Add 1 more that will bring the cache over its limit
        lru.put(keys[MEMORY_LIMIT], values[MEMORY_LIMIT]);

        // Check that the first entry is missing because it was least recently used
        assertEquals(null, lru.get(keys[0]));

        // Check that the remaining entries are still present
        assertEquals(values[1], lru.get(keys[1]));
        assertEquals(values[2], lru.get(keys[2]));
    }

    public void testKeyAccess() throws Exception {
        lru.put(keys[0], values[0]);
        lru.put(keys[1], values[1]);

        // Check that the initial values are present
        assertEquals(values[1], lru.get(keys[1]));
        assertEquals(values[0], lru.get(keys[0]));

        // Add 1 more that will bring the cache over its limit
        lru.put(keys[2], values[2]);

        // Check that the second entry is missing because it was least recently used
        assertEquals(null, lru.get(keys[1]));

        // Check that the remaining entries are still present
        assertEquals(values[0], lru.get(keys[0]));
        assertEquals(values[2], lru.get(keys[2]));
    }

    public void testDelete() throws Exception {
        lru.put(keys[0], values[0]);
        lru.remove(keys[0]);
        assertNull(lru.get(keys[0]));
    }

    public void testReplace() throws Exception {
        assertFalse(lru.replace(keys[0], values[0]));

        lru.put(keys[0], values[0]);
        assertEquals(values[0], lru.get(keys[0]));

        assertTrue(lru.replace(keys[0], values[1]));
        assertEquals(values[1],lru.get(keys[0]));
    }

    public void testReplaceExact() throws Exception {
        assertFalse(lru.replace(keys[0], values[0], values[0]));
        assertNull(lru.get(keys[0]));

        lru.put(keys[0], values[0]);
        assertEquals(values[0], lru.get(keys[0]));

        assertFalse(lru.replace(keys[0], values[1], values[1]));
        assertEquals(values[0], lru.get(keys[0]));

        assertTrue(lru.replace(keys[0], values[0], values[1]));
        assertEquals(values[1],lru.get(keys[0]));
    }

    public void testPutIfAbsent() throws Exception {
        assertTrue(lru.putIfAbsent(keys[0], values[0]));
        assertEquals(values[0], lru.get(keys[0]));

        assertFalse(lru.putIfAbsent(keys[0], values[1]));
        assertEquals(values[0],lru.get(keys[0]));
    }

    public void testExpireGet() throws Exception {
        lru.put(keys[0], values[0]);
        assertEquals(values[0], lru.get(keys[0]));
        curTime = 10;
        assertNull(lru.get(keys[0]));
    }

    public void testExpireReplace() throws Exception {
        lru.put(keys[0], values[0]);
        assertEquals(values[0], lru.get(keys[0]));
        curTime = 10;
        assertFalse(lru.replace(keys[0], values[1]));
    }
}
