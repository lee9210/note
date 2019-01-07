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






----