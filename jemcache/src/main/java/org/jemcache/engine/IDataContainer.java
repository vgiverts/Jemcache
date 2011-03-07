package org.jemcache.engine;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Giverts
 * Date: 1/4/11
 * Time: 2:25 PM
 */
public interface IDataContainer {
    byte[] getData();

    long getFlags();

    void setCasID(long casID);

    long getCasID();

    long getExpirationTime();

    boolean isExpired(long curTime);
}
