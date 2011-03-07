package org.jemcache.engine;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 */
public class LruStorageEngine implements IStorageEngine {

    protected final ConcurrentHashMap<String, Entry> map = new ConcurrentHashMap<String, Entry>();
    protected final ConcurrentLinkedQueue<Entry> queue = new ConcurrentLinkedQueue<Entry>();
    private final AtomicBoolean lruRemovalPermit = new AtomicBoolean(false);
    private final long memoryLimit;
    private final AtomicLong curMemoryUsage = new AtomicLong(0);

    public LruStorageEngine(long memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean replace(String key, IDataContainer newContainer) {
        Entry newE = new Entry(newContainer, key);

        Entry oldE = map.get(key);

        if (isNotNullOrExpired(oldE) && map.replace(key, oldE, newE)) {
            remove(oldE);
            add(newE);
            return true;
        }
        return false;
    }

    public boolean putIfAbsent(String key, IDataContainer newContainer) {
        Entry e = new Entry(newContainer, key);
        Entry oldE = map.get(key);

        if (isNotNullOrExpired(oldE)) {
            return false;
        }

        if (map.putIfAbsent(key, e) == null) {
            add(e);
            return true;
        } else {
            return false;
        }
    }

    public IDataContainer get(String key) {
        Entry e = map.get(key);

        if (isNotNullOrExpired(e)) {
            // Mark the entry as accessed by re-adding it to the lru queue
            e.recordAccess(queue);
            return e.value;
        }

        return null;
    }

    public boolean replace(String key, IDataContainer oldContainer, IDataContainer newContainer) {
        Entry newE = new Entry(newContainer, key);
        Entry oldE = map.get(key);
        if (oldE != null && oldE.value.equals(oldContainer) && map.replace(key, oldE, newE)) {
            remove(oldE);
            add(newE);
            return true;
        }
        return false;

    }

    public void put(String key, IDataContainer container) {
        Entry e = new Entry(container, key);
        Entry oldE = map.put(key, e);
        if (oldE != null) {
            remove(oldE);
        }
        add(e);
    }

    public boolean remove(String key) {
        Entry e = map.remove(key);
        if (e != null) {
            remove(e);
            return true;
        }
        return false;
    }

    private boolean isNotNullOrExpired(Entry e) {
        return e != null && !isExpired(e);
    }

    private boolean isExpired(Entry e) {
        if (e.value.isExpired(getTime())) {
            if (map.remove(e.key, e)) {
                remove(e);
            }
            return true;
        }
        return false;
    }

    /**
     * This method is protected for testing purposes.
     *
     * @return The current time
     */
    protected long getTime() {
        return System.currentTimeMillis();
    }

    private void remove(Entry e) {
        e.remove();
        curMemoryUsage.addAndGet(-e.value.getData().length);
    }

    private void add(Entry e) {
        e.recordAccess(queue);
        long curMem = curMemoryUsage.addAndGet(e.value.getData().length);

        // Check if we went over our memory limit.
        // Set to obtain the removal permit. This ensures that no more than 1 thread attempts to remove entries at any given time.
        if (curMem > memoryLimit && lruRemovalPermit.compareAndSet(false, true)) {
            try {
                // Remove enough entries to bring us under the limit again.
                while (curMem > memoryLimit) {
                    Entry removedE = queue.poll();
                    if (map.remove(removedE.key, removedE)) {
                        curMem = curMemoryUsage.addAndGet(-e.value.getData().length);
                    }
                }
            } finally {
                lruRemovalPermit.set(false);
            }
        }
    }


    public static class Entry {

        private static final AtomicReferenceFieldUpdater<Entry, ConcurrentLinkedQueue.Node> nodeSetter =
                AtomicReferenceFieldUpdater.newUpdater(Entry.class, ConcurrentLinkedQueue.Node.class, "node");
        protected final IDataContainer value;
        protected final String key;
        private volatile ConcurrentLinkedQueue.Node<Entry> node = null;

        public Entry(IDataContainer value, String key) {
            this.value = value;
            this.key = key;
        }

        public void recordAccess(ConcurrentLinkedQueue<Entry> queue) {
            queue.addNode(newNode());
        }

        private ConcurrentLinkedQueue.Node<Entry> newNode() {
            ConcurrentLinkedQueue.Node<Entry> newNode = new ConcurrentLinkedQueue.Node<Entry>(this);
            while (true) {
                // Replace the old node atomically to make sure that this Entry only has 1 node that has a non-null item.
                ConcurrentLinkedQueue.Node<Entry> oldNode = this.node;
                if (nodeSetter.compareAndSet(this, oldNode, newNode)) {
                    // Null out the node so that it would be remove in the calls to expireEntries().
                    if (oldNode != null)
                        oldNode.setItem(null);
                    return newNode;
                }
            }
        }

        private void remove() {
            node.setItem(null);
        }
    }
}
