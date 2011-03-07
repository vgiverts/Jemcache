package org.jemcache.api;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Aug 27, 2008
 * Time: 2:25:46 PM
 */
public class StatResponse extends MemcachedMessage {

    private String name;
    private String value;

    public StatResponse() {
        super(Cmd.STAT);
    }

    public StatResponse(String name, String value) {
        super(Cmd.STAT);
        this.name = name;
        this.value = value;
    }

    public void parse(String args) {
        int i = args.indexOf(' ');
        name = args.substring(0, i);
        value = args.substring(i + 1);
    }

    public String toString() {
        return getType().toString() + ' ' + name + ' ' + value + "\r\n";
    }

    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof StatResponse)) return false;
        if (!super.equals(o)) return false;

        StatResponse that = (StatResponse) o;

        return name.equals(that.name) && value.equals(that.value);
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}