# 19 SQL 初始化（下）之 SqlSource #
## 19.1 SqlSource ##
org.apache.ibatis.mapping.SqlSource ，SQL 来源接口。它代表从 Mapper XML 或方法注解上，读取的一条 SQL 内容

````
public interface SqlSource {

    /**
     * 根据传入的参数对象，返回 BoundSql 对象
     * @param parameterObject 参数对象
     * @return BoundSql 对象
     */
    BoundSql getBoundSql(Object parameterObject);

}
````

## 19.2 SqlSourceBuilder ##
org.apache.ibatis.builder.SqlSourceBuilder ，继承 BaseBuilder 抽象类，SqlSource 构建器，负责将 SQL 语句中的 #{} 替换成相应的 ? 占位符，并获取该 ? 占位符对应的 org.apache.ibatis.mapping.ParameterMapping 对象。

### 19.2.1 构造方法 ###

````
private static final String parameterProperties = "javaType,jdbcType,mode,numericScale,resultMap,typeHandler,jdbcTypeName";

public SqlSourceBuilder(Configuration configuration) {
    super(configuration);
}
````

### 19.2.2 parse ###
````
/**
 * 执行解析原始 SQL ，成为 SqlSource 对象
 *
 * @param originalSql 原始 SQL
 * @param parameterType 参数类型
 * @param additionalParameters 附加参数集合。可能是空集合，也可能是 {@link org.apache.ibatis.scripting.xmltags.DynamicContext#bindings} 集合
 * @return SqlSource 对象
 */
public SqlSource parse(String originalSql, Class<?> parameterType, Map<String, Object> additionalParameters) {
    // <1> 创建 ParameterMappingTokenHandler 对象
    ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler(configuration, parameterType, additionalParameters);
    // <2> 创建 GenericTokenParser 对象
    GenericTokenParser parser = new GenericTokenParser("#{", "}", handler);
    // <3> 执行解析
    String sql = parser.parse(originalSql);
    // <4> 创建 StaticSqlSource 对象
    return new StaticSqlSource(configuration, sql, handler.getParameterMappings());
}
````

### 19.2.3 ParameterMappingTokenHandler ###
ParameterMappingTokenHandler ，实现 TokenHandler 接口，继承 BaseBuilder 抽象类，负责将匹配到的 #{ 和 } 对，替换成相应的 ? 占位符，并获取该 ? 占位符对应的 org.apache.ibatis.mapping.ParameterMapping 对象。

#### 19.2.3.1 构造方法 ####
ParameterMappingTokenHandler 是 SqlSourceBuilder 的内部私有静态类。
````
/**
 * ParameterMapping 数组
 */
private List<ParameterMapping> parameterMappings = new ArrayList<>();
/**
 * 参数类型
 */
private Class<?> parameterType;
/**
 * additionalParameters 参数的对应的 MetaObject 对象
 */
private MetaObject metaParameters;

public ParameterMappingTokenHandler(Configuration configuration, Class<?> parameterType, Map<String, Object> additionalParameters) {
    super(configuration);
    this.parameterType = parameterType;
    // 创建 additionalParameters 参数的对应的 MetaObject 对象
    this.metaParameters = configuration.newMetaObject(additionalParameters);
}
````

#### 19.2.3.2 handleToken ####
````
@Override
public String handleToken(String content) {
    // <1> 构建 ParameterMapping 对象，并添加到 parameterMappings 中
    parameterMappings.add(buildParameterMapping(content));
    // <2> 返回 ? 占位符
    return "?";
}
````

#### 19.2.3.3 buildParameterMapping ####
buildParameterMapping(String content) 方法，构建 ParameterMapping 对象。

## 19.3 SqlSource 的实现类 ##
### 19.3.1 StaticSqlSource ###
org.apache.ibatis.builder.StaticSqlSource ，实现 SqlSource 接口，静态的 SqlSource 实现类。
````
public class StaticSqlSource implements SqlSource {

    /**
     * 静态的 SQL
     */
    private final String sql;
    /**
     * ParameterMapping 集合
     */
    private final List<ParameterMapping> parameterMappings;
    private final Configuration configuration;

    public StaticSqlSource(Configuration configuration, String sql) {
        this(configuration, sql, null);
    }

    public StaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.configuration = configuration;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        // 创建 BoundSql 对象
        return new BoundSql(configuration, sql, parameterMappings, parameterObject);
    }

}
````
getBoundSql((Object parameterObject) 方法，创建 BoundSql 对象。通过 parameterMappings 和 parameterObject 属性，可以设置 sql 上的每个占位符的值。例如：
![](/picture/mybatis-getboundsql-map.png)

- 如果是动态 SQL 的情况下，则创建 DynamicSqlSource 对象。
- 如果非动态 SQL 的情况下，则创建 RawSqlSource 对象。


### 19.3.2 DynamicSqlSource ###
org.apache.ibatis.scripting.xmltags.DynamicSqlSource ，实现 SqlSource 接口，动态的 SqlSource 实现类。

适用于使用了 OGNL 表达式，或者使用了 ${} 表达式的 SQL ，所以它是动态的，需要在每次执行 #getBoundSql(Object parameterObject) 方法，根据参数，生成对应的 SQL 。

````
public class DynamicSqlSource implements SqlSource {

    private final Configuration configuration;
    /**
     * 根 SqlNode 对象
     */
    private final SqlNode rootSqlNode;

    public DynamicSqlSource(Configuration configuration, SqlNode rootSqlNode) {
        this.configuration = configuration;
        this.rootSqlNode = rootSqlNode;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        // <1> 创建 DynamicContext 对象，并执行 DynamicContext#apply(DynamicContext context) 方法，应用 rootSqlNode ，相当于生成动态 SQL 。
        DynamicContext context = new DynamicContext(configuration, parameterObject);
        rootSqlNode.apply(context);
        // <2> 创建 SqlSourceBuilder 对象，并执行 SqlSourceBuilder#parse(String originalSql, Class<?> parameterType, Map<String, Object> additionalParameters) 方法，解析出 SqlSource 对象。
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
        // <2> 返回的 SqlSource 对象，类型是 StaticSqlSource 类。
		// 这个过程，会将 #{} 对，转换成对应的 ? 占位符，并获取该占位符对应的 ParameterMapping 对象。
        Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
        SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());
        // <3> 调用 StaticSqlSource#getBoundSql(Object parameterObject) 方法，获得 BoundSql 对象。
        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        // <4> 从 context.bindings 中，添加附加参数到 BoundSql 对象中。
        for (Map.Entry<String, Object> entry : context.getBindings().entrySet()) {
            boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
        }
        // <5> 返回 BoundSql 对象
        return boundSql;
    }
}
````

### 19.3.3 RawSqlSource ###
org.apache.ibatis.scripting.xmltags.RawSqlSource ，实现 SqlSource 接口，原始的 SqlSource 实现类。
适用于仅使用 #{} 表达式，或者不使用任何表达式的情况，所以它是静态的，仅需要在构造方法中，直接生成对应的 SQL 。
````
public class RawSqlSource implements SqlSource {

    /**
     * SqlSource 对象
     */
    private final SqlSource sqlSource;

    public RawSqlSource(Configuration configuration, SqlNode rootSqlNode, Class<?> parameterType) {
        // <1> 调用 #getSql(Configuration configuration, SqlNode rootSqlNode) 方法，获得 SQL 。
        this(configuration, getSql(configuration, rootSqlNode), parameterType);
    }

    public RawSqlSource(Configuration configuration, String sql, Class<?> parameterType) {
        // <2> 创建 SqlSourceBuilder 对象，并执行 SqlSourceBuilder#parse(String originalSql, Class<?> parameterType, Map<String, Object> additionalParameters) 方法，解析出 SqlSource 对象。对应到 DynamicSqlSource 
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
        Class<?> clazz = parameterType == null ? Object.class : parameterType;
        // <2> 获得 SqlSource 对象
        sqlSource = sqlSourceParser.parse(sql, clazz, new HashMap<>());
    }

    private static String getSql(Configuration configuration, SqlNode rootSqlNode) {
        // 创建 DynamicContext 对象
        DynamicContext context = new DynamicContext(configuration, null);
        // 解析出 SqlSource 对象
        rootSqlNode.apply(context);
        // 获得 sql
        return context.getSql();
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        // 获得 BoundSql 对象
        return sqlSource.getBoundSql(parameterObject);
    }
}
````

### 19.3.4 ProviderSqlSource ###
org.apache.ibatis.builder.annotation.ProviderSqlSource ，实现 SqlSource 接口，基于方法上的 @ProviderXXX 注解的 SqlSource 实现类。

#### 19.3.4.1 构造方法 ####
````
private final Configuration configuration;
private final SqlSourceBuilder sqlSourceParser;
/**
 * `@ProviderXXX` 注解的对应的类
 */
private final Class<?> providerType;
/**
 * `@ProviderXXX` 注解的对应的方法
 */
private Method providerMethod;
/**
 * `@ProviderXXX` 注解的对应的方法的参数名数组
 */
private String[] providerMethodArgumentNames;
/**
 * `@ProviderXXX` 注解的对应的方法的参数类型数组
 */
private Class<?>[] providerMethodParameterTypes;
/**
 * 若 {@link #providerMethodParameterTypes} 参数有 ProviderContext 类型的，创建 ProviderContext 对象
 */
private ProviderContext providerContext;
/**
 * {@link #providerMethodParameterTypes} 参数中，ProviderContext 类型的参数，在数组中的位置
 */
private Integer providerContextIndex;

/**
 * @deprecated Please use the {@link #ProviderSqlSource(Configuration, Object, Class, Method)} instead of this.
 */
@Deprecated
public ProviderSqlSource(Configuration configuration, Object provider) {
    this(configuration, provider, null, null);
}

/**
 * @since 3.4.5
 */
public ProviderSqlSource(Configuration configuration, Object provider, Class<?> mapperType, Method mapperMethod) {
    String providerMethodName;
    try {
        this.configuration = configuration;
        // 创建 SqlSourceBuilder 对象
        this.sqlSourceParser = new SqlSourceBuilder(configuration);
        // 获得 @ProviderXXX 注解的对应的类
        this.providerType = (Class<?>) provider.getClass().getMethod("type").invoke(provider);
        // 获得 @ProviderXXX 注解的对应的方法相关的信息
        providerMethodName = (String) provider.getClass().getMethod("method").invoke(provider);
        for (Method m : this.providerType.getMethods()) {
            if (providerMethodName.equals(m.getName()) && CharSequence.class.isAssignableFrom(m.getReturnType())) {
                if (providerMethod != null) {
                    throw new BuilderException("Error creating SqlSource for SqlProvider. Method '"
                            + providerMethodName + "' is found multiple in SqlProvider '" + this.providerType.getName()
                            + "'. Sql provider method can not overload.");
                }
                this.providerMethod = m;
                this.providerMethodArgumentNames = new ParamNameResolver(configuration, m).getNames();
                this.providerMethodParameterTypes = m.getParameterTypes();
            }
        }
    } catch (BuilderException e) {
        throw e;
    } catch (Exception e) {
        throw new BuilderException("Error creating SqlSource for SqlProvider.  Cause: " + e, e);
    }
    if (this.providerMethod == null) {
        throw new BuilderException("Error creating SqlSource for SqlProvider. Method '"
                + providerMethodName + "' not found in SqlProvider '" + this.providerType.getName() + "'.");
    }
    // 初始化 providerContext 和 providerContextIndex 属性
    for (int i = 0; i < this.providerMethodParameterTypes.length; i++) {
        Class<?> parameterType = this.providerMethodParameterTypes[i];
        if (parameterType == ProviderContext.class) {
            if (this.providerContext != null) {
                throw new BuilderException("Error creating SqlSource for SqlProvider. ProviderContext found multiple in SqlProvider method ("
                        + this.providerType.getName() + "." + providerMethod.getName()
                        + "). ProviderContext can not define multiple in SqlProvider method argument.");
            }
            this.providerContext = new ProviderContext(mapperType, mapperMethod);
            this.providerContextIndex = i;
        }
    }
}
````

#### 19.3.4.2 getBoundSql ####
````
@Override
public BoundSql getBoundSql(Object parameterObject) {
    // <1> 调用 #createSqlSource(Object parameterObject) 方法，创建 SqlSource 对象。因为它是通过 @ProviderXXX 注解的指定类的指定方法，动态生成 SQL 。所以，从思路上，和 DynamicSqlSource 是有点接近的。
    SqlSource sqlSource = createSqlSource(parameterObject);
    // <2> 调用 SqlSource#getBoundSql(Object parameterObject) 方法，获得 BoundSql 对象。
    return sqlSource.getBoundSql(parameterObject);
}
````

#### 19.3.4.3 createSqlSource ####
createSqlSource(Object parameterObject) 方法，创建 SqlSource 对象。

#### 19.3.4.4 ProviderContext ####
org.apache.ibatis.builder.annotation.ProviderContext ，ProviderSqlSource 的上下文。

## 19.4 BoundSql ##
org.apache.ibatis.mapping.BoundSql ，一次可执行的 SQL 封装。

### 19.4.1 ParameterMapping ###
org.apache.ibatis.mapping.ParameterMapping ，参数映射。

````
private Configuration configuration;

/**
 * 属性的名字
 */
private String property;
/**
 * 参数类型。
 *
 * 目前只需要关注 ParameterMode.IN 的情况，另外的 OUT、INOUT 是在存储过程中使用，暂时无视
 */
private ParameterMode mode;
/**
 * Java 类型
 */
private Class<?> javaType = Object.class;
/**
 * JDBC 类型
 */
private JdbcType jdbcType;
/**
 * 对于数值类型，还有一个小数保留位数的设置，来确定小数点后保留的位数
 */
private Integer numericScale;
/**
 * TypeHandler 对象
 *
 * {@link Builder#resolveTypeHandler()}
 */
private TypeHandler<?> typeHandler;
/**
 * 貌似只在 ParameterMode 在 OUT、INOUT 是在存储过程中使用
 */
private String resultMapId;
/**
 * 貌似只在 ParameterMode 在 OUT、INOUT 是在存储过程中使用
 */
private String jdbcTypeName;
/**
 * 表达式。
 *
 * ps：目前暂时不支持
 */
private String expression;

public static class Builder {
    
    // ... 省略代码
    
}
````

### 19.4.2 ParameterMode ###
org.apache.ibatis.mapping.ParameterMode ，参数类型。

## 19.5 ParameterHandler ##
org.apache.ibatis.executor.parameter.ParameterHandler ，参数处理器接口。

### 19.5.1 DefaultParameterHandler ###
org.apache.ibatis.scripting.default.DefaultParameterHandler ，实现 ParameterHandler 接口，默认 ParameterHandler 实现类。

#### 19.5.1.1 构造方法 ####
````
private final TypeHandlerRegistry typeHandlerRegistry;
/**
 * MappedStatement 对象
 */
private final MappedStatement mappedStatement;
/**
 * 参数对象
 */
private final Object parameterObject;
/**
 * BoundSql 对象
 */
private final BoundSql boundSql;
private final Configuration configuration;

public DefaultParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
    this.mappedStatement = mappedStatement;
    this.configuration = mappedStatement.getConfiguration();
    this.typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
    this.parameterObject = parameterObject;
    this.boundSql = boundSql;
}
````

#### 19.5.1.2 setParameters ####
setParameters(PreparedStatement ps) 方法

````
@Override
public void setParameters(PreparedStatement ps) {
    ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());
    // <1> 遍历 ParameterMapping 数组
    List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    if (parameterMappings != null) {
        for (int i = 0; i < parameterMappings.size(); i++) {
            // <2> 获得 ParameterMapping 对象
            ParameterMapping parameterMapping = parameterMappings.get(i);
            if (parameterMapping.getMode() != ParameterMode.OUT) {
                // <3> 获得值
                Object value;
                String propertyName = parameterMapping.getProperty();
                if (boundSql.hasAdditionalParameter(propertyName)) { // issue #448 ask first for additional params
                    value = boundSql.getAdditionalParameter(propertyName);
                } else if (parameterObject == null) {
                    value = null;
                } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                    value = parameterObject;
                } else {
                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                    value = metaObject.getValue(propertyName);
                }
                // <4> 获得 typeHandler、jdbcType 属性
                TypeHandler typeHandler = parameterMapping.getTypeHandler();
                JdbcType jdbcType = parameterMapping.getJdbcType();
                if (value == null && jdbcType == null) {
                    jdbcType = configuration.getJdbcTypeForNull();
                }
                // <5> 调用 TypeHandler#setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) 方法，设置指定位置的 ? 占位符的参数。
                try {
                    typeHandler.setParameter(ps, i + 1, value, jdbcType);
                } catch (TypeException | SQLException e) {
                    throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);
                }
            }
        }
    }
}
````

----

# 20 SQL 执行（一）之 Executor #

- 对应 executor 和 cursor 模块。前者对应执行器，后者对应执行结果的游标。
- SQL 语句的执行涉及多个组件 ，其中比较重要的是 Executor、StatementHandler、ParameterHandler 和 ResultSetHandler 。

- Executor 主要负责维护一级缓存和二级缓存，并提供事务管理的相关操作，它会将数据库相关操作委托给 StatementHandler完成。
- StatementHandler 首先通过 ParameterHandler 完成 SQL 语句的实参绑定，然后通过 java.sql.Statement 对象执行 SQL 语句并得到结果集，最后通过 ResultSetHandler 完成结果集的映射，得到结果对象并返回。

整体执行流程：
![](/picture/mybatis-excutor-flow.png)

## 20.1 Executor ##
org.apache.ibatis.executor.Executor ，执行器接口。

````
public interface Executor {

    // 空 ResultHandler 对象的枚举
    ResultHandler NO_RESULT_HANDLER = null;

    // 更新 or 插入 or 删除，由传入的 MappedStatement 的 SQL 所决定
    int update(MappedStatement ms, Object parameter) throws SQLException;

    // 查询，带 ResultHandler + CacheKey + BoundSql
    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql) throws SQLException;
    // 查询，带 ResultHandler
    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException;
    // 查询，返回值为 Cursor
    <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException;

    // 刷入批处理语句
    List<BatchResult> flushStatements() throws SQLException;

    // 提交事务
    void commit(boolean required) throws SQLException;
    // 回滚事务
    void rollback(boolean required) throws SQLException;

    // 创建 CacheKey 对象
    CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql);
    // 判断是否缓存
    boolean isCached(MappedStatement ms, CacheKey key);
    // 清除本地缓存
    void clearLocalCache();

    // 延迟加载
    void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType);

    // 获得事务
    Transaction getTransaction();
    // 关闭事务
    void close(boolean forceRollback);
    // 判断事务是否关闭
    boolean isClosed();

    // 设置包装的 Executor 对象
    void setExecutorWrapper(Executor executor);

}
````

## 20.2 BaseExecutor ##
org.apache.ibatis.executor.BaseExecutor ，实现 Executor 接口，提供骨架方法，从而使子类只要实现指定的几个抽象方法即可。

### 20.2.1 构造方法 ###

````
/**
 * 事务对象
 */
protected Transaction transaction;
/**
 * 包装的 Executor 对象
 */
protected Executor wrapper;

/**
 * DeferredLoad( 延迟加载 ) 队列
 */
protected ConcurrentLinkedQueue<DeferredLoad> deferredLoads;
/**
 * 本地缓存，即一级缓存
 */
protected PerpetualCache localCache;
/**
 * 本地输出类型的参数的缓存
 */
protected PerpetualCache localOutputParameterCache;
protected Configuration configuration;

/**
 * 记录嵌套查询的层级
 */
protected int queryStack;
/**
 * 是否关闭
 */
private boolean closed;

protected BaseExecutor(Configuration configuration, Transaction transaction) {
    this.transaction = transaction;
    this.deferredLoads = new ConcurrentLinkedQueue<>();
    this.localCache = new PerpetualCache("LocalCache");
    this.localOutputParameterCache = new PerpetualCache("LocalOutputParameterCache");
    this.closed = false;
    this.configuration = configuration;
    this.wrapper = this; // 自己
}
````
````
每当我们使用 MyBatis 开启一次和数据库的会话，MyBatis 会创建出一个 SqlSession 对象表示一次数据库会话，而每个 SqlSession 都会创建一个 Executor 对象。

在对数据库的一次会话中，我们有可能会反复地执行完全相同的查询语句，如果不采取一些措施的话，每一次查询都会查询一次数据库，而我们在极短的时间内做了完全相同的查询，那么它们的结果极有可能完全相同，由于查询一次数据库的代价很大，这有可能造成很大的资源浪费。

为了解决这一问题，减少资源的浪费，MyBatis 会在表示会话的SqlSession 对象中建立一个简单的缓存，将每次查询到的结果结果缓存起来，当下次查询的时候，如果判断先前有个完全一样的查询，会直接从缓存中直接将结果取出，返回给用户，不需要再进行一次数据库查询了。 注意，这个“简单的缓存”就是一级缓存，且默认开启，无法关闭。

如下图所示，MyBatis 会在一次会话的表示 —— 一个 SqlSession 对象中创建一个本地缓存( localCache )，对于每一次查询，都会尝试根据查询的条件去本地缓存中查找是否在缓存中，如果在缓存中，就直接从缓存中取出，然后返回给用户；否则，从数据库读取数据，将查询结果存入缓存并返回给用户。
````
![](/picture/mybatis-cache-first.png)

### 20.2.2 clearLocalCache ###
clearLocalCache() 方法，清理一级（本地）缓存。

### 20.2.3 createCacheKey ###
createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) 方法，创建 CacheKey 对象。
````
@Override
public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
    if (closed) {
        throw new ExecutorException("Executor was closed.");
    }
    // <1> 创建 CacheKey 对象
    CacheKey cacheKey = new CacheKey();
    // <2> 设置 id、offset、limit、sql 到 CacheKey 对象中
    cacheKey.update(ms.getId());
    cacheKey.update(rowBounds.getOffset());
    cacheKey.update(rowBounds.getLimit());
    cacheKey.update(boundSql.getSql());
    // <3> 设置 ParameterMapping 数组的元素对应的每个 value 到 CacheKey 对象中
    List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    TypeHandlerRegistry typeHandlerRegistry = ms.getConfiguration().getTypeHandlerRegistry();
    // mimic DefaultParameterHandler logic 这块逻辑，和 DefaultParameterHandler 获取 value 是一致的。
    for (ParameterMapping parameterMapping : parameterMappings) {
        if (parameterMapping.getMode() != ParameterMode.OUT) {
            Object value;
            String propertyName = parameterMapping.getProperty();
            if (boundSql.hasAdditionalParameter(propertyName)) {
                value = boundSql.getAdditionalParameter(propertyName);
            } else if (parameterObject == null) {
                value = null;
            } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                value = parameterObject;
            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                value = metaObject.getValue(propertyName);
            }
            cacheKey.update(value);
        }
    }
    // <4> 设置 Environment.id 到 CacheKey 对象中
    if (configuration.getEnvironment() != null) {
        cacheKey.update(configuration.getEnvironment().getId());
    }
    return cacheKey;
}
````

### 20.2.4 isCached ###
isCached(MappedStatement ms, CacheKey key) 方法，判断一级缓存是否存在。

### 20.2.5 query ###
- query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) 方法，读操作。
````
@Override
public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
    // <1> 调用 MappedStatement#getBoundSql(Object parameterObject) 方法，获得 BoundSql 对象。
    BoundSql boundSql = ms.getBoundSql(parameter);
    // <2> 调用 #createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) 方法，创建 CacheKey 对象。
    CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);
    // <3> 调用 #query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) 方法，读操作。通过这样的方式，两个 #query(...) 方法，实际是统一的。
    return query(ms, parameter, rowBounds, resultHandler, key, boundSql);
}
````

- query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) 方法，读操作。

````
@Override
public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
    ErrorContext.instance().resource(ms.getResource()).activity("executing a query").object(ms.getId());
    // <1> 已经关闭，则抛出 ExecutorException 异常
    if (closed) {
        throw new ExecutorException("Executor was closed.");
    }
    // <2> 清空本地缓存，如果 queryStack 为零，并且要求清空本地缓存。
    if (queryStack == 0 && ms.isFlushCacheRequired()) {
        clearLocalCache();
    }
    List<E> list;
    try {
        // <3> queryStack + 1
        queryStack++;
        // <4.1> 从一级缓存中，获取查询结果
        list = resultHandler == null ? (List<E>) localCache.getObject(key) : null;
        // <4.2> 获取到，则进行处理
        if (list != null) {
            handleLocallyCachedOutputParameters(ms, key, parameter, boundSql);
        // <4.3> 获得不到，则从数据库中查询
        } else {
            list = queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
        }
    } finally {
        // <5> queryStack - 1
        queryStack--;
    }
    if (queryStack == 0) {
        // <6.1> 执行延迟加载
        for (DeferredLoad deferredLoad : deferredLoads) {
            deferredLoad.load();
        }
        // issue #601
        // <6.2> 清空 deferredLoads
        deferredLoads.clear();
        // <7> 如果缓存级别是 LocalCacheScope.STATEMENT ，则进行清理
        if (configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT) {
            // issue #482
            clearLocalCache();
        }
    }
    return list;
}
````
#### 20.2.5.1 queryFromDatabase ####
queryFromDatabase(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) 方法，从数据库中读取操作。

````
private <E> List<E> queryFromDatabase(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
    List<E> list;
    // <1> 在缓存中，添加占位对象。此处的占位符，和延迟加载有关，可见 `DeferredLoad#canLoad()` 方法
    localCache.putObject(key, EXECUTION_PLACEHOLDER);
    try {
        // <2> 执行读操作
        list = doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
    } finally {
        // <3> 从缓存中，移除占位对象
        localCache.removeObject(key);
    }
    // <4> 添加到缓存中
    localCache.putObject(key, list);
    // <5> 暂时忽略，存储过程相关
    if (ms.getStatementType() == StatementType.CALLABLE) {
        localOutputParameterCache.putObject(key, parameter);
    }
    return list;
}
````

#### 20.2.5.2 doQuery ####

### 20.2.6 queryCursor ###
queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) 方法，执行查询，返回的结果为 Cursor 游标对象。

````
@Override
public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
    // <1> 调用 MappedStatement#getBoundSql(Object parameterObject) 方法，获得 BoundSql 对象。
    BoundSql boundSql = ms.getBoundSql(parameter);
    // 调用 #doQueryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) 方法，执行读操作。这是个抽象方法，由子类实现。
    return doQueryCursor(ms, parameter, rowBounds, boundSql);
}
````

### 20.2.7 update ###
update(MappedStatement ms, Object parameter) 方法，执行写操作。
````
@Override
public int update(MappedStatement ms, Object parameter) throws SQLException {
    ErrorContext.instance().resource(ms.getResource()).activity("executing an update").object(ms.getId());
    // <1> 已经关闭，则抛出 ExecutorException 异常
    if (closed) {
        throw new ExecutorException("Executor was closed.");
    }
    // <2> 调用 #clearLocalCache() 方法，清空本地缓存。因为，更新后，可能缓存会失效。但是，又没很好的办法，判断哪一些失效。所以，最稳妥的做法，就是全部清空。
    clearLocalCache();
    // <3> 调用 #doUpdate(MappedStatement ms, Object parameter) 方法，执行写操作。这是个抽象方法，由子类实现。
    return doUpdate(ms, parameter);
}
````

### 20.2.8 flushStatements ###
flushStatements() 方法，刷入批处理语句。

````
@Override
public List<BatchResult> flushStatements() throws SQLException {
    return flushStatements(false);
}

public List<BatchResult> flushStatements(boolean isRollBack) throws SQLException {
    // <1> 已经关闭，则抛出 ExecutorException 异常
    if (closed) {
        throw new ExecutorException("Executor was closed.");
    }
    // <2> 执行刷入批处理语句
    return doFlushStatements(isRollBack);
}
````

### 20.2.9 getTransaction ###
getTransaction() 方法，获得事务对象。

#### 20.2.9.1 commit ####

````
@Override
public void commit(boolean required) throws SQLException {
    // 已经关闭，则抛出 ExecutorException 异常
    if (closed) {
        throw new ExecutorException("Cannot commit, transaction is already closed");
    }
    // 清空本地缓存
    clearLocalCache();
    // 刷入批处理语句
    flushStatements();
    // 是否要求提交事务。如果是，则提交事务。
    if (required) {
        transaction.commit();
    }
}
````

#### 20.2.9.2 rollback ####
````
public void rollback(boolean required) throws SQLException {
    if (!closed) {
        try {
            // 清空本地缓存
            clearLocalCache();
            // 刷入批处理语句
            flushStatements(true);
        } finally {
            if (required) {
                // 是否要求回滚事务。如果是，则回滚事务。
                transaction.rollback();
            }
        }
    }
}
````
### 20.2.10 close ###
close() 方法，关闭执行器。

````
@Override
public void close(boolean forceRollback) {
    try {
        // 回滚事务
        try {
            rollback(forceRollback);
        } finally {
            // 关闭事务
            if (transaction != null) {
                transaction.close();
            }
        }
    } catch (SQLException e) {
        // Ignore.  There's nothing that can be done at this point.
        log.warn("Unexpected exception on closing transaction.  Cause: " + e);
    } finally {
        // 置空变量
        transaction = null;
        deferredLoads = null;
        localCache = null;
        localOutputParameterCache = null;
        closed = true;
    }
}
````
### 20.2.11 deferLoad ###

### 20.2.12 setExecutorWrapper ###
setExecutorWrapper(Executor wrapper) 方法，设置包装器。

### 20.2.13 其它方法 ###
````
// 获得 Connection 对象
protected Connection getConnection(Log statementLog) throws SQLException {
    // 获得 Connection 对象
    Connection connection = transaction.getConnection();
    // 如果 debug 日志级别，则创建 ConnectionLogger 对象，进行动态代理
    if (statementLog.isDebugEnabled()) {
        return ConnectionLogger.newInstance(connection, statementLog, queryStack);
    } else {
        return connection;
    }
}

// 设置事务超时时间
protected void applyTransactionTimeout(Statement statement) throws SQLException {
    StatementUtil.applyTransactionTimeout(statement, statement.getQueryTimeout(), transaction.getTimeout());
}

// 关闭 Statement 对象
protected void closeStatement(Statement statement) {
    if (statement != null) {
        try {
            if (!statement.isClosed()) {
                statement.close();
            }
        } catch (SQLException e) {
            // ignore
        }
    }
}
````

## 20.3 SimpleExecutor ##
org.apache.ibatis.executor.SimpleExecutor ，继承 BaseExecutor 抽象类，简单的 Executor 实现类。

- 每次开始读或写操作，都创建对应的 Statement 对象。
- 执行完成后，关闭该 Statement 对象。

### 20.3.1 构造方法 ###

````
public SimpleExecutor(Configuration configuration, Transaction transaction) {
    super(configuration, transaction);
}
````

### 20.3.2 doQuery ###
从此处开始，我们会看到 StatementHandler 相关的调用

````
@Override
public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
    Statement stmt = null;
    try {
        Configuration configuration = ms.getConfiguration();
        // <1> 创建 StatementHandler 对象
        StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
        // <2> 初始化 StatementHandler 对象
        stmt = prepareStatement(handler, ms.getStatementLog());
        // <3> 执行 StatementHandler  ，进行读操作
        return handler.query(stmt, resultHandler);
    } finally {
        // <4> 关闭 StatementHandler 对象
        closeStatement(stmt);
    }
}

private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
    Statement stmt;
    // <2.1> 获得 Connection 对象
    Connection connection = getConnection(statementLog);
    // <2.2> 创建 Statement 或 PrepareStatement 对象
    stmt = handler.prepare(connection, transaction.getTimeout());
    // <2.3> 设置 SQL 上的参数，例如 PrepareStatement 对象上的占位符
    handler.parameterize(stmt);
    return stmt;
}
````

### 20.3.3 doQueryCursor ###
和 #doQuery(...) 方法的思路是一致的

````
@Override
protected <E> Cursor<E> doQueryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) throws SQLException {
    Configuration configuration = ms.getConfiguration();
    // 创建 StatementHandler 对象
    StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, null, boundSql);
    // 初始化 StatementHandler 对象
    Statement stmt = prepareStatement(handler, ms.getStatementLog());
    // 设置 Statement ，如果执行完成，则进行自动关闭
    stmt.closeOnCompletion();
    // 执行 StatementHandler  ，进行读操作
    return handler.queryCursor(stmt);
}
````

### 20.3.4 doUpdate ###
````
@Override
public int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
    Statement stmt = null;
    try {
        Configuration configuration = ms.getConfiguration();
        // 创建 StatementHandler 对象
        StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, RowBounds.DEFAULT, null, null);
        // 初始化 StatementHandler 对象
        stmt = prepareStatement(handler, ms.getStatementLog());
        // <3> 执行 StatementHandler ，进行写操作
        return handler.update(stmt);
    } finally {
        // 关闭 StatementHandler 对象
        closeStatement(stmt);
    }
}
````
相比 #doQuery(...) 方法，差异点在 <3> 处，换成了调用 StatementHandler#update(Statement statement) 方法，进行写操作。

### 20.3.5 doFlushStatements ###
````
@Override
public List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException {
    return Collections.emptyList();
}
````
不存在批量操作的情况，所以直接返回空数组。

## 20.4 ReuseExecutor ##

org.apache.ibatis.executor.ReuseExecutor ，继承 BaseExecutor 抽象类，可重用的 Executor 实现类。

- 每次开始读或写操作，优先从缓存中获取对应的 Statement 对象。如果不存在，才进行创建。
- 执行完成后，不关闭该 Statement 对象。
- 其它的，和 SimpleExecutor 是一致的。


### 20.4.1 构造方法 ###
````
/**
 * Statement 的缓存
 *
 * KEY ：SQL
 */
private final Map<String, Statement> statementMap = new HashMap<>();

public ReuseExecutor(Configuration configuration, Transaction transaction) {
    super(configuration, transaction);
}
````

### 20.4.2 doQuery ###
````
@Override
public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
    Configuration configuration = ms.getConfiguration();
    // 创建 StatementHandler 对象
    StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
    // <1> 初始化 StatementHandler 对象
    Statement stmt = prepareStatement(handler, ms.getStatementLog());
    // 执行 StatementHandler  ，进行读操作
    return handler.query(stmt, resultHandler);
}
private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
    Statement stmt;
    BoundSql boundSql = handler.getBoundSql();
    String sql = boundSql.getSql();
    // 存在
    if (hasStatementFor(sql)) {
        // <1.1> 从缓存中获得 Statement 或 PrepareStatement 对象
        stmt = getStatement(sql);
        // <1.2> 设置事务超时时间
        applyTransactionTimeout(stmt);
    // 不存在
    } else {
        // <2.1> 获得 Connection 对象
        Connection connection = getConnection(statementLog);
        // <2.2> 创建 Statement 或 PrepareStatement 对象
        stmt = handler.prepare(connection, transaction.getTimeout());
        // <2.3> 添加到缓存中
        putStatement(sql, stmt);
    }
    // <2> 设置 SQL 上的参数，例如 PrepareStatement 对象上的占位符
    handler.parameterize(stmt);
    return stmt;
}
````

- ReuseExecutor 考虑到重用性，但是 Statement 最终还是需要有地方关闭。答案就在 #doFlushStatements(boolean isRollback) 方法中。而 BaseExecutor 在关闭 #close() 方法中，最终也会调用该方法，从而完成关闭缓存的 Statement 对象们
- 另外，BaseExecutor 在提交或者回滚事务方法中，最终也会调用该方法，也能完成关闭缓存的 Statement 对象们。

## 20.5 BatchExecutor ##
org.apache.ibatis.executor.BatchExecutor ，继承 BaseExecutor 抽象类，批量执行的 Executor 实现类。

````
执行update（没有select，JDBC批处理不支持select），将所有sql都添加到批处理中（addBatch()），等待统一执行（executeBatch()），它缓存了多个Statement对象，每个Statement对象都是addBatch()完毕后，等待逐一执行executeBatch()批处理的；BatchExecutor相当于维护了多个桶，每个桶里都装了很多属于自己的SQL，就像苹果蓝里装了很多苹果，番茄蓝里装了很多番茄，最后，再统一倒进仓库。（可以是Statement或PrepareStatement对象）
````

### 20.5.1 构造方法 ###
````
/**
 * Statement 数组
 */
private final List<Statement> statementList = new ArrayList<>();
/**
 * BatchResult 数组
 *
 * 每一个 BatchResult 元素，对应一个 {@link #statementList} 的 Statement 元素
 */
private final List<BatchResult> batchResultList = new ArrayList<>();
/**
 * 当前 SQL
 */
private String currentSql;
/**
 * 当前 MappedStatement 对象
 */
private MappedStatement currentStatement;

public BatchExecutor(Configuration configuration, Transaction transaction) {
    super(configuration, transaction);
}
````

### 20.5.2 BatchResult ###
org.apache.ibatis.executor.BatchResult ，相同 SQL 聚合的结果。

### 20.5.3 doUpdate ###

````
@Override
public int doUpdate(MappedStatement ms, Object parameterObject) throws SQLException {
    final Configuration configuration = ms.getConfiguration();
    // <1> 创建 StatementHandler 对象
    final StatementHandler handler = configuration.newStatementHandler(this, ms, parameterObject, RowBounds.DEFAULT, null, null);
    final BoundSql boundSql = handler.getBoundSql();
    final String sql = boundSql.getSql();
    final Statement stmt;
    // <2> 如果匹配最后一次 currentSql 和 currentStatement ，则聚合到 BatchResult 中
    if (sql.equals(currentSql) && ms.equals(currentStatement)) {
        // <2.1> 获得最后一次的 Statement 对象
        int last = statementList.size() - 1;
        stmt = statementList.get(last);
        // <2.2> 设置事务超时时间
        applyTransactionTimeout(stmt);
        // <2.3> 设置 SQL 上的参数，例如 PrepareStatement 对象上的占位符
        handler.parameterize(stmt);//fix Issues 322
        // <2.4> 获得最后一次的 BatchResult 对象，并添加参数到其中
        BatchResult batchResult = batchResultList.get(last);
        batchResult.addParameterObject(parameterObject);
    // <3> 如果不匹配最后一次 currentSql 和 currentStatement ，则新建 BatchResult 对象
    } else {
        // <3.1> 获得 Connection
        Connection connection = getConnection(ms.getStatementLog());
        // <3.2> 创建 Statement 或 PrepareStatement 对象
        stmt = handler.prepare(connection, transaction.getTimeout());
        // <3.3> 设置 SQL 上的参数，例如 PrepareStatement 对象上的占位符
        handler.parameterize(stmt);    //fix Issues 322
        // <3.4> 重新设置 currentSql 和 currentStatement
        currentSql = sql;
        currentStatement = ms;
        // <3.5> 添加 Statement 到 statementList 中
        statementList.add(stmt);
        // <3.6> 创建 BatchResult 对象，并添加到 batchResultList 中
        batchResultList.add(new BatchResult(ms, sql, parameterObject));
    }
    // handler.parameterize(stmt);
    // <4> 批处理
    handler.batch(stmt);
    return BATCH_UPDATE_RETURN_VALUE;
}
````

### 20.5.4 doFlushStatements ###
````
@Override
public List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException {
    try {
        // <1> 如果 isRollback 为 true ，返回空数组
        if (isRollback) {
            return Collections.emptyList();
        }
        // <2> 遍历 statementList 和 batchResultList 数组，逐个提交批处理
        List<BatchResult> results = new ArrayList<>();
        for (int i = 0, n = statementList.size(); i < n; i++) {
            // <2.1> 获得 Statement 和 BatchResult 对象
            Statement stmt = statementList.get(i);
            applyTransactionTimeout(stmt);
            BatchResult batchResult = batchResultList.get(i);
            try {
                // <2.2> 批量执行
                batchResult.setUpdateCounts(stmt.executeBatch());
                // <2.3> 处理主键生成
                MappedStatement ms = batchResult.getMappedStatement();
                List<Object> parameterObjects = batchResult.getParameterObjects();
                KeyGenerator keyGenerator = ms.getKeyGenerator();
                if (Jdbc3KeyGenerator.class.equals(keyGenerator.getClass())) {
                    Jdbc3KeyGenerator jdbc3KeyGenerator = (Jdbc3KeyGenerator) keyGenerator;
                    jdbc3KeyGenerator.processBatch(ms, stmt, parameterObjects);
                } else if (!NoKeyGenerator.class.equals(keyGenerator.getClass())) { //issue #141
                    for (Object parameter : parameterObjects) {
                        keyGenerator.processAfter(this, ms, stmt, parameter);
                    }
                }
                // Close statement to close cursor #1109
                // <2.4> 关闭 Statement 对象
                closeStatement(stmt);
            } catch (BatchUpdateException e) {
                // 如果发生异常，则抛出 BatchExecutorException 异常
                StringBuilder message = new StringBuilder();
                message.append(batchResult.getMappedStatement().getId())
                        .append(" (batch index #")
                        .append(i + 1)
                        .append(")")
                        .append(" failed.");
                if (i > 0) {
                    message.append(" ")
                            .append(i)
                            .append(" prior sub executor(s) completed successfully, but will be rolled back.");
                }
                throw new BatchExecutorException(message.toString(), e, results, batchResult);
            }
            // <2.5> 添加到结果集
            results.add(batchResult);
        }
        return results;
    } finally {
        // <3.1> 关闭 Statement 们
        for (Statement stmt : statementList) {
            closeStatement(stmt);
        }
        // <3.2> 置空 currentSql、statementList、batchResultList 属性
        currentSql = null;
        statementList.clear();
        batchResultList.clear();
    }
}
````

### 20.5.5 doQuery ###
````
@Override
public <E> List<E> doQuery(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql)
        throws SQLException {
    Statement stmt = null;
    try {
        // <1> 刷入批处理语句
        flushStatements();
        Configuration configuration = ms.getConfiguration();
        // 创建 StatementHandler 对象
        StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameterObject, rowBounds, resultHandler, boundSql);
        // 获得 Connection 对象
        Connection connection = getConnection(ms.getStatementLog());
        // 创建 Statement 或 PrepareStatement 对象
        stmt = handler.prepare(connection, transaction.getTimeout());
        // 设置 SQL 上的参数，例如 PrepareStatement 对象上的占位符
        handler.parameterize(stmt);
        // 执行 StatementHandler  ，进行读操作
        return handler.query(stmt, resultHandler);
    } finally {
        // 关闭 StatementHandler 对象
        closeStatement(stmt);
    }
}
````
和 SimpleExecutor 的该方法，逻辑差不多。差别在于 <1> 处，发生查询之前，先调用 #flushStatements() 方法，刷入批处理语句。

## 20.6 二级缓存 ##
在上文中提到的一级缓存中，其最大的共享范围就是一个 SqlSession 内部，如果多个 SqlSession 之间需要共享缓存，则需要使用到二级缓存。开启二级缓存后，会使用 CachingExecutor 装饰 Executor ，进入一级缓存的查询流程前，先在 CachingExecutor 进行二级缓存的查询，具体的工作流程如下所示。

![](/picture/mybatis-second-cache-flow.png)

在代码中就是：


````
/**
 * Cache 对象
 */
private Cache cache;

````

每个 XML Mapper 或 Mapper 接口的每个 SQL 操作声明，对应一个 MappedStatement 对象。通过 @CacheNamespace 或 <cache /> 来声明，创建其所使用的 Cache 对象；也可以通过 @CacheNamespaceRef 或 <cache-ref /> 来声明，使用指定 Namespace 的 Cache 对象。

最终在 Configuration 类中的体现：


````
/**
 * Cache 对象集合
 *
 * KEY：命名空间 namespace
 */
protected final Map<String, Cache> caches = new StrictMap<>("Caches collection");

````


### 20.6.1 CachingExecutor ###
org.apache.ibatis.executor.CachingExecutor ，实现 Executor 接口，支持二级缓存的 Executor 的实现类。

#### 20.6.1.1 构造方法 ####


````
/**
 * 被委托的 Executor 对象
 */
private final Executor delegate;
/**
 * TransactionalCacheManager 对象，支持事务的缓存管理器。因为二级缓存是支持跨 Session 进行共享，此处需要考虑事务，那么，必然需要做到事务提交时，才将当前事务中查询时产生的缓存，同步到二级缓存中。这个功能，就通过 TransactionalCacheManager 来实现。
 */
private final TransactionalCacheManager tcm = new TransactionalCacheManager();

public CachingExecutor(Executor delegate) {
    // <1>
    this.delegate = delegate;
    // <2> 调用 delegate 属性的 #setExecutorWrapper(Executor executor) 方法，设置 delegate 被当前执行器所包装。
    delegate.setExecutorWrapper(this);
}
````

#### 20.6.1.2  直接调用委托方法 ####
CachingExecutor 的如下方法，具体的实现代码，是直接调用委托执行器 delegate 的对应的方法。

````
public Transaction getTransaction() { return delegate.getTransaction(); }

@Override
public boolean isClosed() { return delegate.isClosed(); }

@Override
public List<BatchResult> flushStatements() throws SQLException { return delegate.flushStatements(); }

@Override
public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) { return delegate.createCacheKey(ms, parameterObject, rowBounds, boundSql); }

@Override
public boolean isCached(MappedStatement ms, CacheKey key) { return delegate.isCached(ms, key); }

@Override
public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) { delegate.deferLoad(ms, resultObject, property, key, targetType); }

@Override
public void clearLocalCache() { delegate.clearLocalCache(); }
````

#### 20.6.1.3 query ####
````
@Override
public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
    // 获得 BoundSql 对象
    BoundSql boundSql = ms.getBoundSql(parameterObject);
    // 创建 CacheKey 对象
    CacheKey key = createCacheKey(ms, parameterObject, rowBounds, boundSql);
    // 查询
    return query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
}

@Override
public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql)
        throws SQLException {
    // <1> 
    Cache cache = ms.getCache();
    if (cache != null) { // <2> 
        // <2.1> 如果需要清空缓存，则进行清空
        flushCacheIfRequired(ms);
        if (ms.isUseCache() && resultHandler == null) { // <2.2>
            // 暂时忽略，存储过程相关
            ensureNoOutParams(ms, boundSql);
            @SuppressWarnings("unchecked")
            // <2.3> 从二级缓存中，获取结果
            List<E> list = (List<E>) tcm.getObject(cache, key);
            if (list == null) {
                // <2.4.1> 如果不存在，则从数据库中查询
                list = delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
                // <2.4.2> 缓存结果到二级缓存中
                tcm.putObject(cache, key, list); // issue #578 and #116
            }
            // <2.5> 如果存在，则直接返回结果
            return list;
        }
    }
    // <3> 不使用缓存，则从数据库中查询
    return delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
}
````
#### 20.6.1.4 queryCursor ####
````
@Override
public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
    // 如果需要清空缓存，则进行清空
    flushCacheIfRequired(ms);
    // 执行 delegate 对应的方法
    return delegate.queryCursor(ms, parameter, rowBounds);
}
````
#### 20.6.1.5 update ####
````
@Override
public int update(MappedStatement ms, Object parameterObject) throws SQLException {
    // 如果需要清空缓存，则进行清空
    flushCacheIfRequired(ms);
    // 执行 delegate 对应的方法
    return delegate.update(ms, parameterObject);
}
````

### 20.6.2 TransactionalCacheManager ###
org.apache.ibatis.cache.TransactionalCacheManager ，TransactionalCache 管理器。

#### 20.6.2.1 构造方法 ####
````
/**
 * Cache 和 TransactionalCache 的映射
 */
private final Map<Cache, TransactionalCache> transactionalCaches = new HashMap<>();

````
为什么是一个 Map 对象呢？因为在一次的事务过程中，可能有多个不同的 MappedStatement 操作，而它们可能对应多个 Cache 对象。

#### 20.6.2.2 putObject ####
putObject(Cache cache, CacheKey key, Object value) 方法，添加 Cache + KV ，到缓存中。

````
public void putObject(Cache cache, CacheKey key, Object value) {
    // 首先，获得 Cache 对应的 TransactionalCache 对象
    // 然后，添加 KV 到 TransactionalCache 对象中
    getTransactionalCache(cache).putObject(key, value);
}
````

#### 20.6.2.3 getObject ####
getObject(Cache cache, CacheKey key) 方法，获得缓存中，指定 Cache + K 的值。

````
public Object getObject(Cache cache, CacheKey key) {
    // 首先，获得 Cache 对应的 TransactionalCache 对象
    // 然后从 TransactionalCache 对象中，获得 key 对应的值
    return getTransactionalCache(cache).getObject(key);
}
````

### 20.6.3 TransactionalCache ###
org.apache.ibatis.cache.decorators.TransactionalCache ，实现 Cache 接口，支持事务的 Cache 实现类，主要用于二级缓存中。

#### 20.6.3.1 构造方法 ####

````
/**
 * 委托的 Cache 对象。
 *
 * 实际上，就是二级缓存 Cache 对象。
 */
private final Cache delegate;
/**
 * 提交时，清空 {@link #delegate}
 *
 * 初始时，该值为 false
 * 清理后{@link #clear()} 时，该值为 true ，表示持续处于清空状态
 */
private boolean clearOnCommit;
/**
 * 待提交的 KV 映射
 */
private final Map<Object, Object> entriesToAddOnCommit;
/**
 * 查找不到的 KEY 集合
 */
private final Set<Object> entriesMissedInCache;

public TransactionalCache(Cache delegate) {
    this.delegate = delegate;
    this.clearOnCommit = false;
    this.entriesToAddOnCommit = new HashMap<>();
    this.entriesMissedInCache = new HashSet<>();
}
````
- 在事务未提交时，entriesToAddOnCommit 属性，会暂存当前事务新产生的缓存 KV 对。
- 在事务提交时，entriesToAddOnCommit 属性，会同步到二级缓存 delegate 中。

#### 20.6.3.2 getObject ####
````
@Override
public Object getObject(Object key) {
    // <1> 从 delegate 中获取 key 对应的 value
    Object object = delegate.getObject(key);
    // <2> 如果不存在，则添加到 entriesMissedInCache 中
    if (object == null) {
        entriesMissedInCache.add(key);
    }
    // issue #146
    // <3> 如果 clearOnCommit 为 true ，表示处于持续清空状态，则返回 null
    if (clearOnCommit) {
        return null;
    // <4> 返回 value
    } else {
        return object;
    }
}
````

#### 20.6.3.3 putObject ####
putObject(Object key, Object object) 方法，暂存 KV 到 entriesToAddOnCommit 中。


#### 20.6.3.6 commit ####

````
public void commit() {
    // <1> 如果 clearOnCommit 为 true ，则清空 delegate 缓存
    if (clearOnCommit) {
        delegate.clear();
    }
    // 将 entriesToAddOnCommit、entriesMissedInCache 刷入 delegate 中
    flushPendingEntries();
    // 重置
    reset();
}
````



















-------