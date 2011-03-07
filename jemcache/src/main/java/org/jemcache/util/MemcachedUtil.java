package org.jemcache.util;

import org.jemcache.IJemcacheConstants;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: Dec 5, 2007
 * Time: 10:46:26 AM
 */
public class MemcachedUtil {

    public static long parseTime(String timeStr) {
        long time = Long.valueOf(timeStr);

        // 0 means don't expire, so preserve that value.
        if (time == 0)
            return 0;

        // Convert the time to milliseconds/
        time *= 1000;

        // If the time is not an offset, convert it to one.
        if (time > IJemcacheConstants.MAX_OFFSET_TIME) {
            time -= System.currentTimeMillis();

            // If the converted time became non-positive, make it 1ms so it times out right away.
            if (time <= 0)
                time = 1L;
        }

        return time;
    }

    public static byte[] concat(byte[] d1, byte[] d2) {
        byte[] newData = new byte[d1.length + d2.length];
        System.arraycopy(d1, 0, newData, 0, d1.length);
        System.arraycopy(d2, 0, newData, d1.length, d2.length);
        return newData;
    }

}
