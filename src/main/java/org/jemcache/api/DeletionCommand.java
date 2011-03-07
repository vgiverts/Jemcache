package org.jemcache.api;

import org.jemcache.util.MemcachedUtil;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 4, 2007
 * Time: 10:27:19 AM
 */
public class DeletionCommand extends ModificationCommand {

    private Long time;

    public DeletionCommand() {
        super();
    }

    public DeletionCommand(String key, Long time, boolean noreply) {

        super(Cmd.delete, key, noreply);
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void parse(String args) {
        int i = args.indexOf(' ');
        if (i == -1)
            setKey(args);
        else {
            setKey(args.substring(0, i));
            if (i < args.length()) {
                i = i + 1;
                if (args.startsWith(NOREPLY, i))
                    noreply = true;
                else {
                    int l = args.indexOf(' ', i);
                    if (l == -1)
                        time = MemcachedUtil.parseTime(args.substring(i));
                    else
                        time = MemcachedUtil.parseTime(args.substring(i, l));
                    if (args.startsWith(NOREPLY, l + 1))
                        noreply = true;
                }
            }
        }
    }

    public String toString() {
        return getType().toString() + ' ' + getKey() + getTimeStr() + getNoReplyStr() + "\r\n";
    }

    private String getTimeStr() {
        return time == null ? "" : " " + time / 1000;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeletionCommand)) return false;
        if (!super.equals(o)) return false;

        DeletionCommand that = (DeletionCommand) o;

        return !(time != null ? !time.equals(that.time) : that.time != null);

    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (time != null ? time.hashCode() : 0);
        return result;
    }
}
