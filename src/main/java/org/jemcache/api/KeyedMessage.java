package org.jemcache.api;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 4, 2007
 * Time: 10:15:21 AM
 */
public abstract class KeyedMessage extends MemcachedMessage {

    private String key;

    protected KeyedMessage() {
        super();
    }

    protected KeyedMessage(Cmd cmd, String key) {
        super(cmd);
        this.key = key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getServerKey() {
        return key;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyedMessage)) return false;
        if (!super.equals(o)) return false;

        KeyedMessage that = (KeyedMessage) o;

        return !(key != null ? !key.equals(that.key) : that.key != null);

    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (key != null ? key.hashCode() : 0);
        return result;
    }


    public String toString() {
        return  getType().toString() + ' ' + key + "\r\n";
    }
}
