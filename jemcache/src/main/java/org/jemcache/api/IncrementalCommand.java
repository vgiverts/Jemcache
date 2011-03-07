package org.jemcache.api;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 4, 2007
 * Time: 10:31:04 AM
 */
public class IncrementalCommand extends ModificationCommand {

    private long value;

    public IncrementalCommand() {
        super();
    }

    public IncrementalCommand(Cmd cmd, String key, long value, boolean noreply) {
        super(cmd, key, noreply);
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public void parse(String args) {
        int i = args.indexOf(' ');
        setKey(args.substring(0,i));
        i = i+1;
        int l = args.indexOf(' ', i);
        if (l == -1)
            value = Long.valueOf(args.substring(i));
        else {
            value = Long.valueOf(args.substring(i, l));
            noreply = args.startsWith(NOREPLY, l+1);
        }
    }

    public String toString() {
        return getType().toString() + ' ' + getKey() + ' ' + value + getNoReplyStr() + "\r\n";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IncrementalCommand)) return false;
        if (!super.equals(o)) return false;

        IncrementalCommand that = (IncrementalCommand) o;

        return value == that.value;

    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (value ^ (value >>> 32));
        return result;
    }
}
