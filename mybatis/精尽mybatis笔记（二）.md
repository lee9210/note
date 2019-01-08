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















----