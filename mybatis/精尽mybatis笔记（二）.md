# 6 事务模块 #
MyBatis 对数据库中的事务进行了抽象，其自身提供了相应的事务接口和简单实现。
在很多场景中，MyBatis 会与 Spring 框架集成，并由 Spring 框架管理事务。

## 6.1 Transaction ##
org.apache.ibatis.transaction.Transaction ，事务接口。

````
public interface Transaction {
    /** 获得连接 */
    Connection getConnection() throws SQLException;
    /** 事务提交 */
    void commit() throws SQLException;
    /** 事务回滚 */
    void rollback() throws SQLException;
    /** 关闭连接 */
    void close() throws SQLException;
    /** 获得事务超时时间 */
    Integer getTimeout() throws SQLException;
}
````

连接相关
- getConnection() 方法，获得连接。
- close() 方法，关闭连接。

事务相关
- commit() 方法，事务提交。
- rollback() 方法，事务回滚。
- getTimeout() 方法，事务超时时间。实际上，目前这个方法都是空实现。

### 6.1.1 JdbcTransaction ###
org.apache.ibatis.transaction.jdbc.JdbcTransaction ，实现 Transaction 接口，基于 JDBC 的事务实现类。

### 6.1.2 ManagedTransaction ###
org.apache.ibatis.transaction.managed.ManagedTransaction ，实现 Transaction 接口，基于容器管理的事务实现类.

- 和 JdbcTransaction 相比，少了 autoCommit 属性，空实现 #commit() 和 #rollback() 方法。因此，事务的管理，交给了容器。

### 6.1.3 SpringManagedTransaction ###
org.mybatis.spring.transaction.SpringManagedTransaction ，实现 Transaction 接口，基于 Spring 管理的事务实现类。实际真正在使用的

## 6.2 TransactionFactory ##
org.apache.ibatis.transaction.TransactionFactory ，Transaction 工厂接口。

````
public interface TransactionFactory {
    /** 设置工厂的属性 */
    void setProperties(Properties props);
    /** 创建 Transaction 事务 */
    Transaction newTransaction(Connection conn);
    /** 创建 Transaction 事务 */
    Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit);
}
````
### 6.2.1 JdbcTransactionFactory ###
org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory ，实现 TransactionFactory 接口，JdbcTransaction 工厂实现类。

### 6.2.2 ManagedTransactionFactory ###
org.apache.ibatis.transaction.managed.ManagedTransactionFactory ，实现 TransactionFactory 接口，ManagedTransaction 工厂实现类。

----

# 7 缓存模块 #

在优化系统性能时，优化数据库性能是非常重要的一个环节，而添加缓存则是优化数据库时最有效的手段之一。正确、合理地使用缓存可以将一部分数据库请求拦截在缓存这一层。

MyBatis 中提供了一级缓存和二级缓存，而这两级缓存都是依赖于基础支持层中的缓 存模块实现的。这里需要读者注意的是，MyBatis 中自带的这两级缓存与 MyBatis 以及整个应用是运行在同一个 JVM 中的，共享同一块堆内存。如果这两级缓存中的数据量较大， 则可能影响系统中其他功能的运行，所以当需要缓存大量数据时，优先考虑使用 Redis、Memcache 等缓存产品。

## 7.1 Cache ##
org.apache.ibatis.cache.Cache ，缓存容器接口。注意，它是一个容器，有点类似 HashMap ，可以往其中添加各种缓存。

### 7.1.1 PerpetualCache ###
org.apache.ibatis.cache.impl.PerpetualCache ，实现 Cache 接口，永不过期的 Cache 实现类，基于 HashMap 实现类。

### 7.1.2 LoggingCache ###
org.apache.ibatis.cache.decorators.LoggingCache ，实现 Cache 接口，支持打印日志的 Cache 实现类。

### 7.1.3 BlockingCache ###
org.apache.ibatis.cache.decoratorsBlockingCache ，实现 Cache 接口，阻塞的 Cache 实现类。

这里的阻塞比较特殊，当线程去获取缓存值时，如果不存在，则会阻塞后续的其他线程去获取该缓存。
为什么这么有这样的设计呢？因为当线程 A 在获取不到缓存值时，一般会去设置对应的缓存值，这样就避免其他也需要该缓存的线程 B、C 等，重复添加缓存。

### 7.1.4 SynchronizedCache ###
org.apache.ibatis.cache.decorators.SynchronizedCache ，实现 Cache 接口，同步的 Cache 实现类。

- 相应的方法，添加了 synchronized 修饰符。

### 7.1.5 SerializedCache ###
org.apache.ibatis.cache.decorators.SerializedCache ，实现 Cache 接口，支持序列化值的 Cache 实现类。

- 实现结果的序列化和反序列化

### 7.1.6 ScheduledCache ###
org.apache.ibatis.cache.decorators.ScheduledCache ，实现 Cache 接口，定时清空整个容器的 Cache 实现类。

- 每次缓存操作时，都调用 #clearWhenStale() 方法，根据情况，是否清空全部缓存。默认一个小时清空一次。

### 7.1.7 FifoCache ###

org.apache.ibatis.cache.decorators.FifoCache ，实现 Cache 接口，基于先进先出的淘汰机制的 Cache 实现类。

- 默认1024长度的缓存队列。

缺点：
- 如果重复添加一个缓存，那么在 keyList 里会存储两个，占用了缓存上限的两个名额。
- 在移除指定缓存时，不会移除 keyList 里占用的一个名额。

### 7.1.8 LruCache ###
org.apache.ibatis.cache.decorators.LruCache ，实现 Cache 接口，基于最少使用的淘汰机制的 Cache 实现类。

- 利用LinkedHashMap特性，当参数accessOrder为true时，即会按照访问顺序排序，最近访问的放在最前，最早访问的放在后面

此实现类的作用是重写删除方法，当达到条件的时候，即缓存数量达到上限值（默认1024）的时候，返回key值，删除此node

### 7.1.9 WeakCache ###
org.apache.ibatis.cache.decorators.WeakCache ，实现 Cache 接口，基于 java.lang.ref.WeakReference 的 Cache 实现类。

### 7.1.10 SoftCache ###
org.apache.ibatis.cache.decorators.SoftCache ，实现 Cache 接口，基于 java.lang.ref.SoftReference 的 Cache 实现类。

## 7.2 CacheKey ##

----