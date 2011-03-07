package org.jemcache.api;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Aug 26, 2008
 * Time: 4:06:26 PM
 */
public abstract class ModificationCommand extends KeyedMessage {

    protected static final byte[] NOREPLY_BYTES = (" " + NOREPLY + "\r\n").getBytes();
    protected boolean noreply;

    protected ModificationCommand() {
        super();
    }

    protected ModificationCommand(Cmd cmd, String key, boolean noreply) {
        super(cmd, key);
        this.noreply = noreply;
    }

    public boolean isNoreply() {
        return noreply;
    }

    protected String getNoReplyStr() {
        return noreply ? ' ' + NOREPLY : "";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModificationCommand)) return false;
        if (!super.equals(o)) return false;

        ModificationCommand that = (ModificationCommand) o;

        return noreply == that.noreply;

    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (noreply ? 1 : 0);
        return result;
    }
}
