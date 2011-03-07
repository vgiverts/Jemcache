package org.jemcache.server;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 8, 2007
 * Time: 12:09:27 PM
 */
public class WriteJobException extends Exception {
    public WriteJobException() {
        super();
    }

    public WriteJobException(String message) {
        super(message);
    }

    public WriteJobException(String message, Throwable cause) {
        super(message, cause);
    }

    public WriteJobException(Throwable cause) {
        super(cause);
    }
}
