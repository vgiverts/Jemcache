package org.jemcache.api;

import org.jemcache.util.MemcachedUtil;

import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 4, 2007
 * Time: 10:12:42 AM
 */
public class StorageCommand extends DataMessage {

    private long flags;
    private long expInterval;
    private Long casID;

    public StorageCommand() {
        super();
    }

    public StorageCommand(Cmd cmd, String key, long flags, long expInterval, Long casID, byte[] data, boolean noreply) {
        super(cmd, key, data, noreply);
        this.flags = flags;
        this.expInterval = expInterval;
        this.casID = casID;
    }

    public StorageCommand(Cmd cmd, String key, long flags, long expInterval, Long casID, ByteBuffer data, boolean noreply) {
        super(cmd, key, data, noreply);
        this.flags = flags;
        this.expInterval = expInterval;
        this.casID = casID;
    }


    public long getFlags() {
        return flags;
    }

    public long getExpInterval() {
        return expInterval;
    }

    public Long getCasID() {
        return casID;
    }

    public void setFlags(long flags) {
        this.flags = flags;
    }

    public void setExpInterval(long expInterval) {
        this.expInterval = expInterval;
    }

    public void setCasID(long casID) {
        this.casID = casID;
    }

    public void parse(String args) {
        int i = args.indexOf(' ');
        setKey(args.substring(0, i));
        int l = i + 1;
        i = args.indexOf(' ', l);
        flags = Integer.valueOf(args.substring(l, i));
        l = i + 1;
        i = args.indexOf(' ', l);
        expInterval = MemcachedUtil.parseTime(args.substring(l, i));
        l = i + 1;
        i = args.indexOf(' ', l);
        if (i == -1)
            setBytes(Integer.valueOf(args.substring(l)));
        else {
            setBytes(Integer.valueOf(args.substring(l, i)));
            l = i + 1;
            i = args.indexOf(' ', l);
            if (i != -1) {
                casID = Long.valueOf(args.substring(l, i));
                l = i + 1;
                if (l != args.length()) {
                    noreply = args.substring(l).equals("noreply");
                }
            } else if (l != args.length())
                casID = Long.valueOf(args.substring(l));
        }
    }

    public String toString() {
        return getType().toString() + ' ' + getKey() + ' ' + flags + ' ' + expInterval / 1000 + ' ' + getBytes() + getCasStr() + getNoReplyStr() + "\r\n";
    }

    private String getCasStr() {
        return (casID == null ? "" : (' ' + casID.toString()));
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StorageCommand)) return false;
        if (!super.equals(o)) return false;

        StorageCommand that = (StorageCommand) o;

        return expInterval == that.expInterval && flags == that.flags && !(casID != null ? !casID.equals(that.casID) : that.casID != null);

    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (flags ^ (flags >>> 32));
        result = 31 * result + (int) (expInterval ^ (expInterval >>> 32));
        result = 31 * result + (casID != null ? casID.hashCode() : 0);
        return result;
    }
}
