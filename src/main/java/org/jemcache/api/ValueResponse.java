package org.jemcache.api;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 5, 2007
 * Time: 2:59:25 PM
 */
public class ValueResponse extends DataMessage {
    private Long casUnique;
    private long flags;

    public ValueResponse() {
        super();
    }

    public ValueResponse(String key, long flags, byte[] data, boolean noreply) {
        super(Cmd.VALUE, key, data, noreply);
        this.flags = flags;
    }

    public void setCasID(Long casUnique) {
        this.casUnique = casUnique;
    }

    public Long getCasUnique() {
        return casUnique;
    }

    public long getFlags() {
        return flags;
    }

    public void parse(String args) {
        int i = args.indexOf(' ');
        setKey(args.substring(0, i));
        int l = i + 1;
        i = args.indexOf(' ', l);
        flags = Integer.valueOf(args.substring(l, i));
        l = i + 1;
        i = args.indexOf(' ', l);
        if (i == -1)
            setBytes(Integer.valueOf(args.substring(l)));
        else {
            setBytes(Integer.valueOf(args.substring(l, i)));
            l = i + 1;
            if (l != args.length()) {
                if (args.startsWith(NOREPLY, l))
                    noreply = true;
                else {
                    i = args.indexOf(' ', l);
                    if (i == -1)
                        setCasID(Long.valueOf(args.substring(l)));
                    else {
                        setCasID(Long.valueOf(args.substring(l, i)));
                        noreply = args.startsWith(NOREPLY, i);
                    }
                }
            }
        }
    }

    public String toString() {
        return getType().toString() + ' ' + getKey() + ' ' + flags + ' ' + getBytes() + getCasStr() + getNoReplyStr() + "\r\n";
    }

    private String getCasStr() {
        return (casUnique == null ? "" : ' ' + casUnique.toString());
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ValueResponse)) return false;
        if (!super.equals(o)) return false;

        ValueResponse that = (ValueResponse) o;

        return flags == that.flags && !(casUnique != null ? !casUnique.equals(that.casUnique) : that.casUnique != null);

    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (casUnique != null ? casUnique.hashCode() : 0);
        result = 31 * result + (int) (flags ^ (flags >>> 32));
        return result;
    }
}
