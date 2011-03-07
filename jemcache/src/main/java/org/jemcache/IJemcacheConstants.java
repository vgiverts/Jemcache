package org.jemcache;

import org.jemcache.api.*;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 3, 2007
 * Time: 2:53:42 PM
 */
public interface IJemcacheConstants {

    // Messages
    String NUMBER_PARSE_FAILURE = "Failed to parse data into a number.";

    // Arguments
    String NOREPLY = "noreply";

    // Constants
    int MAX_OFFSET_TIME = 60 * 60 * 24 * 30; // # of seconds in 30 days

    // Categories of commands/responses
    public enum Category {

        // Commands
        Storage(StorageCommand.class),
        Retrieval(RetrievalCommand.class),
        Deletion(DeletionCommand.class),
        Incremental(IncrementalCommand.class),
        Status(StatusCommand.class),

        // Responses
        Error(StringResponse.class),
        Simple(SimpleResponse.class),
        Value(ValueResponse.class),
        SimpleValue(SimpleValueResponse.class),
        Stat(StatResponse.class),;

        private final Class<? extends MemcachedMessage> type;

        Category(Class<? extends MemcachedMessage> type) {
            this.type = type;
        }

        public Class<? extends MemcachedMessage> getType() {
            return type;
        }
    }

    // All memcached commands/responses
    public enum Cmd {

        //------------ Commands ------------

        // Storage commands
        set(Category.Storage),
        add(Category.Storage),                              
        replace(Category.Storage),
        append(Category.Storage),
        prepend(Category.Storage),
        cas(Category.Storage),

        // Retrieval commands
        get(Category.Retrieval),
        gets(Category.Retrieval),

        // Incremental commands
        decr(Category.Incremental),
        incr(Category.Incremental),

        // Deletion command
        delete(Category.Deletion),

        // Status command
        stat(Category.Status),

        //------------ Responses ------------

        // Simple response
        END(Category.Simple),
        STORED(Category.Simple),
        NOT_STORED(Category.Simple),
        EXISTS(Category.Simple),
        DELETED(Category.Simple),
        NOT_FOUND(Category.Simple),

        // Error response
        ERROR(Category.Simple),
        SERVER_ERROR(Category.Error),
        CLIENT_ERROR(Category.Error),

        // Stat response
        STAT(Category.Stat),

        // Value response
        VALUE(Category.Value),

        // Simple long value response
        SIMPLE_VALUE(Category.SimpleValue);

        private final Category category;

        Cmd(Category category) {
            this.category = category;
        }

        public Class<? extends MemcachedMessage> getType() {
            return category.getType();
        }

        public Category getCategory() {
            return category;
        }
    }
}
