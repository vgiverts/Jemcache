package org.jemcache.api;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Aug 27, 2008
 * Time: 2:25:46 PM
 */
public class StringResponse extends MemcachedMessage {

    private String value;

    public StringResponse() {
        super();
    }

    public StringResponse(Cmd cmd, String value) {
        super(cmd);
        this.value = value;
    }

    public void parse(String args) {
        value = args;
    }

    public String toString() {
        return getType().toString() + ' ' + value + "\r\n";
    }

    public String getValue() {
        return value;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StringResponse)) return false;
        if (!super.equals(o)) return false;

        StringResponse that = (StringResponse) o;

        return value.equals(that.value);

    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}
