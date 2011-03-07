package org.jemcache.api;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Aug 26, 2008
 * Time: 5:47:15 PM
 */
public class SimpleValueResponse extends MemcachedMessage {

    private long value;

    public SimpleValueResponse() {
        super(Cmd.SIMPLE_VALUE);
    }

    public SimpleValueResponse(long value) {
        super(Cmd.SIMPLE_VALUE);
        this.value = value;
    }

    public void parse(String args) {
        value = Long.parseLong(args);
    }

    public long getValue() {
        return value;
    }

    public String toString() {
        return value + "\r\n";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleValueResponse)) return false;
        if (!super.equals(o)) return false;

        SimpleValueResponse that = (SimpleValueResponse) o;

        return value == that.value;

    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (value ^ (value >>> 32));
        return result;
    }
}
