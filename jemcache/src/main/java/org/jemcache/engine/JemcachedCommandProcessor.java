package org.jemcache.engine;

import org.jemcache.IJemcacheConstants;
import org.jemcache.api.*;
import org.jemcache.util.BlackHoleOutput;
import org.jemcache.util.IOutput;
import org.jemcache.util.MemcachedUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 3, 2007
 * Time: 10:03:55 AM
 */
public class JemcachedCommandProcessor implements ICommandProcessor {

    private static final int INITIAL_CAS = -1;
    private final AtomicLong casIdCounter = new AtomicLong(0);
    private final IStorageEngine storageEngine;

    public JemcachedCommandProcessor(IStorageEngine storageEngine) {
        this.storageEngine = storageEngine;
    }

    public void processMessage(MemcachedMessage message, IOutput<MemcachedMessage> out) {
        // Blackhole the output if requested
        if (message instanceof ModificationCommand && ((ModificationCommand) message).isNoreply()) {
            out = new BlackHoleOutput<MemcachedMessage>();
        }
        processMessageInternal(message, out);
    }

    private void processMessageInternal(MemcachedMessage message, IOutput<MemcachedMessage> out) {
        switch (message.getCategory()) {

            case Error:
                out.write(message);
                break;

            case Deletion:
                processDeletionCommand((DeletionCommand) message, out);
                break;

            case Incremental:
                processIncrementalCommand((IncrementalCommand) message, out);
                break;

            case Retrieval:
                processRetrievalCommand((RetrievalCommand) message, out);
                break;

            case Status:
                // todo
                break;

            case Storage:
                processStorageCommand((StorageCommand) message, out);
                break;
        }
    }

    private void processIncrementalCommand(IncrementalCommand incrementalCommand, IOutput<MemcachedMessage> out) {
        String key = incrementalCommand.getKey();
        do {
            IDataContainer oldContainer = storageEngine.get(key);
            if (oldContainer == null) {
                out.write(new SimpleResponse(IJemcacheConstants.Cmd.NOT_FOUND));
                return;
            } else {
                // Parse the current value
                long value;
                try {
                    String valueStr = new String(oldContainer.getData());
                    value = Long.parseLong(valueStr);
                } catch (NumberFormatException e) {
                    out.write(new StringResponse(IJemcacheConstants.Cmd.SERVER_ERROR, IJemcacheConstants.NUMBER_PARSE_FAILURE));
                    return;
                }

                // Increment the value
                if (incrementalCommand.getType() == IJemcacheConstants.Cmd.incr)
                    value += incrementalCommand.getValue();
                else
                    value -= incrementalCommand.getValue();
                String newValueStr = String.valueOf(value);

                // Replace the old value with the one. If we fail, then we will loop around and try again.
                if (storageEngine.replace(key, oldContainer, new DataContainer(newValueStr.getBytes(), oldContainer.getFlags(), oldContainer.getExpirationTime(), INITIAL_CAS))) {
                    out.write(new SimpleValueResponse(value));
                    return;
                }
            }
        } while (true);
    }

    private void processDeletionCommand(DeletionCommand deletionCommand, IOutput<MemcachedMessage> out) {
        if (storageEngine.remove(deletionCommand.getKey())) {
            out.write(new SimpleResponse(IJemcacheConstants.Cmd.NOT_FOUND));
        } else {
            out.write(new SimpleResponse(IJemcacheConstants.Cmd.DELETED));
        }
    }

    private void processRetrievalCommand(RetrievalCommand cmd, IOutput<MemcachedMessage> out) {
        List<String> keys = cmd.getKeys();
        boolean isGets = cmd.getType() == MemcachedMessage.Cmd.gets;
        for (String key : keys) {
            IDataContainer container = storageEngine.get(key);
            if (container != null) {
                ValueResponse response = new ValueResponse(key, container.getFlags(), container.getData(), false);
                if (isGets) {
                    // Set the cas id for this container.
                    long casID;
                    synchronized (container) {
                        casID = container.getCasID();
                        if (casID == -1) {
                            casID = this.casIdCounter.incrementAndGet();
                            container.setCasID(casID);
                        }
                    }
                    response.setCasID(casID);
                }
                out.write(response);
            }
        }
        out.write(new SimpleResponse(MemcachedMessage.Cmd.END));
    }

    private void processStorageCommand(StorageCommand cmd, IOutput<MemcachedMessage> out) {
        String key = cmd.getKey();
        byte[] data = cmd.getData();
        long flags = cmd.getFlags();
        long expInterval = cmd.getExpInterval();
        long expirationTime = expInterval == 0 ? 0 : System.currentTimeMillis() + expInterval;
        MemcachedMessage.Cmd response = MemcachedMessage.Cmd.NOT_STORED;
        IDataContainer newContainer = new DataContainer(data, flags, expirationTime, INITIAL_CAS);
        IDataContainer oldContainer;

        switch (cmd.getType()) {

            case set:
                storageEngine.put(key, newContainer);
                response = MemcachedMessage.Cmd.STORED;
                break;

            case add:
                if (storageEngine.putIfAbsent(key, newContainer))
                    response = MemcachedMessage.Cmd.STORED;
                break;

            case replace:
                if (storageEngine.replace(key, newContainer))
                    response = MemcachedMessage.Cmd.STORED;
                break;

            case append:
                do {
                    oldContainer = storageEngine.get(key);
                    if (oldContainer == null) {
                        break;
                    }
                    newContainer = new DataContainer(MemcachedUtil.concat(oldContainer.getData(), data), flags, oldContainer.getExpirationTime(), INITIAL_CAS);
                    if (storageEngine.replace(key, oldContainer, newContainer)) {
                        response = MemcachedMessage.Cmd.STORED;
                        break;
                    }
                } while (true);
                break;

            case prepend:
                do {
                    oldContainer = storageEngine.get(key);
                    if (oldContainer == null) {
                        break;
                    }
                    newContainer = new DataContainer(MemcachedUtil.concat(data, oldContainer.getData()), flags, oldContainer.getExpirationTime(), INITIAL_CAS);
                    if (storageEngine.replace(key, oldContainer, newContainer)) {
                        response = MemcachedMessage.Cmd.STORED;
                        break;
                    }
                } while (true);
                break;

            case cas:
                oldContainer = storageEngine.get(key);
                if (oldContainer != null) {
                    if (oldContainer.getCasID() != cmd.getCasID()) {
                        response = IJemcacheConstants.Cmd.EXISTS;
                    } else {
                        if (storageEngine.replace(key, oldContainer, newContainer)) {
                            response = MemcachedMessage.Cmd.STORED;
                        } else {
                            response = IJemcacheConstants.Cmd.EXISTS;
                        }
                    }
                    break;
                }
        }
        out.write(new SimpleResponse(response));
    }
}
