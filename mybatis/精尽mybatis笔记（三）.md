# 13 Binding 模块 #
在调用 SqlSession 相应方法执行数据库操作时，需要指定映射文件中定义的 SQL 节点，如果出现拼写错误，我们只能在运行时才能发现相应的异常。为了尽早发现这种错误，MyBatis 通过 Binding 模块，将用户自定义的 Mapper 接口与映射配置文件关联起来，系统可以通过调用自定义 Mapper 接口中的方法执行相应的 SQL 语句完成数据库操作，从而避免上述问题。

值得读者注意的是，开发人员无须编写自定义 Mapper 接口的实现，MyBatis 会自动为其创建动态代理对象。在有些场景中，自定义 Mapper 接口可以完全代替映射配置文件，但有的映射规则和 SQL 语句的定义还是写在映射配置文件中比较方便，例如动态 SQL 语句的定义。

## 13.1 MapperRegistry ##
org.apache.ibatis.binding.MapperRegistry ，Mapper 注册表。

### 13.1.1 构造方法 ###

````
/**
 * MyBatis Configuration 对象
 */
private final Configuration config;
/**
 * MapperProxyFactory 的映射
 * 
 * KEY：Mapper 接口
 */
private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();

public MapperRegistry(Configuration config) {
    this.config = config;
}
````

### 13.1.2 addMappers ###
addMappers(String packageName, ...) 方法，扫描指定包，并将符合的类，添加到 knownMappers 中。

````
public void addMappers(String packageName) {
    addMappers(packageName, Object.class);
}
    
public void addMappers(String packageName, Class<?> superType) {
    // <1> 扫描指定包下的指定类
    ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
    resolverUtil.find(new ResolverUtil.IsA(superType), packageName);
    Set<Class<? extends Class<?>>> mapperSet = resolverUtil.getClasses();
    // <2> 遍历，添加到 knownMappers 中
    for (Class<?> mapperClass : mapperSet) {
        addMapper(mapperClass);
    }
}
````

### 13.1.3 hasMapper ###
hasMapper(Class<T> type) 方法，判断是否有 Mapper 。

### 13.1.4 addMapper ###
addMapper(Class<T> type) 方法，添加到 knownMappers 中。

````
public <T> void addMapper(Class<T> type) {
    // <1> 判断，必须是接口。
    if (type.isInterface()) {
        // <2> 已经添加过，则抛出 BindingException 异常
        if (hasMapper(type)) {
            throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
        }
        boolean loadCompleted = false;
        try {
            // <3> 添加到 knownMappers 中
            knownMappers.put(type, new MapperProxyFactory<>(type));
            // <4> 解析 Mapper 的注解配置
            MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
            parser.parse();
            // <5> 标记加载完成
            loadCompleted = true;
        } finally {
            // <6> 若加载未完成，从 knownMappers 中移除
            if (!loadCompleted) {
                knownMappers.remove(type);
            }
        }
    }
}
````

### 13.1.5 getMapper ###
getMapper(Class<T> type, SqlSession sqlSession) 方法，获得 Mapper Proxy 对象。

````
public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    // <1> 从 knownMappers 中，获得 MapperProxyFactory 对象。
    final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
    // 不存在，则抛出 BindingException 异常
    if (mapperProxyFactory == null) {
        throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
    }
    // 调用 MapperProxyFactory#newInstance(SqlSession sqlSession) 方法，创建 Mapper Proxy 对象。
    try {
        return mapperProxyFactory.newInstance(sqlSession);
    } catch (Exception e) {
        throw new BindingException("Error getting mapper instance. Cause: " + e, e);
    }
}
````

## 13.2 MapperProxyFactory ##
org.apache.ibatis.binding.MapperProxyFactory ，Mapper Proxy 工厂类。

### 13.2.1 构造方法 ###

````
/**
 * Mapper 接口
 */
private final Class<T> mapperInterface;
/**
 * 方法与 MapperMethod 的映射
 */
private final Map<Method, MapperMethod> methodCache = new ConcurrentHashMap<>();

public MapperProxyFactory(Class<T> mapperInterface) {
    this.mapperInterface = mapperInterface;
}
````

### 13.2.2 newInstance ###
newInstance(...) 方法，创建 Mapper Proxy 对象。

基于 JDK Proxy 实现，而 InvocationHandler 参数是 MapperProxy 对象。

## 13.3 MapperProxy ##
org.apache.ibatis.binding.MapperProxy ，实现 InvocationHandler、Serializable 接口，Mapper Proxy 。关键是 java.lang.reflect.InvocationHandler 接口，

### 13.3.1 构造方法 ###

````
/**
 * SqlSession 对象
 */
private final SqlSession sqlSession;
/**
 * Mapper 接口
 */
private final Class<T> mapperInterface;
/**
 * 方法与 MapperMethod 的映射
 *
 * 从 {@link MapperProxyFactory#methodCache} 传递过来
 */
private final Map<Method, MapperMethod> methodCache;

public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethod> methodCache) {
    this.sqlSession = sqlSession;
    this.mapperInterface = mapperInterface;
    this.methodCache = methodCache;
}
````

### 13.3.2 invoke ###
invoke(Object proxy, Method method, Object[] args) 方法，调用方法。

````
@Override
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
        // <1> 如果是 Object 定义的方法，直接调用
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        // 调用 #isDefaultMethod((Method method) 方法，判断是否为 default 修饰的方法，若是，则调用 #invokeDefaultMethod(Object proxy, Method method, Object[] args) 方法，进行反射调用。
        } else if (isDefaultMethod(method)) {
            return invokeDefaultMethod(proxy, method, args);
        }
    } catch (Throwable t) {
        throw ExceptionUtil.unwrapThrowable(t);
    }
    // <3.1> 获得 MapperMethod 对象
    final MapperMethod mapperMethod = cachedMapperMethod(method);
    // <3.2> 执行 MapperMethod 方法
    return mapperMethod.execute(sqlSession, args);
}

private Object invokeDefaultMethod(Object proxy, Method method, Object[] args)
        throws Throwable {
    final Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
            .getDeclaredConstructor(Class.class, int.class);
    if (!constructor.isAccessible()) {
        constructor.setAccessible(true);
    }
    final Class<?> declaringClass = method.getDeclaringClass();
    return constructor
            .newInstance(declaringClass,
                    MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
                            | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC)
            .unreflectSpecial(method, declaringClass).bindTo(proxy).invokeWithArguments(args);
}

private boolean isDefaultMethod(Method method) {
    return (method.getModifiers()
            & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) == Modifier.PUBLIC
            && method.getDeclaringClass().isInterface();
}
````

## 13.4 MapperMethod ##

org.apache.ibatis.binding.MapperMethod ，Mapper 方法。在 Mapper 接口中，每个定义的方法，对应一个 MapperMethod 对象。

### 13.4.1 构造方法 ###

````
/**
 * SqlCommand 对象
 */
private final SqlCommand command;
/**
 * MethodSignature 对象
 */
private final MethodSignature method;

public MapperMethod(Class<?> mapperInterface, Method method, Configuration config) {
    this.command = new SqlCommand(config, mapperInterface, method);
    this.method = new MethodSignature(config, mapperInterface, method);
}
````

## 13.5 SqlCommand ##
SqlCommand ，是 MapperMethod 的内部静态类，SQL 命令。

### 13.5.1 构造方法 ###

````
/**
 * {@link MappedStatement#getId()}
 */
private final String name;
/**
 * SQL 命令类型
 */
private final SqlCommandType type;

public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
    final String methodName = method.getName();
    final Class<?> declaringClass = method.getDeclaringClass();
    // <1> 获得 MappedStatement 对象
    MappedStatement ms = resolveMappedStatement(mapperInterface, methodName, declaringClass,
            configuration);
    // <2> 找不到 MappedStatement
    if (ms == null) {
        // 如果有 @Flush 注解，则标记为 FLUSH 类型
        if (method.getAnnotation(Flush.class) != null) {
            name = null;
            type = SqlCommandType.FLUSH;
        } else { // 抛出 BindingException 异常，如果找不到 MappedStatement
            throw new BindingException("Invalid bound statement (not found): "
                    + mapperInterface.getName() + "." + methodName);
        }
    // <3> 找到 MappedStatement
    } else {
        // 获得 name
        name = ms.getId();
        // 获得 type
        type = ms.getSqlCommandType();
        if (type == SqlCommandType.UNKNOWN) { // 抛出 BindingException 异常，如果是 UNKNOWN 类型
            throw new BindingException("Unknown execution method for: " + name);
        }
    }
}
````
name 属性,对应 MappedStatement#getId() 方法获得的标识。实际上，就是 ${NAMESPACE_NAME}.${语句_ID}， 例如："org.apache.ibatis.autoconstructor.AutoConstructorMapper.getSubject2"
type 属性，SQL 命令类型。org.apache.ibatis.mapping.SqlCommandType 类

### 13.5.2 resolveMappedStatement ###
resolveMappedStatement(Class<?> mapperInterface, String methodName, Class<?> declaringClass, Configuration configuration) 方法，获得 MappedStatement 对象。

````
private MappedStatement resolveMappedStatement(Class<?> mapperInterface, String methodName,
                                               Class<?> declaringClass, Configuration configuration) {
    // <1> 获得编号。这个编号，和我们上文提到的 ${NAMESPACE_NAME}.${语句_ID} 。
    String statementId = mapperInterface.getName() + "." + methodName;
    // <2> 通过 Configuration#hasStatement(String statementId) 方法，判断是否有 MappedStatement 。如果有，则调用 Configuration#getMappedStatement(String statementId) 方法，获得 MappedStatement 对象。关于 Configuration ，我们在后续的文章中，详细解析。在这里，胖友只需要知道，Configuration 里缓存了所有的 MappedStatement ，并且每一个 XML 里声明的例如 <select /> 或者 <update /> 等等，都对应一个 MappedStatement 对象。
    if (configuration.hasStatement(statementId)) {
        return configuration.getMappedStatement(statementId);
    // 如果没有，并且当前方法就是 declaringClass 声明的，则说明真的找不到。
    } else if (mapperInterface.equals(declaringClass)) {
        return null;
    }
    // 遍历父接口，递归继续获得 MappedStatement 对象。因为，该该方法定义在父接口中。
    for (Class<?> superInterface : mapperInterface.getInterfaces()) {
        if (declaringClass.isAssignableFrom(superInterface)) {
            MappedStatement ms = resolveMappedStatement(superInterface, methodName,
                    declaringClass, configuration);
            if (ms != null) {
                return ms;
            }
        }
    }
    // 真的找不到，返回 null
    return null;
}
````

## 13.6 MethodSignature ##
MethodSignature ，是 MapperMethod 的内部静态类，方法签名。

### 13.6.1 构造方法 ###

````
/**
 * 返回类型是否为集合
 */
private final boolean returnsMany;
/**
 * 返回类型是否为 Map
 */
private final boolean returnsMap;
/**
 * 返回类型是否为 void
 */
private final boolean returnsVoid;
/**
 * 返回类型是否为 {@link org.apache.ibatis.cursor.Cursor}
 */
private final boolean returnsCursor;
/**
 * 返回类型是否为 {@link java.util.Optional}
 */
private final boolean returnsOptional;
/**
 * 返回类型
 */
private final Class<?> returnType;
/**
 * 返回方法上的 {@link MapKey#value()} ，前提是返回类型为 Map
 */
private final String mapKey;
/**
 * 获得 {@link ResultHandler} 在方法参数中的位置。
 *
 * 如果为 null ，说明不存在这个类型
 */
private final Integer resultHandlerIndex;
/**
 * 获得 {@link RowBounds} 在方法参数中的位置。
 *
 * 如果为 null ，说明不存在这个类型
 */
private final Integer rowBoundsIndex;
/**
 * ParamNameResolver 对象
 */
private final ParamNameResolver paramNameResolver;

public MethodSignature(Configuration configuration, Class<?> mapperInterface, Method method) {
    // 初始化 returnType 属性
    Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, mapperInterface);
    if (resolvedReturnType instanceof Class<?>) { // 普通类
        this.returnType = (Class<?>) resolvedReturnType;
    } else if (resolvedReturnType instanceof ParameterizedType) { // 泛型
        this.returnType = (Class<?>) ((ParameterizedType) resolvedReturnType).getRawType();
    } else { // 内部类等等
        this.returnType = method.getReturnType();
    }
    // 初始化 returnsVoid 属性
    this.returnsVoid = void.class.equals(this.returnType);
    // 初始化 returnsMany 属性
    this.returnsMany = configuration.getObjectFactory().isCollection(this.returnType) || this.returnType.isArray();
    // 初始化 returnsCursor 属性
    this.returnsCursor = Cursor.class.equals(this.returnType);
    // 初始化 returnsOptional 属性
    this.returnsOptional = Optional.class.equals(this.returnType);
    // <1> 初始化 mapKey
    this.mapKey = getMapKey(method);
    // 初始化 returnsMap
    this.returnsMap = this.mapKey != null;
    // <2> 初始化 rowBoundsIndex、resultHandlerIndex
    this.rowBoundsIndex = getUniqueParamIndex(method, RowBounds.class);
    this.resultHandlerIndex = getUniqueParamIndex(method, ResultHandler.class);
    // 初始化 paramNameResolver
    this.paramNameResolver = new ParamNameResolver(configuration, method);
}
````

### 13.6.2 convertArgsToSqlCommandParam ###
convertArgsToSqlCommandParam(Object[] args) 方法，获得 SQL 通用参数。

----

# 14 MyBatis 初始化（一）之加载 mybatis-config #

在 MyBatis 初始化过程中，会加载 mybatis-config.xml 配置文件、映射配置文件以及 Mapper 接口中的注解信息，解析后的配置信息会形成相应的对象并保存到 Configuration 对象中。例如：
````
<resultMap>节点(即 ResultSet 的映射规则) 会被解析成 ResultMap 对象。
<result> 节点(即属性映射)会被解析成 ResultMapping 对象。
之后，利用该 Configuration 对象创建 SqlSessionFactory对象。待 MyBatis 初始化之后，开发人员可以通过初始化得到 SqlSessionFactory 创建 SqlSession 对象并完成数据库操作。
````

对应 builder 模块，为配置解析过程
对应 mapping 模块，主要为 SQL 操作解析后的映射。

MyBatis 的初始化流程的入口是 SqlSessionFactoryBuilder 的 #build(Reader reader, String environment, Properties properties) 方法

## 14.1 BaseBuilder ##
org.apache.ibatis.builder.BaseBuilder ，基础构造器抽象类，为子类提供通用的工具类。

### 14.1.1 构造方法 ###
````
/**
 * MyBatis Configuration 对象
 */
protected final Configuration configuration;
protected final TypeAliasRegistry typeAliasRegistry;
protected final TypeHandlerRegistry typeHandlerRegistry;

public BaseBuilder(Configuration configuration) {
    this.configuration = configuration;
    this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
    this.typeHandlerRegistry = this.configuration.getTypeHandlerRegistry();
}
````
configuration 属性，MyBatis Configuration 对象。XML 和注解中解析到的配置，最终都会设置到 org.apache.ibatis.session.Configuration 中。

### 14.1.2 parseExpression ###
parseExpression(String regex, String defaultValue) 方法，创建正则表达式。


### 14.1.3 xxxValueOf ###
xxxValueOf(...) 方法，将字符串转换成对应的数据类型的值。

### 14.1.4 resolveJdbcType ###
resolveJdbcType(String alias) 方法，解析对应的 JdbcType 类型。
````
protected JdbcType resolveJdbcType(String alias) {
    if (alias == null) {
        return null;
    }
    try {
        return JdbcType.valueOf(alias);
    } catch (IllegalArgumentException e) {
        throw new BuilderException("Error resolving JdbcType. Cause: " + e, e);
    }
}
````

### 14.1.5 resolveResultSetType ###
resolveResultSetType(String alias) 方法，解析对应的 ResultSetType 类型。

````
protected ResultSetType resolveResultSetType(String alias) {
    if (alias == null) {
        return null;
    }
    try {
        return ResultSetType.valueOf(alias);
    } catch (IllegalArgumentException e) {
        throw new BuilderException("Error resolving ResultSetType. Cause: " + e, e);
    }
}
````
### 14.1.6 resolveParameterMode ###
resolveParameterMode(String alias) 方法，解析对应的 ParameterMode 类型。

### 14.1.7 createInstance ###
createInstance(String alias) 方法，创建指定对象。

### 14.1.8 resolveTypeHandler ###
resolveTypeHandler(Class<?> javaType, String typeHandlerAlias) 方法，从 typeHandlerRegistry 中获得或创建对应的 TypeHandler 对象。

## 14.2 XMLConfigBuilder ##
org.apache.ibatis.builder.xml.XMLConfigBuilder ，继承 BaseBuilder 抽象类，XML 配置构建器，主要负责解析 mybatis-config.xml 配置文件。

### 14.2.1 构造方法 ###
````
/**
 * 是否已解析
 */
private boolean parsed;
/**
 * 基于 Java XPath 解析器
 */
private final XPathParser parser;
/**
 * 环境
 */
private String environment;
/**
 * ReflectorFactory 对象
 */
private final ReflectorFactory localReflectorFactory = new DefaultReflectorFactory();
private XMLConfigBuilder(XPathParser parser, String environment, Properties props) {
    // <1> 创建 Configuration 对象
    super(new Configuration());
    ErrorContext.instance().resource("SQL Mapper Configuration");
    // <2> 设置 Configuration 的 variables 属性
    this.configuration.setVariables(props);
    this.parsed = false;
    this.environment = environment;
    this.parser = parser;
}
````

### 14.2.2 parse ###
parse() 方法，解析 XML 成 Configuration 对象。


### 14.2.3 parseConfiguration ###
parseConfiguration(XNode root) 方法，解析 <configuration /> 节点。

````
private void parseConfiguration(XNode root) {
    try {
        // <1> 解析 <properties /> 标签
        propertiesElement(root.evalNode("properties"));
        // <2> 解析 <settings /> 标签
        Properties settings = settingsAsProperties(root.evalNode("settings"));
        // <3> 加载自定义 VFS 实现类
        loadCustomVfs(settings);
        // <4> 解析 <typeAliases /> 标签
        typeAliasesElement(root.evalNode("typeAliases"));
        // <5> 解析 <plugins /> 标签
        pluginElement(root.evalNode("plugins"));
        // <6> 解析 <objectFactory /> 标签
        objectFactoryElement(root.evalNode("objectFactory"));
        // <7> 解析 <objectWrapperFactory /> 标签
        objectWrapperFactoryElement(root.evalNode("objectWrapperFactory"));
        // <8> 解析 <reflectorFactory /> 标签
        reflectorFactoryElement(root.evalNode("reflectorFactory"));
        // <9> 赋值 <settings /> 到 Configuration 属性
        settingsElement(settings);
        // read it after objectFactory and objectWrapperFactory issue #631
        // <10> 解析 <environments /> 标签
        environmentsElement(root.evalNode("environments"));
        // <11> 解析 <databaseIdProvider /> 标签
        databaseIdProviderElement(root.evalNode("databaseIdProvider"));
        // <12> 解析 <typeHandlers /> 标签
        typeHandlerElement(root.evalNode("typeHandlers"));
        // <13> 解析 <mappers /> 标签
        mapperElement(root.evalNode("mappers"));
    } catch (Exception e) {
        throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
    }
}
````

#### 14.2.3.1 propertiesElement ####

propertiesElement(XNode context) 方法，解析 <properties /> 节点。大体逻辑如下：
1. 解析 <properties /> 标签，成 Properties 对象。
2. 覆盖 configuration 中的 Properties 对象到上面的结果。
3. 设置结果到 parser 和 configuration 中。

#### 14.2.3.2 settingsAsProperties ####
settingsElement(Properties props) 方法，将 <setting /> 标签解析为 Properties 对象。

#### 14.2.3.3 loadCustomVfs ####
loadCustomVfs(Properties settings) 方法，加载自定义 VFS 实现类。

#### 14.2.3.4 typeAliasesElement ####
typeAliasesElement(XNode parent) 方法，解析 <typeAliases /> 标签，将配置类注册到 typeAliasRegistry 中。

#### 14.2.3.5 pluginElement ####
pluginElement(XNode parent) 方法，解析 <plugins /> 标签，添加到 Configuration#interceptorChain 中。

#### 14.2.3.6 objectFactoryElement ####
objectFactoryElement(XNode parent) 方法，解析 <objectFactory /> 节点。

#### 14.2.3.7 objectWrapperFactoryElement ####
objectWrapperFactoryElement(XNode context) 方法，解析 <objectWrapperFactory /> 节点。

#### 14.2.3.8 reflectorFactoryElement ####
reflectorFactoryElement(XNode parent) 方法，解析 <reflectorFactory /> 节点。

#### 14.2.3.9 settingsElement ####
settingsElement(Properties props) 方法，赋值 <settings /> 到 Configuration 属性。

#### 14.2.3.10 environmentsElement ####
environmentsElement(XNode context) 方法，解析 <environments /> 标签。

#### 14.2.3.11 databaseIdProviderElement ####
databaseIdProviderElement(XNode context) 方法，解析 <databaseIdProvider /> 标签。

#### 14.2.3.12 typeHandlerElement ####
typeHandlerElement(XNode parent) 方法，解析 <typeHandlers /> 标签。

#### 14.2.3.13 mapperElement ####
mapperElement(XNode context) 方法，解析 <mappers /> 标签。

----

# 15 MyBatis 初始化（二）之加载 Mapper 映射配置文件 #

mapper文件的解析结果映射:

![](/picture/mybatis-mapper-map-result.png)

mybatis初始化流程:
![](/picture/mybatis-init-flow.png)

## 15.1 XMLMapperBuilder ##
org.apache.ibatis.builder.xml.XMLMapperBuilder ，继承 BaseBuilder 抽象类，Mapper XML 配置构建器，主要负责解析 Mapper 映射配置文件。

### 15.1.1 构造方法 ###
````
/**
 * 基于 Java XPath 解析器
 */
private final XPathParser parser;
/**
 * Mapper 构造器助手
 * builderAssistant 属性，MapperBuilderAssistant 对象，是 XMLMapperBuilder 和 MapperAnnotationBuilder 的小助手，提供了一些公用的方法，例如创建 ParameterMap、MappedStatement 对象等等。关于 MapperBuilderAssistant 类
 */
private final MapperBuilderAssistant builderAssistant;
/**
 * 可被其他语句引用的可重用语句块的集合
 *
 * 例如：<sql id="userColumns"> ${alias}.id,${alias}.username,${alias}.password </sql>
 */
private final Map<String, XNode> sqlFragments;
/**
 * 资源引用的地址
 */
private final String resource;

private XMLMapperBuilder(XPathParser parser, Configuration configuration, String resource, Map<String, XNode> sqlFragments) {
    super(configuration);
    // 创建 MapperBuilderAssistant 对象
    this.builderAssistant = new MapperBuilderAssistant(configuration, resource);
    this.parser = parser;
    this.sqlFragments = sqlFragments;
    this.resource = resource;
}
````

### 15.1.2 parse ###
parse() 方法，解析 Mapper XML 配置文件。
````
public void parse() {
    // <1> 判断当前 Mapper 是否已经加载过
    if (!configuration.isResourceLoaded(resource)) {
        // <2> 解析 `<mapper />` 节点
        configurationElement(parser.evalNode("/mapper"));
        // <3> 标记该 Mapper 已经加载过
        configuration.addLoadedResource(resource);
        // <4> 绑定 Mapper
        bindMapperForNamespace();
    }

    // <5> 解析待定的 <resultMap /> 节点
    parsePendingResultMaps();
    // <6> 解析待定的 <cache-ref /> 节点
    parsePendingCacheRefs();
    // <7> 解析待定的 SQL 语句的节点
    parsePendingStatements();
}
````

### 15.1.3 configurationElement ###
configurationElement(XNode context) 方法，解析 <mapper /> 节点。

````
private void configurationElement(XNode context) {
    try {
        // <1> 获得 namespace 属性
        String namespace = context.getStringAttribute("namespace");
        if (namespace == null || namespace.equals("")) {
            throw new BuilderException("Mapper's namespace cannot be empty");
        }
        // <1> 设置 namespace 属性
        builderAssistant.setCurrentNamespace(namespace);
        // <2> 解析 <cache-ref /> 节点
        cacheRefElement(context.evalNode("cache-ref"));
        // <3> 解析 <cache /> 节点
        cacheElement(context.evalNode("cache"));
        // 已废弃！老式风格的参数映射。内联参数是首选,这个元素可能在将来被移除，这里不会记录。
        parameterMapElement(context.evalNodes("/mapper/parameterMap"));
        // <4> 解析 <resultMap /> 节点们
        resultMapElements(context.evalNodes("/mapper/resultMap"));
        // <5> 解析 <sql /> 节点们
        sqlElement(context.evalNodes("/mapper/sql"));
        // <6> 解析 <select /> <insert /> <update /> <delete /> 节点们
        buildStatementFromContext(context.evalNodes("select|insert|update|delete"));
    } catch (Exception e) {
        throw new BuilderException("Error parsing Mapper XML. The XML location is '" + resource + "'. Cause: " + e, e);
    }
}
````

#### 15.1.3.1 cacheElement ####

cacheRefElement(XNode context) 方法，解析 <cache-ref /> 节点。

例如：
````
<cache-ref namespace="com.someone.application.data.SomeMapper"/>
````
#### 15.1.3.2 cacheElement ####
cacheElement(XNode context) 方法，解析 cache /> 标签。

#### 15.1.3.3 resultMapElements ####

resultMapElements(List<XNode> list) 方法，解析 <resultMap /> 节点们。

````
// 解析 <resultMap /> 节点们
private void resultMapElements(List<XNode> list) throws Exception {
    // 遍历 <resultMap /> 节点们
    for (XNode resultMapNode : list) {
        try {
            // 处理单个 <resultMap /> 节点
            resultMapElement(resultMapNode);
        } catch (IncompleteElementException e) {
            // ignore, it will be retried
        }
    }
}

// 解析 <resultMap /> 节点
private ResultMap resultMapElement(XNode resultMapNode) throws Exception {
    return resultMapElement(resultMapNode, Collections.<ResultMapping>emptyList());
}

// 解析 <resultMap /> 节点
private ResultMap resultMapElement(XNode resultMapNode, List<ResultMapping> additionalResultMappings) throws Exception {
    ErrorContext.instance().activity("processing " + resultMapNode.getValueBasedIdentifier());
    // <1> 获得 id 属性
    String id = resultMapNode.getStringAttribute("id",
            resultMapNode.getValueBasedIdentifier());
    // <1> 获得 type 属性
    String type = resultMapNode.getStringAttribute("type",
            resultMapNode.getStringAttribute("ofType",
                    resultMapNode.getStringAttribute("resultType",
                            resultMapNode.getStringAttribute("javaType"))));
    // <1> 获得 extends 属性
    String extend = resultMapNode.getStringAttribute("extends");
    // <1> 获得 autoMapping 属性
    Boolean autoMapping = resultMapNode.getBooleanAttribute("autoMapping");
    // <1> 解析 type 对应的类
    Class<?> typeClass = resolveClass(type);
    Discriminator discriminator = null;
    // <2> 创建 ResultMapping 集合
    List<ResultMapping> resultMappings = new ArrayList<>();
    resultMappings.addAll(additionalResultMappings);
    // <2> 遍历 <resultMap /> 的子节点
    List<XNode> resultChildren = resultMapNode.getChildren();
    for (XNode resultChild : resultChildren) {
        // <2.1> 处理 <constructor /> 节点
        if ("constructor".equals(resultChild.getName())) {
            processConstructorElement(resultChild, typeClass, resultMappings);
        // <2.2> 处理 <discriminator /> 节点
        } else if ("discriminator".equals(resultChild.getName())) {
            discriminator = processDiscriminatorElement(resultChild, typeClass, resultMappings);
        // <2.3> 处理其它节点
        } else {
            List<ResultFlag> flags = new ArrayList<>();
            if ("id".equals(resultChild.getName())) {
                flags.add(ResultFlag.ID);
            }
            resultMappings.add(buildResultMappingFromContext(resultChild, typeClass, flags));
        }
    }
    // <3> 创建 ResultMapResolver 对象，执行解析
    ResultMapResolver resultMapResolver = new ResultMapResolver(builderAssistant, id, typeClass, extend, discriminator, resultMappings, autoMapping);
    try {
        return resultMapResolver.resolve();
    } catch (IncompleteElementException e) {
        // <4> 解析失败，添加到 configuration 中
        configuration.addIncompleteResultMap(resultMapResolver);
        throw e;
    }
}
````

#### 15.1.3.4 sqlElement ####
sqlElement(List<XNode> list) 方法，解析 <sql /> 节点们。

#### 15.1.3.5 buildStatementFromContext ####
buildStatementFromContext(List<XNode> list) 方法，解析 <select />、<insert />、<update />、<delete /> 节点们。

### 15.1.4 bindMapperForNamespace ###
bindMapperForNamespace() 方法，绑定 Mapper 。

### 15.1.5 parsePendingXXX ###

三个方法的逻辑思路基本一致：1）获得对应的集合；2）遍历集合，执行解析；3）执行成功，则移除出集合；4）执行失败，忽略异常。

## 15.2 MapperBuilderAssistant ##
org.apache.ibatis.builder.MapperBuilderAssistant ，继承 BaseBuilder 抽象类，Mapper 构造器的小助手，提供了一些公用的方法，例如创建 ParameterMap、MappedStatement 对象等等。

### 15.2.1 构造方法 ###
````
/**
 * 当前 Mapper 命名空间
 */
private String currentNamespace;
/**
 * 资源引用的地址
 */
private final String resource;
/**
 * 当前 Cache 对象
 */
private Cache currentCache;
/**
 * 是否未解析成功 Cache 引用
 */
private boolean unresolvedCacheRef; // issue #676

public MapperBuilderAssistant(Configuration configuration, String resource) {
    super(configuration);
    ErrorContext.instance().resource(resource);
    this.resource = resource;
}
````

### 15.2.2 setCurrentNamespace ###
setCurrentNamespace(String currentNamespace) 方法，设置 currentNamespace 属性。

### 15.2.3 useCacheRef ###
useCacheRef(String namespace) 方法，获得指向的 Cache 对象。如果获得不到，则抛出 IncompleteElementException 异常。








----