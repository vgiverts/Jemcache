package org.jemcache.api;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 4, 2007
 * Time: 10:24:00 AM
 */
public class RetrievalCommand extends MultiKeyedMessage {

    public RetrievalCommand() {
        super();
    }

    public RetrievalCommand(Cmd cmd, String... keys) {
        super(cmd, Arrays.asList(keys));
    }

    public RetrievalCommand(Cmd cmd, List<String> keys) {
        super(cmd, keys);
    }

    public void parse(String args) {
        int i = args.indexOf(' ');

        if (i == -1) {
            addKey(args);
        } else {
            int l = 0;
            while (l != args.length()) {
                if (i == -1) {
                    addKey(args.substring(l));
                    break;
                } else {
                    addKey(args.substring(l, i));
                    l = i + 1;
                    i = args.indexOf(' ', l);
                }
            }
        }
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        appendKeys(buf);
        buf.append("\r\n");
        return buf.toString();
    }

}
