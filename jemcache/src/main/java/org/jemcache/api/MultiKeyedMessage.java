package org.jemcache.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 4, 2007
 * Time: 10:15:21 AM
 */
public abstract class MultiKeyedMessage extends MemcachedMessage {

    private List<String> keys;

    protected MultiKeyedMessage() {
        super();
        keys = new ArrayList<String>(2);
    }

    protected MultiKeyedMessage(Cmd cmd, List<String> keys) {
        super(cmd);
        this.keys = keys;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public void addKey(String key) {
        keys.add(key);
    }

    public String getKey() {
        if (keys.size() == 0)
            return null;
        else
            return keys.iterator().next();
    }

    public String getServerKey() {
        return keys.get(0);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MultiKeyedMessage)) return false;
        if (!super.equals(o)) return false;

        MultiKeyedMessage that = (MultiKeyedMessage) o;

        return !(keys != null ? !keys.equals(that.keys) : that.keys != null);

    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (keys != null ? keys.hashCode() : 0);
        return result;
    }

    protected void appendKeys(StringBuilder buf) {
        buf.append(getType()).append(' ');
        List<String> keys = getKeys();
        for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
            String key = iterator.next();
            buf.append(key);
            if (iterator.hasNext())
                buf.append(' ');
        }
    }

    public String toString() {
        StringBuilder buf = new StringBuilder(getType().toString());
        appendKeys(buf);
        buf.append("\r\n");
        return buf.toString();
    }
}