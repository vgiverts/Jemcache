Jemcache is a java implementation of a Memcached server. It's primary purpose is for embedding in unit tests. The performance actually is quite good, however, it does not scale as well as the c version of Memcached. 

All of the APIs are implemented as defined in the [http://code.sixapart.com/svn/memcached/trunk/server/doc/protocol.txt](protocol documentation) except for UDP and timed deletes. The near-term roadmap is to implement the missing features in the following order: timed deletes, UDP.


Launch Jemcache
----

Standalone server
`java -jar jemcache.jar [port [memory-in-mb]]`


Nio implementation
`java -classpath jemcache.jar org.jemcache.server.JemcacheNioServer [port [memory-in-mb [num-threads]]]`


Embed Jemcache
----

`JemcacheServer jemcacheServer = new JemcacheServer(port, memoryLimitBytes);`
or
`JemcacheNioServer jemcacheNioServer = new JemcacheNioServer(port, memoryLimitBytes, numThreads);`


Maven dependency
----

`
<repositories>
    ...
    <repository>
        <id>jemcache</id>
        <url>https://github.com/vgiverts/Jemcache/raw/master/repo</url>
    </repository>
    ...
</repositories>

<dependencies>
    ...
    <dependency>
        <groupId>org.jemcache</groupId>
        <artifactId>jemcache</artifactId>
        <version>0.1.6</version>
    </dependency>
    ...
</dependencies>
`

Credit
----

- The original development of this project was supported by [http://about-tagged.com/](Tagged). 
- More recent development has been supported by [http://www.lionside.com/](Lionside). 
- Ongoing development is supported by [http://marsfog.com](MarsFog)
