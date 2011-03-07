package org.jemcache.api;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 4, 2007
 * Time: 10:36:11 AM
 */
public class StatusCommand extends MemcachedMessage {
    private String args;

    public StatusCommand() {
        this(null);
    }
    public StatusCommand(String args) {
        super(Cmd.stat);
        this.args = args;
    }

    public void parse(String args) {
        if (args.length() > 0)
            this.args = args;
    }

    public String getArgs() {
        return args;
    }

    public String toString() {
        return getType() + getArgsStr() + "\r\n";
    }

    public String getArgsStr() {
        return args == null ? "" : ' ' + args;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StatusCommand)) return false;
        if (!super.equals(o)) return false;

        StatusCommand that = (StatusCommand) o;

        return !(args != null ? !args.equals(that.args) : that.args != null);

    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (args != null ? args.hashCode() : 0);
        return result;
    }
}
