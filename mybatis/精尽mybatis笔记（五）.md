# 23 SQL 执行（四）之 ResultSetHandler #
SQL 执行后，响应的结果集 ResultSet 的处理，涉及 executor/resultset、executor/result、cursor 包。
核心类是 ResultSetHandler 接口及其实现类 DefaultResultSetHandler 。在它的代码逻辑中，会调用类图中的其它类，实现将查询结果的 ResultSet ，转换成映射的对应结果。

## 23.1 ResultSetWrapper ##
org.apache.ibatis.executor.resultset.ResultSetWrapper ，java.sql.ResultSet 的 包装器，可以理解成 ResultSet 的工具类，提供给 DefaultResultSetHandler 使用。

### 23.1.1 构造方法 ###
````
/**
 * ResultSet 对象
 */
private final ResultSet resultSet;
private final TypeHandlerRegistry typeHandlerRegistry;
/**
 * 字段的名字的数组
 */
private final List<String> columnNames = new ArrayList<>();
/**
 * 字段的 Java Type 的数组
 */
private final List<String> classNames = new ArrayList<>();
/**
 * 字段的 JdbcType 的数组
 */
private final List<JdbcType> jdbcTypes = new ArrayList<>();
private final Map<String, Map<Class<?>, TypeHandler<?>>> typeHandlerMap = new HashMap<>();
private final Map<String, List<String>> mappedColumnNamesMap = new HashMap<>();
private final Map<String, List<String>> unMappedColumnNamesMap = new HashMap<>();

public ResultSetWrapper(ResultSet rs, Configuration configuration) throws SQLException {
    this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
    this.resultSet = rs;
    // <1> 遍历 ResultSetMetaData 的字段们，解析出 columnNames、jdbcTypes、classNames 属性
    final ResultSetMetaData metaData = rs.getMetaData();
    final int columnCount = metaData.getColumnCount();
    for (int i = 1; i <= columnCount; i++) {
        columnNames.add(configuration.isUseColumnLabel() ? metaData.getColumnLabel(i) : metaData.getColumnName(i));
        jdbcTypes.add(JdbcType.forCode(metaData.getColumnType(i)));
        classNames.add(metaData.getColumnClassName(i));
    }
}
````

### 23.1.2 getTypeHandler ###

````
/**
 * TypeHandler 的映射
 * KEY1：字段的名字
 * KEY2：Java 属性类型
 */
private final Map<String, Map<Class<?>, TypeHandler<?>>> typeHandlerMap = new HashMap<>();

/**
 * 获得指定字段名的指定 JavaType 类型的 TypeHandler 对象
 * @param propertyType JavaType
 * @param columnName 执行字段
 * @return TypeHandler 对象
 */
public TypeHandler<?> getTypeHandler(Class<?> propertyType, String columnName) {
    TypeHandler<?> handler = null;
    // <1> 先从缓存的 typeHandlerMap 中，获得指定字段名的指定 JavaType 类型的 TypeHandler 对象
    Map<Class<?>, TypeHandler<?>> columnHandlers = typeHandlerMap.get(columnName);
    if (columnHandlers == null) {
        columnHandlers = new HashMap<>();
        typeHandlerMap.put(columnName, columnHandlers);
    } else {
        handler = columnHandlers.get(propertyType);
    }
    // <2> 如果获取不到，则进行查找
    if (handler == null) {
        // <2> 获得 JdbcType 类型
        JdbcType jdbcType = getJdbcType(columnName);
        // <2> 获得 TypeHandler 对象
        handler = typeHandlerRegistry.getTypeHandler(propertyType, jdbcType);
        // Replicate logic of UnknownTypeHandler#resolveTypeHandler
        // See issue #59 comment 10
        // <3> 如果获取不到，则再次进行查找
        if (handler == null || handler instanceof UnknownTypeHandler) {
            // <3> 使用 classNames 中的类型，进行继续查找 TypeHandler 对象
            final int index = columnNames.indexOf(columnName);
            final Class<?> javaType = resolveClass(classNames.get(index));
            if (javaType != null && jdbcType != null) {
                handler = typeHandlerRegistry.getTypeHandler(javaType, jdbcType);
            } else if (javaType != null) {
                handler = typeHandlerRegistry.getTypeHandler(javaType);
            } else if (jdbcType != null) {
                handler = typeHandlerRegistry.getTypeHandler(jdbcType);
            }
        }
        // <4> 如果获取不到，则使用 ObjectTypeHandler 对象
        if (handler == null || handler instanceof UnknownTypeHandler) {
            handler = new ObjectTypeHandler();
        }
        // <5> 缓存到 typeHandlerMap 中
        columnHandlers.put(propertyType, handler);
    }
    return handler;
}
````

### 23.1.3 loadMappedAndUnmappedColumnNames ###

loadMappedAndUnmappedColumnNames(ResultMap resultMap, String columnPrefix) 方法，初始化有 mapped 和无 mapped的字段的名字数组。


## 23.2 ResultSetHandler ##
org.apache.ibatis.executor.resultset.ResultSetHandler ，java.sql.ResultSet 处理器接口。

````
public interface ResultSetHandler {

    /**
     * 处理 {@link java.sql.ResultSet} 成映射的对应的结果
     * @param stmt Statement 对象
     * @param <E> 泛型
     * @return 结果数组
     */
    <E> List<E> handleResultSets(Statement stmt) throws SQLException;

    /**
     * 处理 {@link java.sql.ResultSet} 成 Cursor 对象
     * @param stmt Statement 对象
     * @param <E> 泛型
     * @return Cursor 对象
     */
    <E> Cursor<E> handleCursorResultSets(Statement stmt) throws SQLException;

    // 和存储过程相关
    void handleOutputParameters(CallableStatement cs) throws SQLException;

}
````

### 23.2.1 DefaultResultSetHandler ###
org.apache.ibatis.executor.resultset.DefaultResultSetHandler ，实现 ResultSetHandler 接口，默认的 ResultSetHandler 实现类。

#### 23.2.1.1 构造方法 ####

````
private static final Object DEFERED = new Object();

private final Executor executor;
private final Configuration configuration;
private final MappedStatement mappedStatement;
private final RowBounds rowBounds;
private final ParameterHandler parameterHandler;
/**
 * 用户指定的用于处理结果的处理器。
 *
 * 一般情况下，不设置
 */
private final ResultHandler<?> resultHandler;
private final BoundSql boundSql;
private final TypeHandlerRegistry typeHandlerRegistry;
private final ObjectFactory objectFactory;
private final ReflectorFactory reflectorFactory;

// nested resultmaps
private final Map<CacheKey, Object> nestedResultObjects = new HashMap<>();
private final Map<String, Object> ancestorObjects = new HashMap<>();
private Object previousRowValue;

// multiple resultsets
// 存储过程相关的多 ResultSet 涉及的属性，可以暂时忽略
private final Map<String, ResultMapping> nextResultMaps = new HashMap<>();
private final Map<CacheKey, List<PendingRelation>> pendingRelations = new HashMap<>();

// Cached Automappings
/**
 * 自动映射的缓存
 *
 * KEY：{@link ResultMap#getId()} + ":" +  columnPrefix
 *
 * @see #createRowKeyForUnmappedProperties(ResultMap, ResultSetWrapper, CacheKey, String) 
 */
private final Map<String, List<UnMappedColumnAutoMapping>> autoMappingsCache = new HashMap<>();

// temporary marking flag that indicate using constructor mapping (use field to reduce memory usage)
/**
 * 是否使用构造方法创建该结果对象
 */
private boolean useConstructorMappings;

public DefaultResultSetHandler(Executor executor, MappedStatement mappedStatement, ParameterHandler parameterHandler, ResultHandler<?> resultHandler, BoundSql boundSql, RowBounds rowBounds) {
    this.executor = executor;
    this.configuration = mappedStatement.getConfiguration();
    this.mappedStatement = mappedStatement;
    this.rowBounds = rowBounds;
    this.parameterHandler = parameterHandler;
    this.boundSql = boundSql;
    this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
    this.objectFactory = configuration.getObjectFactory();
    this.reflectorFactory = configuration.getReflectorFactory();
    this.resultHandler = resultHandler;
}
````

#### 23.2.1.2 handleResultSets ####
handleResultSets(Statement stmt) 方法，处理 java.sql.ResultSet 结果集，转换成映射的对应结果。

````
@Override
public List<Object> handleResultSets(Statement stmt) throws SQLException {
    ErrorContext.instance().activity("handling results").object(mappedStatement.getId());

    // <1> 多 ResultSet 的结果集合，每个 ResultSet 对应一个 Object 对象。而实际上，每个 Object 是 List<Object> 对象。
    // 在不考虑存储过程的多 ResultSet 的情况，普通的查询，实际就一个 ResultSet ，也就是说，multipleResults 最多就一个元素。
    final List<Object> multipleResults = new ArrayList<>();

    int resultSetCount = 0;
    // <2> 获得首个 ResultSet 对象，并封装成 ResultSetWrapper 对象
    ResultSetWrapper rsw = getFirstResultSet(stmt);

    // <3> 获得 ResultMap 数组
    // 在不考虑存储过程的多 ResultSet 的情况，普通的查询，实际就一个 ResultSet ，也就是说，resultMaps 就一个元素。
    List<ResultMap> resultMaps = mappedStatement.getResultMaps();
    int resultMapCount = resultMaps.size();
    validateResultMapsCount(rsw, resultMapCount); // <3.1> 校验
    while (rsw != null && resultMapCount > resultSetCount) {
        // <4.1> 获得 ResultMap 对象
        ResultMap resultMap = resultMaps.get(resultSetCount);
        // <4.2> 处理 ResultSet ，将结果添加到 multipleResults 中
        handleResultSet(rsw, resultMap, multipleResults, null);
        // <4.3> 获得下一个 ResultSet 对象，并封装成 ResultSetWrapper 对象
        rsw = getNextResultSet(stmt);
        // <4.4> 清理
        cleanUpAfterHandlingResultSet();
        // resultSetCount ++
        resultSetCount++;
    }

    // <5> 因为 `mappedStatement.resultSets` 只在存储过程中使用，本系列暂时不考虑，忽略即可
    String[] resultSets = mappedStatement.getResultSets();
    if (resultSets != null) {
        while (rsw != null && resultSetCount < resultSets.length) {
            ResultMapping parentMapping = nextResultMaps.get(resultSets[resultSetCount]);
            if (parentMapping != null) {
                String nestedResultMapId = parentMapping.getNestedResultMapId();
                ResultMap resultMap = configuration.getResultMap(nestedResultMapId);
                handleResultSet(rsw, resultMap, null, parentMapping);
            }
            rsw = getNextResultSet(stmt);
            cleanUpAfterHandlingResultSet();
            resultSetCount++;
        }
    }

    // <6> 如果是 multipleResults 单元素，则取首元素返回
    return collapseSingleResultList(multipleResults);
}
````









------