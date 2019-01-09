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
org.apache.ibatis.cache.CacheKey ，实现 Cloneable、Serializable 接口，缓存键。
因为 MyBatis 中的缓存键不是一个简单的 String ，而是通过多个对象组成。所以 CacheKey 可以理解成将多个对象放在一起，计算其缓存键。

### 7.2.1 构造方法 ###
- 当构造方法的方法参数为 Object[] objects 时，会调用 #updateAll(Object[] objects) 方法，更新相关属性。

### 7.2.2 updateAll ###
updateAll(Object[] objects) 方法，更新相关属性
````
public void updateAll(Object[] objects) {
    for (Object o : objects) {
        update(o);
    }
}
````
- 遍历 objects 数组，调用 #update(Object) 方法，更新相关属性。

````
public void update(Object object) {
    // 方法参数 object 的 hashcode
    int baseHashCode = object == null ? 1 : ArrayUtil.hashCode(object);

    count++;

    // checksum 为 baseHashCode 的求和
    checksum += baseHashCode;

    // 计算新的 hashcode 值
    baseHashCode *= count;
    hashcode = multiplier * hashcode + baseHashCode;

    // 添加 object 到 updateList 中
    updateList.add(object);
}
````

----

# 8 类型模块 #

1. MyBatis 为简化配置文件提供了别名机制，该机制是类型转换模块的主要功能之一。
2. 类型转换模块的另一个功能是实现 JDBC 类型与 Java 类型之间的转换，该功能在为 SQL 语句绑定实参以及映射查询结果集时都会涉及：
- 在为 SQL 语句绑定实参时，会将数据由 Java 类型转换成 JDBC 类型。
- 而在映射结果集时，会将数据由 JDBC 类型转换成 Java 类型。

## 8.1 TypeHandler ##
org.apache.ibatis.type.TypeHandler ，类型转换处理器。

一共有两类方法，分别是：
- setParameter(...) 方法，是 Java Type => JDBC Type 的过程。
- getResult(...) 方法，是 JDBC Type => Java Type 的过程。

流程图如下：

![](/picture/mybatis-typeHandler-flow.png)

- 左边是 #setParameter(...) 方法，是 Java Type => JDBC Type 的过程，从上往下看。
- 右边是 #getResult(...) 方法，是 JDBC Type => Java Type 的过程，从下往上看。

### 8.1.1 BaseTypeHandler ###
org.apache.ibatis.type.BaseTypeHandler ，实现 TypeHandler 接口，继承 TypeReference 抽象类，TypeHandler 基础抽象类。

#### 8.1.1.1 setParameter ####

setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) 方法

````
@Override
public void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
    // <1> 参数为空时，设置为 null 类型
    if (parameter == null) {
        if (jdbcType == null) {
            throw new TypeException("JDBC requires that the JdbcType must be specified for all nullable parameters.");
        }
        try {
            ps.setNull(i, jdbcType.TYPE_CODE);
        } catch (SQLException e) {
            throw new TypeException("Error setting null for parameter #" + i + " with JdbcType " + jdbcType + " . " +
                    "Try setting a different JdbcType for this parameter or a different jdbcTypeForNull configuration property. " +
                    "Cause: " + e, e);
        }
    // 参数非空时，设置对应的参数
    } else {
        try {
            setNonNullParameter(ps, i, parameter, jdbcType);
        } catch (Exception e) {
            throw new TypeException("Error setting non null for parameter #" + i + " with JdbcType " + jdbcType + " . " +
                    "Try setting a different JdbcType for this parameter or a different configuration property. " +
                    "Cause: " + e, e);
        }
    }
}
````

参数非空，调用 #setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) 抽象方法，设置对应的参数。


#### 8.1.1.2 getResult ####

````
@Override
public T getResult(ResultSet rs, String columnName) throws SQLException {
    try {
        return getNullableResult(rs, columnName);
    } catch (Exception e) {
        throw new ResultMapException("Error attempting to get column '" + columnName + "' from result set.  Cause: " + e, e);
    }
}

@Override
public T getResult(ResultSet rs, int columnIndex) throws SQLException {
    try {
        return getNullableResult(rs, columnIndex);
    } catch (Exception e) {
        throw new ResultMapException("Error attempting to get column #" + columnIndex + " from result set.  Cause: " + e, e);
    }
}

@Override
public T getResult(CallableStatement cs, int columnIndex) throws SQLException {
    try {
        return getNullableResult(cs, columnIndex);
    } catch (Exception e) {
        throw new ResultMapException("Error attempting to get column #" + columnIndex + " from callable statement.  Cause: " + e, e);
    }
}
````

### 8.1.2 子类 ###

TypeHandler 有非常多的子类，当然所有子类都是继承自 BaseTypeHandler 抽象类。

#### 8.1.2.1 IntegerTypeHandler ####
org.apache.ibatis.type.IntegerTypeHandler ，继承 BaseTypeHandler 抽象类，Integer 类型的 TypeHandler 实现类。

````
public class IntegerTypeHandler extends BaseTypeHandler<Integer> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Integer parameter, JdbcType jdbcType)
            throws SQLException {
        // 直接设置参数即可
        ps.setInt(i, parameter);
    }

    @Override
    public Integer getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        // 获得字段的值
        int result = rs.getInt(columnName);
        // 先通过 rs 判断是否空，如果是空，则返回 null ，否则返回 result
        return (result == 0 && rs.wasNull()) ? null : result;
    }

    @Override
    public Integer getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        // 获得字段的值
        int result = rs.getInt(columnIndex);
        // 先通过 rs 判断是否空，如果是空，则返回 null ，否则返回 result
        return (result == 0 && rs.wasNull()) ? null : result;
    }

    @Override
    public Integer getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        // 获得字段的值
        int result = cs.getInt(columnIndex);
        // 先通过 cs 判断是否空，如果是空，则返回 null ，否则返回 result
        return (result == 0 && cs.wasNull()) ? null : result;
    }
}
````

#### 8.1.2.2 DateTypeHandler ####
org.apache.ibatis.type.DateTypeHandler ，继承 BaseTypeHandler 抽象类，Date 类型的 TypeHandler 实现类

主要是java.util.Date 和 java.sql.Timestamp 的互相转换。

#### 8.1.2.3 DateOnlyTypeHandler ####
org.apache.ibatis.type.DateOnlyTypeHandler ，继承 BaseTypeHandler 抽象类，Date 类型的 TypeHandler 实现类。

主要是java.util.Date 和 java.sql.Date 的互相转换。
数据库里的时间有多种类型，以 MySQL 举例子，有 date、timestamp、datetime 三种类型。

#### 8.1.2.4 EnumTypeHandler ####
org.apache.ibatis.type.EnumTypeHandler ，继承 BaseTypeHandler 抽象类，Enum 类型的 TypeHandler 实现类。
主要是java.lang.Enum 和 java.util.String 的互相转换。
因为数据库不存在枚举类型，所以讲枚举类型持久化到数据库有两种方式，Enum.name <=> String 和 Enum.ordinal <=> int 。我们目前看到的 EnumTypeHandler 是前者，下面我们将看到的 EnumOrdinalTypeHandler 是后者。

#### 8.1.2.5 EnumOrdinalTypeHandler ####
org.apache.ibatis.type.EnumOrdinalTypeHandler ，继承 BaseTypeHandler 抽象类，Enum 类型的 TypeHandler 实现类。

**tips:**
EnumTypeHandler和EnumOrdinalTypeHandler的区别主要是数据库中存储字段的类型差别，由于EnumOrdinalTypeHandler使用枚举类型的ordinal作为存储，所以必须使用数字类型字段存储。

#### 8.1.2.6 ObjectTypeHandler ####
org.apache.ibatis.type.ObjectTypeHandler ，继承 BaseTypeHandler 抽象类，Object 类型的 TypeHandler 实现类。

#### 8.1.2.7 UnknownTypeHandler ####

org.apache.ibatis.type.UnknownTypeHandler ，继承 BaseTypeHandler 抽象类，未知的 TypeHandler 实现类。通过获取对应的 TypeHandler ，进行处理。

````
public class UnknownTypeHandler extends BaseTypeHandler<Object> {

    /**
     * ObjectTypeHandler 单例
     */
    private static final ObjectTypeHandler OBJECT_TYPE_HANDLER = new ObjectTypeHandler();

    /**
     * TypeHandler 注册表
     */
    private TypeHandlerRegistry typeHandlerRegistry;

    public UnknownTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {
        this.typeHandlerRegistry = typeHandlerRegistry;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
            throws SQLException {
        // 获得参数对应的处理器
        TypeHandler handler = resolveTypeHandler(parameter, jdbcType); // <1>
        // 使用 handler 设置参数
        handler.setParameter(ps, i, parameter, jdbcType);
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        // 获得参数对应的处理器
        TypeHandler<?> handler = resolveTypeHandler(rs, columnName); // <2>
        // 使用 handler 获得值
        return handler.getResult(rs, columnName);
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        // 获得参数对应的处理器
        TypeHandler<?> handler = resolveTypeHandler(rs.getMetaData(), columnIndex); // <3>
        // 如果找不到对应的处理器，使用 OBJECT_TYPE_HANDLER
        if (handler == null || handler instanceof UnknownTypeHandler) {
            handler = OBJECT_TYPE_HANDLER;
        }
        // 使用 handler 获得值
        return handler.getResult(rs, columnIndex);
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        return cs.getObject(columnIndex);
    }

    private TypeHandler<? extends Object> resolveTypeHandler(Object parameter, JdbcType jdbcType) { // <1>
        TypeHandler<? extends Object> handler;
        // 参数为空，返回 OBJECT_TYPE_HANDLER
        if (parameter == null) {
            handler = OBJECT_TYPE_HANDLER;
        // 参数非空，使用参数类型获得对应的 TypeHandler
        } else {
            handler = typeHandlerRegistry.getTypeHandler(parameter.getClass(), jdbcType);
            // check if handler is null (issue #270)
            // 获取不到，则使用 OBJECT_TYPE_HANDLER
            if (handler == null || handler instanceof UnknownTypeHandler) {
                handler = OBJECT_TYPE_HANDLER;
            }
        }
        return handler;
    }

    private TypeHandler<?> resolveTypeHandler(ResultSet rs, String column) {
        try {
            // 获得 columnIndex
            Map<String, Integer> columnIndexLookup = new HashMap<>();
            ResultSetMetaData rsmd = rs.getMetaData(); // 通过 metaData
            int count = rsmd.getColumnCount();
            for (int i = 1; i <= count; i++) {
                String name = rsmd.getColumnName(i);
                columnIndexLookup.put(name, i);
            }
            Integer columnIndex = columnIndexLookup.get(column);
            TypeHandler<?> handler = null;
            // 首先，通过 columnIndex 获得 TypeHandler
            if (columnIndex != null) {
                handler = resolveTypeHandler(rsmd, columnIndex); // <3>
            }
            // 获得不到，使用 OBJECT_TYPE_HANDLER
            if (handler == null || handler instanceof UnknownTypeHandler) {
                handler = OBJECT_TYPE_HANDLER;
            }
            return handler;
        } catch (SQLException e) {
            throw new TypeException("Error determining JDBC type for column " + column + ".  Cause: " + e, e);
        }
    }

    private TypeHandler<?> resolveTypeHandler(ResultSetMetaData rsmd, Integer columnIndex) { // <3>
        TypeHandler<?> handler = null;
        // 获得 JDBC Type 类型
        JdbcType jdbcType = safeGetJdbcTypeForColumn(rsmd, columnIndex);
        // 获得 Java Type 类型
        Class<?> javaType = safeGetClassForColumn(rsmd, columnIndex);
        //获得对应的 TypeHandler 对象
        if (javaType != null && jdbcType != null) {
            handler = typeHandlerRegistry.getTypeHandler(javaType, jdbcType);
        } else if (javaType != null) {
            handler = typeHandlerRegistry.getTypeHandler(javaType);
        } else if (jdbcType != null) {
            handler = typeHandlerRegistry.getTypeHandler(jdbcType);
        }
        return handler;
    }

    private JdbcType safeGetJdbcTypeForColumn(ResultSetMetaData rsmd, Integer columnIndex) {
        try {
            // 从 ResultSetMetaData 中，获得字段类型
            // 获得 JDBC Type
            return JdbcType.forCode(rsmd.getColumnType(columnIndex));
        } catch (Exception e) {
            return null;
        }
    }

    private Class<?> safeGetClassForColumn(ResultSetMetaData rsmd, Integer columnIndex) {
        try {
            // 从 ResultSetMetaData 中，获得字段类型
            // 获得 Java Type
            return Resources.classForName(rsmd.getColumnClassName(columnIndex));
        } catch (Exception e) {
            return null;
        }
    }
    
}
````

## 8.2 TypeReference ##
org.apache.ibatis.type.TypeReference ，引用泛型抽象类。目的很简单，就是解析类上定义的泛型。

````
public abstract class TypeReference<T> {

    /**
     * 泛型
     */
    private final Type rawType;

    protected TypeReference() {
        rawType = getSuperclassTypeParameter(getClass());
    }

    Type getSuperclassTypeParameter(Class<?> clazz) {
        // 【1】从父类中获取 <T>
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass instanceof Class) {
            // 能满足这个条件的，例如 GenericTypeSupportedInHierarchiesTestCase.CustomStringTypeHandler 这个类
            // try to climb up the hierarchy until meet something useful
            if (TypeReference.class != genericSuperclass) { // 排除 TypeReference 类
                return getSuperclassTypeParameter(clazz.getSuperclass());
            }

            throw new TypeException("'" + getClass() + "' extends TypeReference but misses the type parameter. "
                    + "Remove the extension or add a type parameter to it.");
        }

        // 【2】获取 <T>
        Type rawType = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
        // TODO remove this when Reflector is fixed to return Types
        // 必须是泛型，才获取 <T>
        if (rawType instanceof ParameterizedType) {
            rawType = ((ParameterizedType) rawType).getRawType();
        }

        return rawType;
    }

    public final Type getRawType() {
        return rawType;
    }

    @Override
    public String toString() {
        return rawType.toString();
    }
}
````
举个例子，IntegerTypeHandler 解析后的结果 rawType 为 Integer 。

## 8.3 注解 ##
type 包中，定义了三个注解

### 8.3.1 @MappedTypes ###

org.apache.ibatis.type.@MappedTypes ，匹配的 Java Type 类型的注解。

### 8.3.2 @MappedJdbcTypes ###
org.apache.ibatis.type.@MappedJdbcTypes ，匹配的 JDBC Type 类型的注解。

### 8.3.3 Alias ###
org.apache.ibatis.type.@Alias ，别名的注解

## 8.4 JdbcType ##
org.apache.ibatis.type.JdbcType ，Jdbc Type 枚举。

````
public enum JdbcType {

    /*
     * This is added to enable basic support for the
     * ARRAY data type - but a custom type handler is still required
     */
    ARRAY(Types.ARRAY),
    BIT(Types.BIT),
    TINYINT(Types.TINYINT),
    SMALLINT(Types.SMALLINT),
    INTEGER(Types.INTEGER),
    BIGINT(Types.BIGINT),
    FLOAT(Types.FLOAT),
    REAL(Types.REAL),
    DOUBLE(Types.DOUBLE),
    NUMERIC(Types.NUMERIC),
    DECIMAL(Types.DECIMAL),
    CHAR(Types.CHAR),
    VARCHAR(Types.VARCHAR),
    LONGVARCHAR(Types.LONGVARCHAR),
    DATE(Types.DATE),
    TIME(Types.TIME),
    TIMESTAMP(Types.TIMESTAMP),
    BINARY(Types.BINARY),
    VARBINARY(Types.VARBINARY),
    LONGVARBINARY(Types.LONGVARBINARY),
    NULL(Types.NULL),
    OTHER(Types.OTHER),
    BLOB(Types.BLOB),
    CLOB(Types.CLOB),
    BOOLEAN(Types.BOOLEAN),
    CURSOR(-10), // Oracle
    UNDEFINED(Integer.MIN_VALUE + 1000),
    NVARCHAR(Types.NVARCHAR), // JDK6
    NCHAR(Types.NCHAR), // JDK6
    NCLOB(Types.NCLOB), // JDK6
    STRUCT(Types.STRUCT),
    JAVA_OBJECT(Types.JAVA_OBJECT),
    DISTINCT(Types.DISTINCT),
    REF(Types.REF),
    DATALINK(Types.DATALINK),
    ROWID(Types.ROWID), // JDK6
    LONGNVARCHAR(Types.LONGNVARCHAR), // JDK6
    SQLXML(Types.SQLXML), // JDK6
    DATETIMEOFFSET(-155); // SQL Server 2008

    /**
     * 类型编号。嘿嘿，此处代码不规范
     */
    public final int TYPE_CODE;

    /**
     * 代码编号和 {@link JdbcType} 的映射
     */
    private static Map<Integer, JdbcType> codeLookup = new HashMap<>();

    static {
        // 初始化 codeLookup
        for (JdbcType type : JdbcType.values()) {
            codeLookup.put(type.TYPE_CODE, type);
        }
    }

    JdbcType(int code) {
        this.TYPE_CODE = code;
    }

    public static JdbcType forCode(int code) {
        return codeLookup.get(code);
    }

}
````

## 8.5 TypeHandlerRegistry ##
org.apache.ibatis.type.TypeHandlerRegistry ，TypeHandler 注册表，相当于管理 TypeHandler 的容器，从其中能获取到对应的 TypeHandler 。

### 8.5.1 构造方法 ###
````
/**
 * 空 TypeHandler 集合的标识，即使 {@link #TYPE_HANDLER_MAP} 中，某个 KEY1 对应的 Map<JdbcType, TypeHandler<?>> 为空。
 *
 * @see #getJdbcHandlerMap(Type)
 */
private static final Map<JdbcType, TypeHandler<?>> NULL_TYPE_HANDLER_MAP = Collections.emptyMap();

/**
 * JDBC Type 和 {@link TypeHandler} 的映射
 * 一个 JDBC Type 只对应一个 Java Type ，也就是一个 TypeHandler ，不同于 TYPE_HANDLER_MAP 属性。在 <2> 处，我们可以看到，我们可以看到，三个时间类型的 JdbcType 注册到 JDBC_TYPE_HANDLER_MAP 中。
那么可能会有胖友问，JDBC_TYPE_HANDLER_MAP 是一一映射，简单就可以获得 JDBC Type 对应的 TypeHandler ，而 TYPE_HANDLER_MAP 是一对多映射，一个 JavaType 怎么获取到对应的 TypeHandler 呢？继续往下看，答案在 #getTypeHandler(Type type, JdbcType jdbcType) 方法。
 */
private final Map<JdbcType, TypeHandler<?>> JDBC_TYPE_HANDLER_MAP = new EnumMap<>(JdbcType.class);
/**
 * {@link TypeHandler} 的映射
 * 一个 Java Type 可以对应多个 JDBC Type ，也就是多个 TypeHandler ，所以 Map 的第一层的值是 Map<JdbcType, TypeHandler<?> 。在 <1> 处，我们可以看到，Date 对应了多个 JDBC 的 TypeHandler 的注册。
当一个 Java Type 不存在对应的 JDBC Type 时，就使用 NULL_TYPE_HANDLER_MAP 静态属性，添加到 TYPE_HANDLER_MAP 中进行占位。
 */
private final Map<Type, Map<JdbcType, TypeHandler<?>>> TYPE_HANDLER_MAP = new ConcurrentHashMap<>();
/**
 * 所有 TypeHandler 的“集合”
 *
 * KEY：{@link TypeHandler#getClass()}
 * VALUE：{@link TypeHandler} 对象
 */
private final Map<Class<?>, TypeHandler<?>> ALL_TYPE_HANDLERS_MAP = new HashMap<>();

/**
 * {@link UnknownTypeHandler} 对象
 */
private final TypeHandler<Object> UNKNOWN_TYPE_HANDLER = new UnknownTypeHandler(this);
/**
 * 默认的枚举类型的 TypeHandler 对象
 */
private Class<? extends TypeHandler> defaultEnumTypeHandler = EnumTypeHandler.class;

public TypeHandlerRegistry() {
    // ... 省略其它类型的注册

    // <1>
    register(Date.class, new DateTypeHandler());
    register(Date.class, JdbcType.DATE, new DateOnlyTypeHandler());
    register(Date.class, JdbcType.TIME, new TimeOnlyTypeHandler());
    // <2>
    register(JdbcType.TIMESTAMP, new DateTypeHandler());
    register(JdbcType.DATE, new DateOnlyTypeHandler());
    register(JdbcType.TIME, new TimeOnlyTypeHandler());

    // ... 省略其它类型的注册
}
````

### 8.5.2 getInstance ###
getInstance(Class<?> javaTypeClass, Class<?> typeHandlerClass) 方法，创建 TypeHandler 对象。

### 8.5.3 register ###
register(...) 方法，注册 TypeHandler 。TypeHandlerRegistry 中有大量该方法的重载实现，大体整理如下：

![](/picture/mybatis-TypeHandlerRegistry-flow.png)

除了 ⑤ 以外，所有方法最终都会调用 ④ ，即 #register(Type javaType, JdbcType jdbcType, TypeHandler<?> handler) 方法

````
private void register(Type javaType, JdbcType jdbcType, TypeHandler<?> handler) {
    // <1> 添加 handler 到 TYPE_HANDLER_MAP 中
    if (javaType != null) {
        // 获得 Java Type 对应的 map
        Map<JdbcType, TypeHandler<?>> map = TYPE_HANDLER_MAP.get(javaType);
        if (map == null || map == NULL_TYPE_HANDLER_MAP) { // 如果不存在，则进行创建
            map = new HashMap<>();
            TYPE_HANDLER_MAP.put(javaType, map);
        }
        // 添加到 handler 中 map 中
        map.put(jdbcType, handler);
    }
    // <2> 添加 handler 到 ALL_TYPE_HANDLERS_MAP 中
    ALL_TYPE_HANDLERS_MAP.put(handler.getClass(), handler);
}
````

register(String packageName) 方法，扫描指定包下的所有 TypeHandler 类，并发起注册。

````
public void register(String packageName) {
    // 扫描指定包下的所有 TypeHandler 类
    ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
    resolverUtil.find(new ResolverUtil.IsA(TypeHandler.class), packageName);
    Set<Class<? extends Class<?>>> handlerSet = resolverUtil.getClasses();
    // 遍历 TypeHandler 数组，发起注册
    for (Class<?> type : handlerSet) {
        //Ignore inner classes and interfaces (including package-info.java) and abstract classes
        // 排除匿名类、接口、抽象类
        if (!type.isAnonymousClass() && !type.isInterface() && !Modifier.isAbstract(type.getModifiers())) {
            register(type);
        }
    }
}
````
此方法，会调用 ⑥ #register(Class<?> typeHandlerClass) 方法，注册指定 TypeHandler 类。

````
public void register(Class<?> typeHandlerClass) {
    boolean mappedTypeFound = false;
    // <3> 获得 @MappedTypes 注解
    MappedTypes mappedTypes = typeHandlerClass.getAnnotation(MappedTypes.class);
    if (mappedTypes != null) {
        // 遍历注解的 Java Type 数组，逐个进行注册
        for (Class<?> javaTypeClass : mappedTypes.value()) {
            register(javaTypeClass, typeHandlerClass);
            mappedTypeFound = true;
        }
    }
    // <4> 未使用 @MappedTypes 注解，则直接注册
    if (!mappedTypeFound) {
        register(getInstance(null, typeHandlerClass)); // 创建 TypeHandler 对象
    }
}
````
分成 <3> <4> 两种情况。

<3> 处，基于 @MappedTypes 注解，调用 #register(Class<?> javaTypeClass, Class<?> typeHandlerClass) 方法，注册指定 Java Type 的指定 TypeHandler 类。代码如下：
````
public void register(Class<?> javaTypeClass, Class<?> typeHandlerClass) {
    register(javaTypeClass, getInstance(javaTypeClass, typeHandlerClass)); // 创建 TypeHandler 对象
}
````
调用 ③ #register(Class<T> javaType, TypeHandler<? extends T> typeHandler) 方法，注册指定 Java Type 的指定 TypeHandler 对象。代码如下：

````
private <T> void register(Type javaType, TypeHandler<? extends T> typeHandler) {
    // 获得 MappedJdbcTypes 注解
    MappedJdbcTypes mappedJdbcTypes = typeHandler.getClass().getAnnotation(MappedJdbcTypes.class);
    if (mappedJdbcTypes != null) {
        // 遍历 MappedJdbcTypes 注册的 JDBC Type 进行注册
        for (JdbcType handledJdbcType : mappedJdbcTypes.value()) {
            register(javaType, handledJdbcType, typeHandler);
        }
        if (mappedJdbcTypes.includeNullJdbcType()) {
            // <5>
            register(javaType, null, typeHandler); // jdbcType = null
        }
    } else {
        // <5>
        register(javaType, null, typeHandler); // jdbcType = null
    }
}
````
- 有 @MappedJdbcTypes 注解的 ④ #register(Type javaType, JdbcType jdbcType, TypeHandler<?> handler) 方法，发起最终注册。
- 对于 <5> 处，发起注册时，jdbcType 参数为 null


<4> 处，调用 ② #register(TypeHandler<T> typeHandler) 方法，未使用 @MappedTypes 注解，调用 #register(TypeHandler<T> typeHandler) 方法，注册 TypeHandler 对象。代码如下：

````
public <T> void register(TypeHandler<T> typeHandler) {
    boolean mappedTypeFound = false;
    // <5> 获得 @MappedTypes 注解
    MappedTypes mappedTypes = typeHandler.getClass().getAnnotation(MappedTypes.class);
    // 优先，使用 @MappedTypes 注解的 Java Type 进行注册
    if (mappedTypes != null) {
        for (Class<?> handledType : mappedTypes.value()) {
            register(handledType, typeHandler);
            mappedTypeFound = true;
        }
    }
    // @since 3.1.0 - try to auto-discover the mapped type
    // <6> 其次，当 typeHandler 为 TypeReference 子类时，进行注册
    if (!mappedTypeFound && typeHandler instanceof TypeReference) {
        try {
            TypeReference<T> typeReference = (TypeReference<T>) typeHandler;
            register(typeReference.getRawType(), typeHandler); // Java Type 为 <T> 泛型
            mappedTypeFound = true;
        } catch (Throwable t) {
            // maybe users define the TypeReference with a different type and are not assignable, so just ignore it
        }
    }
    // <7> 最差，使用 Java Type 为 null 进行注册
    if (!mappedTypeFound) {
        register((Class<T>) null, typeHandler);
    }
}
````
分成三种情况，最终都是调用 #register(Type javaType, TypeHandler<? extends T> typeHandler) 方法，进行注册，也就是跳到 ③ 。

<5> 处，优先，有符合的 @MappedTypes 注解时，使用 @MappedTypes 注解的 Java Type 进行注册。
<6> 处，其次，当 typeHandler 为 TypeReference 子类时，使用 <T> 作为 Java Type 进行注册。
<7> 处，最差，使用 null 作为 Java Type 进行注册。但是，这种情况下，只会将 typeHandler 添加到 ALL_TYPE_HANDLERS_MAP 中。因为，实际上没有 Java Type 。

### 8.5.4 getTypeHandler ###
getTypeHandler(...) 方法，获得 TypeHandler 。

大体流程如下：

![](/picture/mybatis-getTypeHandler-flow.png)

最终会调用 ① 处的 #getTypeHandler(Type type, JdbcType jdbcType) 方法。

主要有三种调用情况：

#### 8.5.4.1 调用情况一：#getTypeHandler(Class<T> type) 方法 ####
````
public <T> TypeHandler<T> getTypeHandler(Class<T> type) {
    return getTypeHandler((Type) type, null);
}
````
#### 8.5.4.2 调用情况二：#getTypeHandler(Class<T> type, JdbcType jdbcType) 方法 ####
````
public <T> TypeHandler<T> getTypeHandler(Class<T> type, JdbcType jdbcType) {
    return getTypeHandler((Type) type, jdbcType);
}
````
将 type 转换成 Type 类型。
#### 8.5.4.3 调用情况三：#getTypeHandler(TypeReference<T> javaTypeReference, ...) 方法 ####

````
public <T> TypeHandler<T> getTypeHandler(TypeReference<T> javaTypeReference) {
    return getTypeHandler(javaTypeReference, null);
}

public <T> TypeHandler<T> getTypeHandler(TypeReference<T> javaTypeReference, JdbcType jdbcType) {
    return getTypeHandler(javaTypeReference.getRawType(), jdbcType);
}
````
使用 <T> 泛型作为 type 。


 ① 处的 #getTypeHandler(Type type, JdbcType jdbcType) 方法。代码如下：

````
private <T> TypeHandler<T> getTypeHandler(Type type, JdbcType jdbcType) {
    // 忽略 ParamMap 的情况
    if (ParamMap.class.equals(type)) {
        return null;
    }
    // <1> 获得 Java Type 对应的 TypeHandler 集合
    Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = getJdbcHandlerMap(type);
    TypeHandler<?> handler = null;
    if (jdbcHandlerMap != null) {
        // <2.1> 优先，使用 jdbcType 获取对应的 TypeHandler
        handler = jdbcHandlerMap.get(jdbcType);
        // <2.2> 其次，使用 null 获取对应的 TypeHandler ，可以认为是默认的 TypeHandler
        if (handler == null) {
            handler = jdbcHandlerMap.get(null);
        }
        // <2.3> 最差，从 TypeHandler 集合中选择一个唯一的 TypeHandler
        if (handler == null) {
            // #591
            handler = pickSoleHandler(jdbcHandlerMap);
        }
    }
    // type drives generics here
    return (TypeHandler<T>) handler;
}
````
- <1> 处，调用 #getJdbcHandlerMap(Type type) 方法，获得 Java Type 对应的 TypeHandler 集合。
- <2.1> 处，优先，使用 jdbcType 获取对应的 TypeHandler 。
- <2.2> 处，其次，使用 null 获取对应的 TypeHandler ，可以认为是默认的 TypeHandler 。这里是解决一个 Java Type 可能对应多个 TypeHandler 的方式之一。
- <2.3> 处，最差，调用 #pickSoleHandler(Map<JdbcType, TypeHandler<?>> jdbcHandlerMap) 方法，从 TypeHandler 集合中选择一个唯一的 TypeHandler 。代码如下：

````
private TypeHandler<?> pickSoleHandler(Map<JdbcType, TypeHandler<?>> jdbcHandlerMap) {
    TypeHandler<?> soleHandler = null;
    for (TypeHandler<?> handler : jdbcHandlerMap.values()) {
        // 选择一个
        if (soleHandler == null) {
            soleHandler = handler;
        // 如果还有，并且不同类，那么不好选择，所以返回 null
        } else if (!handler.getClass().equals(soleHandler.getClass())) {
            // More than one type handlers registered.
            return null;
        }
    }
    return soleHandler;
}
// 这段代码看起来比较绕，其实目的很清晰，就是选择第一个，并且不能有其它的不同类的处理器。
// 这里是解决一个 Java Type 可能对应多个 TypeHandler 的方式之一。
````
通过 <2.1> + <2.2> + <2.3> 三处，解决 Java Type 对应的 TypeHandler 集合。


````
private Map<JdbcType, TypeHandler<?>> getJdbcHandlerMap(Type type) {
    // <1.1> 获得 Java Type 对应的 TypeHandler 集合
    Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = TYPE_HANDLER_MAP.get(type);
    // <1.2> 如果为 NULL_TYPE_HANDLER_MAP ，意味着为空，直接返回
    if (NULL_TYPE_HANDLER_MAP.equals(jdbcHandlerMap)) {
        return null;
    }
    // <1.3> 如果找不到
    if (jdbcHandlerMap == null && type instanceof Class) {
        Class<?> clazz = (Class<?>) type;
        // 枚举类型
        if (clazz.isEnum()) {
            // 获得父类对应的 TypeHandler 集合
            jdbcHandlerMap = getJdbcHandlerMapForEnumInterfaces(clazz, clazz);
            // 如果找不到
            if (jdbcHandlerMap == null) {
                // 注册 defaultEnumTypeHandler ，并使用它
                register(clazz, getInstance(clazz, defaultEnumTypeHandler));
                // 返回结果
                return TYPE_HANDLER_MAP.get(clazz);
            }
        // 非枚举类型
        } else {
            // 获得父类对应的 TypeHandler 集合
            jdbcHandlerMap = getJdbcHandlerMapForSuperclass(clazz);
        }
    }
    // <1.4> 如果结果为空，设置为 NULL_TYPE_HANDLER_MAP ，提升查找速度，避免二次查找
    TYPE_HANDLER_MAP.put(type, jdbcHandlerMap == null ? NULL_TYPE_HANDLER_MAP : jdbcHandlerMap);
    // 返回结果
    return jdbcHandlerMap;
}
````
- <1.1> 处，获得 Java Type 对应的 TypeHandler 集合。
- <1.2> 处，如果为 NULL_TYPE_HANDLER_MAP ，意味着为空，直接返回。原因可见 <1.4> 处。
- <1.3> 处，找不到，则根据 type 是否为枚举类型，进行不同处理。
- <1.4> 处，如果结果为空，设置为 NULL_TYPE_HANDLER_MAP ，提升查找速度，避免二次查找。

**【非枚举】**
先调用 #getJdbcHandlerMapForEnumInterfaces(Class<?> clazz, Class<?> enumClazz) 方法， 获得父类对应的 TypeHandler 集合。代码如下：

````
private Map<JdbcType, TypeHandler<?>> getJdbcHandlerMapForEnumInterfaces(Class<?> clazz, Class<?> enumClazz) {
    // 遍历枚举类的所有接口
    for (Class<?> iface : clazz.getInterfaces()) {
        // 获得该接口对应的 jdbcHandlerMap 集合
        Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = TYPE_HANDLER_MAP.get(iface);
        // 为空，递归 getJdbcHandlerMapForEnumInterfaces 方法，继续从父类对应的 TypeHandler 集合
        if (jdbcHandlerMap == null) {
            jdbcHandlerMap = getJdbcHandlerMapForEnumInterfaces(iface, enumClazz);
        }
        // 如果找到，则从 jdbcHandlerMap 初始化中 newMap 中，并进行返回
        if (jdbcHandlerMap != null) {
            // Found a type handler regsiterd to a super interface
            HashMap<JdbcType, TypeHandler<?>> newMap = new HashMap<>();
            for (Entry<JdbcType, TypeHandler<?>> entry : jdbcHandlerMap.entrySet()) {
                // Create a type handler instance with enum type as a constructor arg
                newMap.put(entry.getKey(), getInstance(enumClazz, entry.getValue().getClass()));
            }
            return newMap;
        }
    }
    // 找不到，则返回 null
    return null;
}
````
找不到，则注册 defaultEnumTypeHandler ，并使用它。


**【非枚举】**
调用 #getJdbcHandlerMapForSuperclass(Class<?> clazz) 方法，获得父类对应的 TypeHandler 集合。代码如下：

````
private Map<JdbcType, TypeHandler<?>> getJdbcHandlerMapForSuperclass(Class<?> clazz) {
    // 获得父类
    Class<?> superclass = clazz.getSuperclass();
    // 不存在非 Object 的父类，返回 null
    if (superclass == null || Object.class.equals(superclass)) {
        return null;
    }
    // 获得父类对应的 TypeHandler 集合
    Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = TYPE_HANDLER_MAP.get(superclass);
    // 找到，则直接返回
    if (jdbcHandlerMap != null) {
        return jdbcHandlerMap;
    // 找不到，则递归 getJdbcHandlerMapForSuperclass 方法，继续获得父类对应的 TypeHandler 集合
    } else {
        return getJdbcHandlerMapForSuperclass(superclass);
    }
}
````

## 8.6 TypeAliasRegistry ##
org.apache.ibatis.type.TypeAliasRegistry ，类型与别名的注册表。通过别名，我们在 Mapper XML 中的 resultType 和 parameterType 属性，直接使用，而不用写全类名。

### 8.6.1 构造方法 ###

````
/**
 * 类型与别名的映射。
 */
private final Map<String, Class<?>> TYPE_ALIASES = new HashMap<>();

/**
 * 初始化默认的类型与别名
 *
 * 另外，在 {@link org.apache.ibatis.session.Configuration} 构造方法中，也有默认的注册
 */
public TypeAliasRegistry() {
    registerAlias("string", String.class);
    registerAlias("byte", Byte.class);
    // ... 省略其他注册调用
}
````
### 8.6.2 registerAlias ###
registerAlias(Class<?> type) 方法，注册指定类。

````
public void registerAlias(Class<?> type) {
    // <1> 默认为，简单类名
    String alias = type.getSimpleName();
    // <2> 如果有注解，使用注册上的名字
    Alias aliasAnnotation = type.getAnnotation(Alias.class);
    if (aliasAnnotation != null) {
        alias = aliasAnnotation.value();
    }
    // <3> 调用 #registerAlias(String alias, Class<?> value) 方法，注册类型与别名的注册表
    registerAlias(alias, type);
}
````
````
public void registerAlias(String alias, Class<?> value) {
    if (alias == null) {
        throw new TypeException("The parameter alias cannot be null");
    }
    // 将别名转换成**小写**。这样的话，无论我们在 Mapper XML 中，写 `String` 还是 `string` 甚至是 `STRING` ，都是对应的 String 类型。
    String key = alias.toLowerCase(Locale.ENGLISH);
    if (TYPE_ALIASES.containsKey(key) && TYPE_ALIASES.get(key) != null && !TYPE_ALIASES.get(key).equals(value)) { // <2> 如果已经注册，并且类型不一致，说明有冲突，抛出 TypeException 异常。
        throw new TypeException("The alias '" + alias + "' is already mapped to the value '" + TYPE_ALIASES.get(key).getName() + "'.");
    }
    // <3> 添加到 `TYPE_ALIASES` 中。
    TYPE_ALIASES.put(key, value);
}
````

### 8.6.3 registerAliases ###
registerAliases(String packageName, ...) 方法，扫描指定包下的所有类，并进行注册。

````
/**
 * 注册指定包下的别名与类的映射
 */
public void registerAliases(String packageName) {
    registerAliases(packageName, Object.class);
}

/**
 * 注册指定包下的别名与类的映射。另外，要求类必须是 {@param superType} 类型（包括子类）。
 * @param packageName 指定包
 * @param superType 指定父类
 */
public void registerAliases(String packageName, Class<?> superType) {
    // 获得指定包下的类门
    ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
    resolverUtil.find(new ResolverUtil.IsA(superType), packageName);
    Set<Class<? extends Class<?>>> typeSet = resolverUtil.getClasses();
    // 遍历，逐个注册类型与别名的注册表
    for (Class<?> type : typeSet) {
        if (!type.isAnonymousClass() // 排除匿名类
                && !type.isInterface()  // 排除接口
                && !type.isMemberClass()) { // 排除内部类
            registerAlias(type);
        }
    }
}

// #resolveAlias(String string) 方法，获得别名对应的类型。代码如下：

// TypeAliasRegistry.java

public <T> Class<T> resolveAlias(String string) {
    try {
        if (string == null) {
            return null;
        }
        // issue #748
        // <1> 转换成小写
        String key = string.toLowerCase(Locale.ENGLISH);
        Class<T> value;
        // <2.1> 首先，从 TYPE_ALIASES 中获取
        if (TYPE_ALIASES.containsKey(key)) {
            value = (Class<T>) TYPE_ALIASES.get(key);
        // <2.2> 其次，直接获得对应类
        } else {
            value = (Class<T>) Resources.classForName(string);
        }
        return value;
    } catch (ClassNotFoundException e) { // <2.3> 异常
        throw new TypeException("Could not resolve type alias '" + string + "'.  Cause: " + e, e);
    }
}

//<1> 处，将别名转换成小写。
//<2.1> 处，首先，从 TYPE_ALIASES 中获取对应的类型。
//<2.2> 处，其次，直接获取对应的类。所以，这个方法，同时处理了别名与全类名两种情况。
//<2.3> 处，最差，找不到对应的类，发生异常，抛出 TypeException 异常。
````

## 8.7 SimpleTypeRegistry ##
org.apache.ibatis.type.SimpleTypeRegistry ，简单类型注册表。

````
public class SimpleTypeRegistry {

    /**
     * 简单类型的集合
     */
    private static final Set<Class<?>> SIMPLE_TYPE_SET = new HashSet<>();

    // 初始化常用类到 SIMPLE_TYPE_SET 中
    static {
        SIMPLE_TYPE_SET.add(String.class);
        SIMPLE_TYPE_SET.add(Byte.class);
        SIMPLE_TYPE_SET.add(Short.class);
        SIMPLE_TYPE_SET.add(Character.class);
        SIMPLE_TYPE_SET.add(Integer.class);
        SIMPLE_TYPE_SET.add(Long.class);
        SIMPLE_TYPE_SET.add(Float.class);
        SIMPLE_TYPE_SET.add(Double.class);
        SIMPLE_TYPE_SET.add(Boolean.class);
        SIMPLE_TYPE_SET.add(Date.class);
        SIMPLE_TYPE_SET.add(Class.class);
        SIMPLE_TYPE_SET.add(BigInteger.class);
        SIMPLE_TYPE_SET.add(BigDecimal.class);
    }

    private SimpleTypeRegistry() {
        // Prevent Instantiation
    }

    /*
     * Tells us if the class passed in is a known common type
     */
    public static boolean isSimpleType(Class<?> clazz) {
        return SIMPLE_TYPE_SET.contains(clazz);
    }

}
````

## 8.8 ByteArrayUtils ##
org.apache.ibatis.type.ByteArrayUtils ，Byte 数组的工具类。

# 9 IO 模块 #
资源加载模块，主要是对类加载器进行封装，确定类加载器的使用顺序，并提供了加载类文件以及其他资源文件的功能 。

## 9.1 ClassLoaderWrapper ##

org.apache.ibatis.io.ClassLoaderWrapper ，ClassLoader 包装器。可使用多个 ClassLoader 加载对应的资源，直到有一成功后返回资源。

### 9.1.1 构造方法 ###

````
/**
 * 默认 ClassLoader 对象
 */
ClassLoader defaultClassLoader;
/**
 * 系统 ClassLoader 对象
 */
ClassLoader systemClassLoader;

ClassLoaderWrapper() {
    try {
        systemClassLoader = ClassLoader.getSystemClassLoader();
    } catch (SecurityException ignored) {
        // AccessControlException on Google App Engine
    }
}
````

- defaultClassLoader 属性，默认 ClassLoader 对象。目前不存在初始化该属性的构造方法。可通过 ClassLoaderWrapper.defaultClassLoader = xxx 的方式，进行设置。
- systemClassLoader 属性，系统 ClassLoader 对象。在构造方法中，已经初始化。

### 9.1.2 getClassLoaders ###
getClassLoaders(ClassLoader classLoader) 方法，获得 ClassLoader 数组。

````
ClassLoader[] getClassLoaders(ClassLoader classLoader) {
    return new ClassLoader[]{
            classLoader,
            defaultClassLoader,
            Thread.currentThread().getContextClassLoader(),
            getClass().getClassLoader(),
            systemClassLoader};
}
````

### 9.1.3 getResourceAsURL ###
getResourceAsURL(String resource, ...) 方法，获得指定资源的 URL 。

````
/**
 * Get a resource as a URL using the current class path
 * @param resource - the resource to locate
 * @return the resource or null
 */
public URL getResourceAsURL(String resource) {
    return getResourceAsURL(resource, getClassLoaders(null));
}
 
/**
 * Get a resource from the classpath, starting with a specific class loader
 * @param resource    - the resource to find
 * @param classLoader - the first classloader to try
 * @return the stream or null
 */
public URL getResourceAsURL(String resource, ClassLoader classLoader) {
    return getResourceAsURL(resource, getClassLoaders(classLoader));
}
````
- 先调用 #getClassLoaders(ClassLoader classLoader) 方法，获得 ClassLoader 数组。
- 再调用 #getResourceAsURL(String resource, ClassLoader[] classLoader) 方法，获得指定资源的 InputStream 。

### 9.1.4 getResourceAsStream ###
getResourceAsStream(String resource, ...) 方法，获得指定资源的 InputStream 对象。

````
/**
 * Get a resource from the classpath
 * @param resource - the resource to find
 * @return the stream or null
 */
public InputStream getResourceAsStream(String resource) {
    return getResourceAsStream(resource, getClassLoaders(null));
}

/**
 * Get a resource from the classpath, starting with a specific class loader
 * @param resource    - the resource to find
 * @param classLoader - the first class loader to try
 * @return the stream or null
 */
public InputStream getResourceAsStream(String resource, ClassLoader classLoader) {
    return getResourceAsStream(resource, getClassLoaders(classLoader));
}
````

- 先调用 #getClassLoaders(ClassLoader classLoader) 方法，获得 ClassLoader 数组。
- 再调用 #getResourceAsStream(String resource, ClassLoader[] classLoader) 方法，获得指定资源的 InputStream 。

### 9.1.5 classForName ###
classForName(String name, ...) 方法，获得指定类名对应的类

````
/**
 * Find a class on the classpath (or die trying)
 * @param name - the class to look for
 * @return - the class
 * @throws ClassNotFoundException Duh.
 */
public Class<?> classForName(String name) throws ClassNotFoundException {
    return classForName(name, getClassLoaders(null));
}

/**
 * Find a class on the classpath, starting with a specific classloader (or die trying)
 * @param name        - the class to look for
 * @param classLoader - the first classloader to try
 * @return - the class
 * @throws ClassNotFoundException Duh.
 */
public Class<?> classForName(String name, ClassLoader classLoader) throws ClassNotFoundException {
    return classForName(name, getClassLoaders(classLoader));
}
````
- 先调用 #getClassLoaders(ClassLoader classLoader) 方法，获得 ClassLoader 数组。
- 再调用 #classForName(String name, ClassLoader[] classLoader) 方法，获得指定类名对应的类。

## 9.2 Resources ##
org.apache.ibatis.io.Resources ，Resource 工具类。

### 9.2.1 构造方法 ###

````
/**
 * ClassLoaderWrapper 对象
 */
private static ClassLoaderWrapper classLoaderWrapper = new ClassLoaderWrapper();

/**
 * 字符集
 */
private static Charset charset;

Resources() {
}

public static void setDefaultClassLoader(ClassLoader defaultClassLoader) {defaultClassLoader
    classLoaderWrapper.defaultClassLoader = defaultClassLoader; // 修改 ClassLoaderWrapper.
}

public static void setCharset(Charset charset) {
    Resources.charset = charset;
}
````

### 9.2.2 getResource ###
基于 classLoaderWrapper 属性的封装。

#### 9.2.2.1 getResourceURL ####
getResourceURL(String resource) 静态方法，获得指定资源的 URL 。

#### 9.2.2.2 getResourceAsStream ####
getResourceAsStream(String resource) 静态方法，获得指定资源的 InputStream 。

#### 9.2.2.3 getResourceAsReader ####
getResourceAsReader(String resource) 静态方法，获得指定资源的 Reader 。

#### 9.2.2.4 getResourceAsFile ####
getResourceAsFile(String resource) 静态方法，获得指定资源的 File 。

#### 9.2.2.5 getResourceAsProperties ####

### 9.2.3 getUrl ###
#### 9.2.3.1 getUrlAsStream ####
getUrlAsStream(String urlString) 静态方法，获得指定 URL 。

#### 9.2.3.2 getUrlAsReader ####
getUrlAsReader(String urlString) 静态方法，指定 URL 的 Reader 。

#### 9.2.3.3 getUrlAsProperties ####
getUrlAsReader(String urlString) 静态方法，指定 URL 的 Properties 。

#### 9.2.3.4 classForName ####
classForName(String className) 静态方法，获得指定类名对应的类。

## 9.3 ResolverUtil ##
org.apache.ibatis.io.ResolverUtil ，解析器工具类，用于获得指定目录符合条件的类们。

### 9.3.1 Test ###
Test ，匹配判断接口。

#### 9.3.1.1 IsA ####
IsA ，实现 Test 接口，判断是否为指定类

#### 9.3.1.2 AnnotatedWith ####
AnnotatedWith ，判断是否有指定注解。

### 9.3.2 构造方法 ###
````
/** The set of matches being accumulated. */
private Set<Class<? extends T>> matches = new HashSet<>(); // 符合条件的类的集合

private ClassLoader classloader;

public Set<Class<? extends T>> getClasses() {
    return matches;
}

public ClassLoader getClassLoader() {
    return classloader == null ? Thread.currentThread().getContextClassLoader() : classloader;
}
public void setClassLoader(ClassLoader classloader) {
    this.classloader = classloader;
}
````

### 9.3.3 find ###
find(Test test, String packageName) 方法，获得指定包下，符合条件的类。

````
public ResolverUtil<T> find(Test test, String packageName) {
    // <1> 获得包的路径
    String path = getPackagePath(packageName);

    try {
        // <2> 获得路径下的所有文件
        List<String> children = VFS.getInstance().list(path);
        // <3> 遍历
        for (String child : children) {
            // 是 Java Class
            if (child.endsWith(".class")) {
                // 如果匹配，则添加到结果集
                addIfMatching(test, child);
            }
        }
    } catch (IOException ioe) {
        log.error("Could not read package: " + packageName, ioe);
    }

    return this;
} 
````

#### 9.3.3.1 findImplementations ####
findImplementations(Class<?> parent, String... packageNames) 方法，判断指定目录下们，符合指定类的类们。

#### 9.3.3.2 findAnnotated ####
findAnnotated(Class<? extends Annotation> annotation, String... packageNames) 方法，判断指定目录下们，符合指定注解的类们。

## 9.4 VFS ##
org.apache.ibatis.io.VFS ，虚拟文件系统( Virtual File System )抽象类，用来查找指定路径下的的文件们。

### 9.4.1 静态属性 ###

````
/** The built-in implementations. */
public static final Class<?>[] IMPLEMENTATIONS = {JBoss6VFS.class, DefaultVFS.class}; // 静态属性，内置的 VFS 实现类的数组。目前 VFS 有 JBoss6VFS 和 DefaultVFS 两个实现类。

/** The list to which implementations are added by {@link #addImplClass(Class)}. */
public static final List<Class<? extends VFS>> USER_IMPLEMENTATIONS = new ArrayList<>(); // 自定义的 VFS 实现类的数组。可通过 #addImplClass(Class<? extends VFS> clazz) 方法，进行添加。

public static void addImplClass(Class<? extends VFS> clazz) {
    if (clazz != null) {
        USER_IMPLEMENTATIONS.add(clazz);
    }
}
````

### 9.4.2 getInstance ###
getInstance() 方法，获得 VFS 单例。

单例有多种实现方式，该类采用的是“懒汉式，线程安全”。
INSTANCE 属性，最后通过 #createVFS() 静态方法来创建，虽然 USER_IMPLEMENTATIONS 和 IMPLEMENTATIONS 有多种 VFS 的实现类，但是最终选择的是，最后一个符合的创建的 VFS 对象。

### 9.4.3 反射相关方法 ###
因为 VFS 自己有反射调用方法的需求，所以自己实现了三个方法。

### 9.4.4 isValid ###
isValid() 抽象方法，判断是否为合法的 VFS 。

### 9.4.5 list ###
list(String path) 方法，获得指定路径下的所有资源。

````
public List<String> list(String path) throws IOException {
    List<String> names = new ArrayList<>();
    for (URL url : getResources(path)) {
        names.addAll(list(url, path));
    }
    return names;
}
````
先调用 #getResources(String path) 静态方法，获得指定路径下的 URL 数组
后遍历 URL 数组，调用 #list(URL url, String forPath) 方法，递归的列出所有的资源们。

### 9.4.6 DefaultVFS ###
org.apache.ibatis.io.DefaultVFS ，继承 VFS 抽象类，默认的 VFS 实现类。

#### 9.4.6.1 isValid ####
都返回 true ，因为默认支持。

#### 9.4.6.2 list ####
list(URL url, String path) 方法，递归的列出所有的资源们。

大体逻辑就是，不断递归文件夹，获得到所有文件。

### 9.4.7 JBoss6VFS ###

org.apache.ibatis.io.JBoss6VFS ，继承 VFS 抽象类，基于 JBoss 的 VFS 实现类。

----

# 10 日志模块 #

无论在开发测试环境中，还是在线上生产环境中，日志在整个系统中的地位都是非常重要的。良好的日志功能可以帮助开发人员和测试人员快速定位 Bug 代码，也可以帮助运维人员快速定位性能瓶颈等问题。目前的 Java 世界中存在很多优秀的日志框架，例如 Log4j、 Log4j2、Slf4j 等。

MyBatis 作为一个设计优良的框架，除了提供详细的日志输出信息，还要能够集成多种日志框架，其日志模块的一个主要功能就是集成第三方日志框架。


## 10.1 LogFactory ##
org.apache.ibatis.logging.LogFactory ，Log 工厂类。

### 10.1.1 构造方法 ###
````
/**
 * Marker to be used by logging implementations that support markers
 */
public static final String MARKER = "MYBATIS";

/**
 * 使用的 Log 的构造方法
 */
private static Constructor<? extends Log> logConstructor;

static {
    // 按照 Slf4j、CommonsLogging、Log4J2Logging、Log4JLogging、JdkLogging、NoLogging 的顺序，逐个尝试，判断使用哪个 Log 的实现类，即初始化 logConstructor 属性。
    tryImplementation(LogFactory::useSlf4jLogging);
    tryImplementation(LogFactory::useCommonsLogging);
    tryImplementation(LogFactory::useLog4J2Logging);
    tryImplementation(LogFactory::useLog4JLogging);
    tryImplementation(LogFactory::useJdkLogging);
    tryImplementation(LogFactory::useNoLogging);
}

private LogFactory() {
    // disable construction
}
````

tryImplementation(Runnable runnable) 方法，判断使用哪个 Log 的实现类。
````
private static void tryImplementation(Runnable runnable) {
    if (logConstructor == null) {//当 logConstructor 为空时，执行 runnable 的方法。
        try {
            runnable.run();
        } catch (Throwable t) {
            // ignore
        }
    }
}
````
tryImplementation(LogFactory::useSlf4jLogging) 代码块,执行设置logConstructor
````
// 如果对应的类能创建成功，意味着可以使用，设置为 logConstructor
private static void setImplementation(Class<? extends Log> implClass) {
    try {
        // 获得参数为 String 的构造方法
        Constructor<? extends Log> candidate = implClass.getConstructor(String.class);
        // 创建 Log 对象
        Log log = candidate.newInstance(LogFactory.class.getName());
        if (log.isDebugEnabled()) {
            log.debug("Logging initialized using '" + implClass + "' adapter.");
        }
        // 创建成功，意味着可以使用，设置为 logConstructor
        logConstructor = candidate;
    } catch (Throwable t) {
        throw new LogException("Error setting Log implementation.  Cause: " + t, t);
    }
}
````
### 10.1.2 getLog ###
getLog(...) 方法，获得 Log 对象。

## 10.2 Log ##
org.apache.ibatis.logging.Log ，MyBatis Log 接口。

### 10.2.1 StdOutImpl ###
org.apache.ibatis.logging.stdout.StdOutImpl ，实现 Log 接口，StdOut 实现类。
比较简单，基于 System.out 和 System.err 来实现。

### Slf4jImpl ###
org.apache.ibatis.logging.slf4j.Slf4jImpl ，实现 Log 接口，Slf4j 实现类。

**构造方法**
````
public Slf4jImpl(String clazz) {
    // 使用 SLF LoggerFactory 获得 SLF Logger 对象
    Logger logger = LoggerFactory.getLogger(clazz);
    // 如果是 LocationAwareLogger ，则创建 Slf4jLocationAwareLoggerImpl 对象
    if (logger instanceof LocationAwareLogger) {
        try {
            // check for slf4j >= 1.6 method signature
            logger.getClass().getMethod("log", Marker.class, String.class, int.class, String.class, Object[].class, Throwable.class);
            log = new Slf4jLocationAwareLoggerImpl((LocationAwareLogger) logger);
            return;
        } catch (SecurityException | NoSuchMethodException e) {
            // fail-back to Slf4jLoggerImpl
        }
    }
    // 否则，创建 Slf4jLoggerImpl 对象
    log = new Slf4jLoggerImpl(logger);
}
````

- 在构造方法中，可以看到，适配不同的 SLF4J 的版本，分别使用 org.apache.ibatis.logging.slf4j.Slf4jLocationAwareLoggerImpl 和 org.apache.ibatis.logging.slf4j.Slf4jLoggerImpl 类。
- 具体的方法实现，委托调用对应的 SLF4J 的方法。

## 10.3 BaseJdbcLogger ##
在 org.apache.ibatis.logging 包下的 jdbc 包，有如下五个类：

- BaseJdbcLogger
	- ConnectionLogger
	- PreparedStatementLogger
	- StatementLogger
	- ResultSetLogger

是一个基于 JDBC 接口实现增强的案例，而原理上，也是基于 JDK 实现动态代理。

# 11 注解模块 #

- 增删改查： @Insert、@Update、@Delete、@Select、@MapKey、@Options、@SelelctKey、@Param、@InsertProvider、@UpdateProvider、@DeleteProvider、@SelectProvider
- 结果集映射： @Results、@Result、@ResultMap、@ResultType、@ConstructorArgs、@Arg、@One、@Many、@TypeDiscriminator、@Case
- 缓存： @CacheNamespace、@Property、@CacheNamespaceRef、@Flush

## 11.1 CRUD 常用操作注解 ##

````
// 最基本的注解CRUD
public interface IUserDAO {

    @Select("select *from User")
    public List<User> retrieveAllUsers();
                                                                                                                                                                                                                                  
    //注意这里只有一个参数，则#{}中的标识符可以任意取
    @Select("select *from User where id=#{idss}")
    public User retrieveUserById(int id);
                                                                                                                                                                                                                                  
    @Select("select *from User where id=#{id} and userName like #{name}")
    public User retrieveUserByIdAndName(@Param("id")int id,@Param("name")String names);
                                                                                                                                                                                                                                  
    @Insert("INSERT INTO user(userName,userAge,userAddress) VALUES(#{userName},"
            + "#{userAge},#{userAddress})")
    public void addNewUser(User user);
                                                                                                                                                                                                                                  
    @Delete("delete from user where id=#{id}")
    public void deleteUser(int id);
                                                                                                                                                                                                                                  
    @Update("update user set userName=#{userName},userAddress=#{userAddress}"
            + " where id=#{id}")
    public void updateUser(User user);
    
}
````

### 11.1.1 @Select ###
org.apache.ibatis.annotations.@Select ，查询语句注解。

### 11.1.2 @Insert ###
org.apache.ibatis.annotations.@Insert ，插入语句注解。

### 11.1.3 @Update ###
org.apache.ibatis.annotations.@Update ，更新语句注解。

### 11.1.4 @Delete ###
org.apache.ibatis.annotations.@Delete ，删除语句注解。

### 11.1.5 @Param ###
org.apache.ibatis.annotations.@Param ，方法参数名的注解。

- 当映射器方法需多个参数，这个注解可以被应用于映射器方法参数来给每个参数一个名字。否则，多参数将会以它们的顺序位置来被命名。比如 #{1}，#{2} 等，这是默认的。
- 使用 @Param("person") ，SQL 中参数应该被命名为 #{person} 。

## 11.2 CRUD 高级操作注解 ##

````
@CacheNamespace(size=100)
public interface IBlogDAO {

    @SelectProvider(type = BlogSqlProvider.class, method = "getSql") 
    @Results(value ={ 
            @Result(id=true, property="id",column="id",javaType=Integer.class,jdbcType=JdbcType.INTEGER),
            @Result(property="title",column="title",javaType=String.class,jdbcType=JdbcType.VARCHAR),
            @Result(property="date",column="date",javaType=String.class,jdbcType=JdbcType.VARCHAR),
            @Result(property="authername",column="authername",javaType=String.class,jdbcType=JdbcType.VARCHAR),
            @Result(property="content",column="content",javaType=String.class,jdbcType=JdbcType.VARCHAR),
            }) 
    public Blog getBlog(@Param("id") int id);
                                                                                                                                                                                      
    @SelectProvider(type = BlogSqlProvider.class, method = "getAllSql") 
    @Results(value ={ 
            @Result(id=true, property="id",column="id",javaType=Integer.class,jdbcType=JdbcType.INTEGER),
            @Result(property="title",column="title",javaType=String.class,jdbcType=JdbcType.VARCHAR),
            @Result(property="date",column="date",javaType=String.class,jdbcType=JdbcType.VARCHAR),
            @Result(property="authername",column="authername",javaType=String.class,jdbcType=JdbcType.VARCHAR),
            @Result(property="content",column="content",javaType=String.class,jdbcType=JdbcType.VARCHAR),
            }) 
    public List<Blog> getAllBlog();
                                                                                                                                                                                      
    @SelectProvider(type = BlogSqlProvider.class, method = "getSqlByTitle") 
    @ResultMap(value = "sqlBlogsMap") 
    // 这里调用resultMap，这个是SQL配置文件中的,必须该SQL配置文件与本接口有相同的全限定名
    // 注意文件中的namespace路径必须是使用@resultMap的类路径
    public List<Blog> getBlogByTitle(@Param("title")String title);
                                                                                                                                                                                      
    @InsertProvider(type = BlogSqlProvider.class, method = "insertSql") 
    public void insertBlog(Blog blog);
                                                                                                                                                                                      
    @UpdateProvider(type = BlogSqlProvider.class, method = "updateSql")
    public void updateBlog(Blog blog);
                                                                                                                                                                                      
    @DeleteProvider(type = BlogSqlProvider.class, method = "deleteSql")
    @Options(useCache = true, flushCache = false, timeout = 10000) 
    public void deleteBlog(int ids);
                                                                                                                                                                                      
}
````
BlogSqlProvider 类

````
wmport java.util.Map;
import static org.apache.ibatis.jdbc.SqlBuilder.*;
package com.whut.sqlTool;
import java.util.Map;
import static org.apache.ibatis.jdbc.SqlBuilder.*;

public class BlogSqlProvider {

    private final static String TABLE_NAME = "blog";
    
    public String getSql(Map<Integer, Object> parameter) {
        BEGIN();
        //SELECT("id,title,authername,date,content");
        SELECT("*");
        FROM(TABLE_NAME);
        //注意这里这种传递参数方式，#{}与map中的key对应，而map中的key又是注解param设置的
        WHERE("id = #{id}");
        return SQL();
    }
    
    public String getAllSql() {
        BEGIN();
        SELECT("*");
        FROM(TABLE_NAME);
        return SQL();
    }
    
    public String getSqlByTitle(Map<String, Object> parameter) {
        String title = (String) parameter.get("title");
        BEGIN();
        SELECT("*");
        FROM(TABLE_NAME);
        if (title != null)
            WHERE(" title like #{title}");
        return SQL();
    }
    
    public String insertSql() {
        BEGIN();
        INSERT_INTO(TABLE_NAME);
        VALUES("title", "#{title}");
        //  VALUES("title", "#{tt.title}");
        //这里是传递一个Blog对象的，如果是利用上面tt.方式，则必须利用Param来设置别名
        VALUES("date", "#{date}");
        VALUES("authername", "#{authername}");
        VALUES("content", "#{content}");
        return SQL();
    }
    
    public String deleteSql() {
        BEGIN();
        DELETE_FROM(TABLE_NAME);
        WHERE("id = #{id}");
        return SQL();
    }
    
    public String updateSql() {
        BEGIN();
        UPDATE(TABLE_NAME);
        SET("content = #{content}");
        WHERE("id = #{id}");
        return SQL();
    }
}
````
该示例使用 org.apache.ibatis.jdbc.SqlBuilder 来实现 SQL 的拼接与生成。实际上，目前该类已经废弃，推荐使用个的是 org.apache.ibatis.jdbc.SQL 类。
具体的 SQL 使用示例，可参见 org.apache.ibatis.jdbc.SQLTest 单元测试类。

Mapper XML 配置：
````
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
"http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.whut.inter.IBlogDAO">
   <resultMap type="Blog" id="sqlBlogsMap">
      <id property="id" column="id"/>
      <result property="title" column="title"/>
      <result property="authername" column="authername"/>
      <result property="date" column="date"/>
      <result property="content" column="content"/>
   </resultMap> 
</mapper>
````

### 11.2.1 @SelectProvider ###
org.apache.ibatis.annotations.@SelectProvider ，查询语句提供器。

从上面的使用示例可知，XXXProvider 的用途是，指定一个类( type )的指定方法( method )，返回使用的 SQL 。并且，该方法可以使用 Map<String,Object> params 来作为方法参数，传递参数。

### 11.2.2 @InsertProvider ###
org.apache.ibatis.annotations.@InsertProvider ，插入语句提供器。

### 11.2.3 @UpdateProvider ###
org.apache.ibatis.annotations.@UpdateProvider ，更新语句提供器。

### 11.2.4 @DeleteProvider ###
org.apache.ibatis.annotations.@DeleteProvider ，删除语句提供器。

### 11.2.5 @Results ###
org.apache.ibatis.annotations.@Results ，结果的注解。

### 11.2.6 @Result ###
org.apache.ibatis.annotations.@Results ，结果字段的注解。

#### 11.2.6.1 @One ####
org.apache.ibatis.annotations.@One ，复杂类型的单独属性值的注解。

#### 11.2.6.2 @Many ####
org.apache.ibatis.annotations.@Many ，复杂类型的集合属性值的注解。

#### 11.2.7 @ResultMap ####
org.apache.ibatis.annotations.@ResultMap ，使用的结果集的注解。

- 例如上述示例的 #getBlogByTitle(@Param("title")String title) 方法，使用的注解为 @ResultMap(value = "sqlBlogsMap")，而 "sqlBlogsMap" 中 Mapper XML 中有相关的定义。

#### 11.2.8 @ResultType ####
org.apache.ibatis.annotations.@ResultType ，结果类型。

### 11.2.9 @CacheNamespace ###
org.apache.ibatis.annotations.@CacheNamespace ，缓存空间配置的注解。

````
对应 XML 标签为 <cache />
````

#### 11.2.9.1 @Property ####
org.apache.ibatis.annotations.@Property ，属性的注解。

#### 11.2.10 @CacheNamespaceRef ####
org.apache.ibatis.annotations.@CacheNamespaceRef ，指向指定命名空间的注解。

````
对应 XML 标签为 <cache-ref />
````
### 11.2.11 @Options ###
org.apache.ibatis.annotations.@Options ，操作可选项。

### 11.2.12 @SelectKey ###
org.apache.ibatis.annotations.@SelectKey ，通过 SQL 语句获得主键的注解。

### 11.2.13 @MapKey ###
org.apache.ibatis.annotations.@MapKey ，Map 结果的键的注解。

### 11.2.14 @Flush ###
org.apache.ibatis.annotations.@Flush ，Flush 注解。
如果使用了这个注解，定义在 Mapper 接口中的方法能够调用 SqlSession#flushStatements() 方法。（Mybatis 3.3及以上）

## 11.3 其它注解 ##
### 11.3.1 @Mapper ###
org.apache.ibatis.annotations.Mapper ，标记这是个 Mapper 的注解。

### 11.3.2 @Lang ###
org.apache.ibatis.annotations.@Lang ，语言驱动的注解。
https://www.jianshu.com/p/03642b807688
