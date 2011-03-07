package org.jemcache.engine;

import java.util.Arrays;

/**
* Created by IntelliJ IDEA.
* User: Vladimir Giverts
* Date: 1/4/11
* Time: 2:24 PM
*/
public class DataContainer implements IDataContainer {
    private final byte[] data;
    private final long flags;
    private final long expirationTime;

    private long casID ;

    public DataContainer(byte[] data, long flags, long expirationTime, long casID) {
        this.data = data;
        this.casID = casID;
        this.flags = flags;
        this.expirationTime = expirationTime;
    }

    public byte[] getData() {
        return data;
    }

    public long getFlags() {
        return flags;
    }

    public void setCasID(long casID) {
        this.casID = casID;
    }


    public long getCasID() {
        return casID;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public boolean isExpired(long curTime) {
        return expirationTime != 0 && expirationTime - curTime < 0;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        DataContainer that = (DataContainer) o;
        return that != null && Arrays.equals(data, that.data);
    }

    public int hashCode() {
        return Arrays.hashCode(data);
    }
}
