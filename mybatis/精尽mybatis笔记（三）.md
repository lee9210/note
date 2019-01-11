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
sqlElement(List<XNode> list) 方法，解析  <sql /> 节点们。

#### 15.1.3.5 buildStatementFromContext ####
````
buildStatementFromContext(List<XNode> list) 方法，解析 <select />、<insert />、<update />、<delete /> 节点们。
````
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

### 15.2.4 useNewCache ###
useNewCache(Class<? extends Cache> typeClass, Class<? extends Cache> evictionClass, Long flushInterval, Integer size, boolean readWrite, boolean blocking, Properties props) 方法，创建 Cache 对象。

````
public Cache useNewCache(Class<? extends Cache> typeClass,
                         Class<? extends Cache> evictionClass,
                         Long flushInterval,
                         Integer size,
                         boolean readWrite,
                         boolean blocking,
                         Properties props) {
    // <1> 创建 Cache 对象
    Cache cache = new CacheBuilder(currentNamespace)
            .implementation(valueOrDefault(typeClass, PerpetualCache.class))
            .addDecorator(valueOrDefault(evictionClass, LruCache.class))
            .clearInterval(flushInterval)
            .size(size)
            .readWrite(readWrite)
            .blocking(blocking)
            .properties(props)
            .build();
    // <2> 添加到 configuration 的 caches 中
    configuration.addCache(cache);
    // <3> 赋值给 currentCache
    currentCache = cache;
    return cache;
}
````

#### 15.2.4.1 CacheBuilder ####
org.apache.ibatis.mapping.CacheBuilder ，Cache 构造器。基于装饰者设计模式，进行 Cache 对象的构造。

### 15.2.5 buildResultMapping ###
buildResultMapping(Class<?> resultType, String property, String column,Class<?> javaType, JdbcType jdbcType, String nestedSelect, String nestedResultMap, String notNullColumn, String columnPrefix, Class<? extends TypeHandler<?>> typeHandler, List<ResultFlag> flags, String resultSet, String foreignColumn, boolean lazy) 方法，构造 ResultMapping 对象。

#### 15.2.5.1 parseCompositeColumnName ####
parseCompositeColumnName(String columnName) 方法，解析组合字段名称成 ResultMapping 集合。

````
来自数据库的列名,或重命名的列标签。这和通常传递给 resultSet.getString(columnName)方法的字符串是相同的。 column 注 意 : 要 处 理 复 合 主 键 , 你 可 以 指 定 多 个 列 名 通 过 column= ” {prop1=col1,prop2=col2} ” 这种语法来传递给嵌套查询语 句。这会引起 prop1 和 prop2 以参数对象形式来设置给目标嵌套查询语句。
````
#### 15.2.5.2 applyCurrentNamespace ####
applyCurrentNamespace(String base, boolean isReference) 方法，拼接命名空间。
通过这样的方式，生成唯一在的标识。

#### 15.2.5.3 parseMultipleColumnNames ####
parseMultipleColumnNames(String notNullColumn) 方法，将字符串解析成集合。

#### 15.2.5.4 ResultMapping ####
org.apache.ibatis.mapping.ResultMapping ，ResultMap 中的每一条结果字段的映射。

### 15.2.6 buildDiscriminator ###
buildDiscriminator(Class<?> resultType, String column, Class<?> javaType, JdbcType jdbcType, Class<? extends TypeHandler<?>> typeHandler, Map<String, String> discriminatorMap) 方法，构建 Discriminator 对象。

### 15.2.7 addResultMap ###
addResultMap(String id, Class<?> type, String extend, Discriminator discriminator, List<ResultMapping> resultMappings, Boolean autoMapping) 方法，创建 ResultMap 对象，并添加到 Configuration 中。

````
public ResultMap addResultMap(
        String id,
        Class<?> type,
        String extend,
        Discriminator discriminator,
        List<ResultMapping> resultMappings,
        Boolean autoMapping) {
    // <1> 获得 ResultMap 编号，即格式为 `${namespace}.${id}` 。
    id = applyCurrentNamespace(id, false);
    // <2.1> 获取完整的 extend 属性，即格式为 `${namespace}.${extend}` 。从这里的逻辑来看，貌似只能自己 namespace 下的 ResultMap 。
    extend = applyCurrentNamespace(extend, true);

    // <2.2> 如果有父类，则将父类的 ResultMap 集合，添加到 resultMappings 中。
    if (extend != null) {
        // <2.2.1> 获得 extend 对应的 ResultMap 对象。如果不存在，则抛出 IncompleteElementException 异常
        if (!configuration.hasResultMap(extend)) {
            throw new IncompleteElementException("Could not find a parent resultmap with id '" + extend + "'");
        }
        ResultMap resultMap = configuration.getResultMap(extend);
        // 获取 extend 的 ResultMap 对象的 ResultMapping 集合，并移除 resultMappings
        List<ResultMapping> extendedResultMappings = new ArrayList<>(resultMap.getResultMappings());
        extendedResultMappings.removeAll(resultMappings);
        // Remove parent constructor if this resultMap declares a constructor.
        // 判断当前的 resultMappings 是否有构造方法，如果有，则从 extendedResultMappings 移除所有的构造类型的 ResultMapping 们
        boolean declaresConstructor = false;
        for (ResultMapping resultMapping : resultMappings) {
            if (resultMapping.getFlags().contains(ResultFlag.CONSTRUCTOR)) {
                declaresConstructor = true;
                break;
            }
        }
        if (declaresConstructor) {
            extendedResultMappings.removeIf(resultMapping -> resultMapping.getFlags().contains(ResultFlag.CONSTRUCTOR));
        }
        // 将 extendedResultMappings 添加到 resultMappings 中
        resultMappings.addAll(extendedResultMappings);
    }
    // <3> 创建 ResultMap 对象
    ResultMap resultMap = new ResultMap.Builder(configuration, id, type, resultMappings, autoMapping)
            .discriminator(discriminator)
            .build();
    // <4> 添加到 configuration 中
    configuration.addResultMap(resultMap);
    return resultMap;
}
````

#### 15.2.7.1 ResultMap ####
org.apache.ibatis.mapping.ResultMap ，结果集，例如 <resultMap /> 解析后的对象。

----

# 16 MyBatis 初始化（三）之加载 Statement 配置 #

## 16.1 XMLStatementBuilder ##
org.apache.ibatis.builder.xml.XMLStatementBuilder ，继承 BaseBuilder 抽象类，Statement XML 配置构建器，主要负责解析 Statement 配置，即 
````
<select />、<insert />、<update />、<delete />
```` 
标签。

### 16.1.1 构造方法 ###
````
private final MapperBuilderAssistant builderAssistant;
/**
 * 当前 XML 节点，例如：<select />、<insert />、<update />、<delete /> 标签
 */
private final XNode context;
/**
 * 要求的 databaseId
 */
private final String requiredDatabaseId;

public XMLStatementBuilder(Configuration configuration, MapperBuilderAssistant builderAssistant, XNode context, String databaseId) {
    super(configuration);
    this.builderAssistant = builderAssistant;
    this.context = context;
    this.requiredDatabaseId = databaseId;
}
````

### 16.1.2 parseStatementNode ###
parseStatementNode() 方法，执行 Statement 解析。

````
public void parseStatementNode() {
    // <1> 获得 id 属性，编号。
    String id = context.getStringAttribute("id");
    // <2> 获得 databaseId ， 判断 databaseId 是否匹配
    String databaseId = context.getStringAttribute("databaseId");
    if (!databaseIdMatchesCurrent(id, databaseId, this.requiredDatabaseId)) {
        return;
    }

    // <3> 获得各种属性
    Integer fetchSize = context.getIntAttribute("fetchSize");
    Integer timeout = context.getIntAttribute("timeout");
    String parameterMap = context.getStringAttribute("parameterMap");
    String parameterType = context.getStringAttribute("parameterType");
    Class<?> parameterTypeClass = resolveClass(parameterType);
    String resultMap = context.getStringAttribute("resultMap");
    String resultType = context.getStringAttribute("resultType");
    String lang = context.getStringAttribute("lang");

    // <4> 获得 lang 对应的 LanguageDriver 对象
    LanguageDriver langDriver = getLanguageDriver(lang);

    // <5> 获得 resultType 对应的类
    Class<?> resultTypeClass = resolveClass(resultType);
    // <6> 获得 resultSet 对应的枚举值
    String resultSetType = context.getStringAttribute("resultSetType");
    ResultSetType resultSetTypeEnum = resolveResultSetType(resultSetType);
    // <7> 获得 statementType 对应的枚举值
    StatementType statementType = StatementType.valueOf(context.getStringAttribute("statementType", StatementType.PREPARED.toString()));

    // <8> 获得 SQL 对应的 SqlCommandType 枚举值
    String nodeName = context.getNode().getNodeName();
    SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));
    // <9> 获得各种属性
    boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
    boolean flushCache = context.getBooleanAttribute("flushCache", !isSelect);
    boolean useCache = context.getBooleanAttribute("useCache", isSelect);
    boolean resultOrdered = context.getBooleanAttribute("resultOrdered", false);

    // Include Fragments before parsing
    // <10> 创建 XMLIncludeTransformer 对象，并替换 <include /> 标签相关的内容
    XMLIncludeTransformer includeParser = new XMLIncludeTransformer(configuration, builderAssistant);
    includeParser.applyIncludes(context.getNode());

    // Parse selectKey after includes and remove them.
    // <11> 解析 <selectKey /> 标签
    processSelectKeyNodes(id, parameterTypeClass, langDriver);

    // Parse the SQL (pre: <selectKey> and <include> were parsed and removed)
    // <12> 创建 SqlSource
    SqlSource sqlSource = langDriver.createSqlSource(configuration, context, parameterTypeClass);
    // <13> 获得 KeyGenerator 对象
    String resultSets = context.getStringAttribute("resultSets");
    String keyProperty = context.getStringAttribute("keyProperty");
    String keyColumn = context.getStringAttribute("keyColumn");
    KeyGenerator keyGenerator;
    // <13.1> 优先，从 configuration 中获得 KeyGenerator 对象。如果存在，意味着是 <selectKey /> 标签配置的
    String keyStatementId = id + SelectKeyGenerator.SELECT_KEY_SUFFIX;
    keyStatementId = builderAssistant.applyCurrentNamespace(keyStatementId, true);
    if (configuration.hasKeyGenerator(keyStatementId)) {
        keyGenerator = configuration.getKeyGenerator(keyStatementId);
    // <13.2> 其次，根据标签属性的情况，判断是否使用对应的 Jdbc3KeyGenerator 或者 NoKeyGenerator 对象
    } else {
        keyGenerator = context.getBooleanAttribute("useGeneratedKeys", // 优先，基于 useGeneratedKeys 属性判断
                configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType)) // 其次，基于全局的 useGeneratedKeys 配置 + 是否为插入语句类型
                ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
    }

    // 创建 MappedStatement 对象
    builderAssistant.addMappedStatement(id, sqlSource, statementType, sqlCommandType,
            fetchSize, timeout, parameterMap, parameterTypeClass, resultMap, resultTypeClass,
            resultSetTypeEnum, flushCache, useCache, resultOrdered,
            keyGenerator, keyProperty, keyColumn, databaseId, langDriver, resultSets);
}
````

### 16.1.3 databaseIdMatchesCurrent ###
databaseIdMatchesCurrent(String id, String databaseId, String requiredDatabaseId) 方法，判断 databaseId 是否匹配。

````
private boolean databaseIdMatchesCurrent(String id, String databaseId, String requiredDatabaseId) {
    // 如果不匹配，则返回 false
    if (requiredDatabaseId != null) {
        return requiredDatabaseId.equals(databaseId);
    } else {
        // 如果未设置 requiredDatabaseId ，但是 databaseId 存在，说明还是不匹配，则返回 false
        if (databaseId != null) {
            return false;
        }
        // 判断是否已经存在
        id = builderAssistant.applyCurrentNamespace(id, false);
        if (this.configuration.hasStatement(id, false)) {
            // 若存在，则判断原有的 sqlFragment 是否 databaseId 为空。因为，当前 databaseId 为空，这样两者才能匹配。
            return previous.getDatabaseId() == null;
        }
    }
    return true;
}
````

从逻辑上，和我们在 XMLMapperBuilder 看到的同名方法 #databaseIdMatchesCurrent(String id, String databaseId, String requiredDatabaseId) 方法是一致的。

### 16.1.4 getLanguageDriver ###
getLanguageDriver(String lang) 方法，获得 lang 对应的 LanguageDriver 对象。

主要调用 MapperBuilderAssistant#getLanguageDriver(lass<? extends LanguageDriver> langClass) 方法，获得 LanguageDriver 对象。

### 16.1.5 processSelectKeyNodes ###

selectKey标签的作用。
https://blog.csdn.net/xu1916659422/article/details/77921912


processSelectKeyNodes(String id, Class<?> parameterTypeClass, LanguageDriver langDriver) 方法，解析 <selectKey /> 标签。

````
private void processSelectKeyNodes(String id, Class<?> parameterTypeClass, LanguageDriver langDriver) {
    // <1> 获得 <selectKey /> 节点们
    List<XNode> selectKeyNodes = context.evalNodes("selectKey");
    // <2> 执行解析 <selectKey /> 节点们
    if (configuration.getDatabaseId() != null) {
        parseSelectKeyNodes(id, selectKeyNodes, parameterTypeClass, langDriver, configuration.getDatabaseId());
    }
    parseSelectKeyNodes(id, selectKeyNodes, parameterTypeClass, langDriver, null);
    // <3> 移除 <selectKey /> 节点们
    removeSelectKeyNodes(selectKeyNodes);
}
````

#### 16.1.5.1 parseSelectKeyNodes ####
parseSelectKeyNodes(String parentId, List<XNode> list, Class<?> parameterTypeClass, LanguageDriver langDriver, String skRequiredDatabaseId) 方法，执行解析 <selectKey /> 子节点们。


#### 16.1.5.2 parseSelectKeyNode ####
parseSelectKeyNode(...) 方法，执行解析单个 <selectKey /> 节点。

````
private void parseSelectKeyNode(String id, XNode nodeToHandle, Class<?> parameterTypeClass, LanguageDriver langDriver, String databaseId) {
    // <1.1> 获得各种属性和对应的类
    String resultType = nodeToHandle.getStringAttribute("resultType");
    Class<?> resultTypeClass = resolveClass(resultType);
    StatementType statementType = StatementType.valueOf(nodeToHandle.getStringAttribute("statementType", StatementType.PREPARED.toString()));
    String keyProperty = nodeToHandle.getStringAttribute("keyProperty");
    String keyColumn = nodeToHandle.getStringAttribute("keyColumn");
    boolean executeBefore = "BEFORE".equals(nodeToHandle.getStringAttribute("order", "AFTER"));

    // <1.2> 创建 MappedStatement 需要用到的默认值
    boolean useCache = false;
    boolean resultOrdered = false;
    KeyGenerator keyGenerator = NoKeyGenerator.INSTANCE;
    Integer fetchSize = null;
    Integer timeout = null;
    boolean flushCache = false;
    String parameterMap = null;
    String resultMap = null;
    ResultSetType resultSetTypeEnum = null;

    // <1.3> 创建 SqlSource 对象
    SqlSource sqlSource = langDriver.createSqlSource(configuration, nodeToHandle, parameterTypeClass);
    SqlCommandType sqlCommandType = SqlCommandType.SELECT;

    // <1.4> 创建 MappedStatement 对象
    builderAssistant.addMappedStatement(id, sqlSource, statementType, sqlCommandType,
            fetchSize, timeout, parameterMap, parameterTypeClass, resultMap, resultTypeClass,
            resultSetTypeEnum, flushCache, useCache, resultOrdered,
            keyGenerator, keyProperty, keyColumn, databaseId, langDriver, null);

    // <2.1> 获得 SelectKeyGenerator 的编号，格式为 `${namespace}.${id}`
    id = builderAssistant.applyCurrentNamespace(id, false);
    // <2.2> 获得 MappedStatement 对象
    MappedStatement keyStatement = configuration.getMappedStatement(id, false);
    // <2.3> 创建 SelectKeyGenerator 对象，并添加到 configuration 中
    configuration.addKeyGenerator(id, new SelectKeyGenerator(keyStatement, executeBefore));
}
````

## 16.2 XMLIncludeTransformer ##
org.apache.ibatis.builder.xml.XMLIncludeTransformer ，XML <include /> 标签的转换器，负责将 SQL 中的 <include /> 标签转换成对应的 <sql /> 的内容。

### 16.2.1 构造方法 ###

````
private final Configuration configuration;
private final MapperBuilderAssistant builderAssistant;

public XMLIncludeTransformer(Configuration configuration, MapperBuilderAssistant builderAssistant) {
    this.configuration = configuration;
    this.builderAssistant = builderAssistant;
}
````

### 16.2.2 applyIncludes ###
applyIncludes(Node source) 方法，将 <include /> 标签，替换成引用的 <sql /> 。

````
public void applyIncludes(Node source) {
    // <1> 创建 variablesContext ，并将 configurationVariables 添加到其中
    Properties variablesContext = new Properties();
    Properties configurationVariables = configuration.getVariables();
    if (configurationVariables != null) {
        variablesContext.putAll(configurationVariables);
    }
    // <2> 处理 <include />
    applyIncludes(source, variablesContext, false);
}
````

applyIncludes(Node source, final Properties variablesContext, boolean included) 方法，使用递归的方式，将 <include /> 标签，替换成引用的 <sql /> 。

### 16.2.3 findSqlFragment ###
findSqlFragment(String refid, Properties variables) 方法，获得对应的 <sql /> 节点。

### 16.2.4 getVariablesContext ###
getVariablesContext(Node node, Properties inheritedVariablesContext) 方法，获得包含 <include /> 标签内的属性 Properties 对象

## 16.3 MapperBuilderAssistant ##
### 16.3.1 addMappedStatement ###
addMappedStatement(String id, SqlSource sqlSource, StatementType statementType, SqlCommandType sqlCommandType, Integer fetchSize, Integer timeout, String parameterMap, Class<?> parameterType, String resultMap, Class<?> resultType, ResultSetType resultSetType, boolean flushCache, boolean useCache, boolean resultOrdered, KeyGenerator keyGenerator, String keyProperty, String keyColumn, String databaseId, LanguageDriver lang, String resultSets) 方法，构建 MappedStatement 对象


````
public MappedStatement addMappedStatement(
        String id,
        SqlSource sqlSource,
        StatementType statementType,
        SqlCommandType sqlCommandType,
        Integer fetchSize,
        Integer timeout,
        String parameterMap,
        Class<?> parameterType,
        String resultMap,
        Class<?> resultType,
        ResultSetType resultSetType,
        boolean flushCache,
        boolean useCache,
        boolean resultOrdered,
        KeyGenerator keyGenerator,
        String keyProperty,
        String keyColumn,
        String databaseId,
        LanguageDriver lang,
        String resultSets) {

    // <1> 如果只想的 Cache 未解析，抛出 IncompleteElementException 异常
    if (unresolvedCacheRef) {
        throw new IncompleteElementException("Cache-ref not yet resolved");
    }

    // <2> 获得 id 编号，格式为 `${namespace}.${id}`
    id = applyCurrentNamespace(id, false);
    boolean isSelect = sqlCommandType == SqlCommandType.SELECT;

    // <3> 创建 MappedStatement.Builder 对象
    MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, id, sqlSource, sqlCommandType)
            .resource(resource)
            .fetchSize(fetchSize)
            .timeout(timeout)
            .statementType(statementType)
            .keyGenerator(keyGenerator)
            .keyProperty(keyProperty)
            .keyColumn(keyColumn)
            .databaseId(databaseId)
            .lang(lang)
            .resultOrdered(resultOrdered)
            .resultSets(resultSets)
            .resultMaps(getStatementResultMaps(resultMap, resultType, id)) // <3.1> 获得 ResultMap 集合
            .resultSetType(resultSetType)
            .flushCacheRequired(valueOrDefault(flushCache, !isSelect))
            .useCache(valueOrDefault(useCache, isSelect))
            .cache(currentCache);

    // <3.2> 获得 ParameterMap ，并设置到 MappedStatement.Builder 中
    ParameterMap statementParameterMap = getStatementParameterMap(parameterMap, parameterType, id);
    if (statementParameterMap != null) {
        statementBuilder.parameterMap(statementParameterMap);
    }

    // <4> 创建 MappedStatement 对象
    MappedStatement statement = statementBuilder.build();
    // <5> 添加到 configuration 中
    configuration.addMappedStatement(statement);
    return statement;
}
````

#### 16.3.1.1 MappedStatement ####
org.apache.ibatis.mapping.MappedStatement ，映射的语句，每个 
````
<select />、<insert />、<update />、<delete />
````
对应一个 MappedStatement 对象。

另外，比较特殊的是，<selectKey /> 解析后，也会对应一个 MappedStatement 对象。
在另外，MappedStatement 有一个非常重要的方法 #getBoundSql(Object parameterObject) 方法，获得 BoundSql 对象。

**BoundSql 表示动态生成的SQL语句以及相应的参数信息**
````
public BoundSql getBoundSql(Object parameterObject) {
    // 获得 BoundSql 对象
    BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
    // 忽略，因为 <parameterMap /> 已经废弃，参见 http://www.mybatis.org/mybatis-3/zh/sqlmap-xml.html 文档
    List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    if (parameterMappings == null || parameterMappings.isEmpty()) {
        boundSql = new BoundSql(configuration, boundSql.getSql(), parameterMap.getParameterMappings(), parameterObject);
    }

    // check for nested result maps in parameter mappings (issue #30)
    // 判断传入的参数中，是否有内嵌的结果 ResultMap 。如果有，则修改 hasNestedResultMaps 为 true
    // 存储过程相关，暂时无视
    for (ParameterMapping pm : boundSql.getParameterMappings()) {
        String rmId = pm.getResultMapId();
        if (rmId != null) {
            ResultMap rm = configuration.getResultMap(rmId);
            if (rm != null) {
                hasNestedResultMaps |= rm.hasNestedResultMaps();
            }
        }
    }

    return boundSql;
}
````

#### 16.3.1.2 ParameterMap ####
org.apache.ibatis.mapping.ParameterMap ，参数集合，对应 paramType="" 或 paramMap="" 标签属性。

#### 16.3.1.3 getStatementResultMaps ####
getStatementResultMaps(...) 方法，获得 ResultMap 集合。

````
private List<ResultMap> getStatementResultMaps(
        String resultMap,
        Class<?> resultType,
        String statementId) {
    // 获得 resultMap 的编号
    resultMap = applyCurrentNamespace(resultMap, true);

    // 创建 ResultMap 集合
    List<ResultMap> resultMaps = new ArrayList<>();
    // 如果 resultMap 非空，则获得 resultMap 对应的 ResultMap 对象(们）
    if (resultMap != null) {
        String[] resultMapNames = resultMap.split(",");
        for (String resultMapName : resultMapNames) {
            try {
                resultMaps.add(configuration.getResultMap(resultMapName.trim())); // 从 configuration 中获得
            } catch (IllegalArgumentException e) {
                throw new IncompleteElementException("Could not find result map " + resultMapName, e);
            }
        }
    // 如果 resultType 非空，则创建 ResultMap 对象
    } else if (resultType != null) {
        ResultMap inlineResultMap = new ResultMap.Builder(
                configuration,
                statementId + "-Inline",
                resultType,
                new ArrayList<>(),
                null).build();
        resultMaps.add(inlineResultMap);
    }
    return resultMaps;
}
````

#### 16.3.1.4 getStatementResultMaps ####
getStatementParameterMap(...) 方法，获得 ParameterMap 对象。

````
private ParameterMap getStatementParameterMap(
        String parameterMapName,
        Class<?> parameterTypeClass,
        String statementId) {
    // 获得 ParameterMap 的编号，格式为 `${namespace}.${parameterMapName}`
    parameterMapName = applyCurrentNamespace(parameterMapName, true);
    ParameterMap parameterMap = null;
    // <2> 如果 parameterMapName 非空，则获得 parameterMapName 对应的 ParameterMap 对象
    if (parameterMapName != null) {
        try {
            parameterMap = configuration.getParameterMap(parameterMapName);
        } catch (IllegalArgumentException e) {
            throw new IncompleteElementException("Could not find parameter map " + parameterMapName, e);
        }
    // <1> 如果 parameterTypeClass 非空，则创建 ParameterMap 对象
    } else if (parameterTypeClass != null) {
        List<ParameterMapping> parameterMappings = new ArrayList<>();
        parameterMap = new ParameterMap.Builder(
                configuration,
                statementId + "-Inline",
                parameterTypeClass,
                parameterMappings).build();
    }
    return parameterMap;
}
````
----

# 17 MyBatis 初始化（四）之加载注解配置 #

加载注解配置。这个部分的入口是 MapperAnnotationBuilder 。


## 17.1 MapperAnnotationBuilder ##
org.apache.ibatis.builder.annotation.MapperAnnotationBuilder ，Mapper 注解构造器，负责解析 Mapper 接口上的注解。

主要的作用是解析方法上的注解并组装成数据，然后构造成xml文件解析的数据。

### 17.1.1 构造方法 ###
````
/**
 * SQL 操作注解集合
 */
private static final Set<Class<? extends Annotation>> SQL_ANNOTATION_TYPES = new HashSet<>();
/**
 * SQL 操作提供者注解集合
 */
private static final Set<Class<? extends Annotation>> SQL_PROVIDER_ANNOTATION_TYPES = new HashSet<>();

private final Configuration configuration;
private final MapperBuilderAssistant assistant;
/**
 * Mapper 接口类
 */
private final Class<?> type;

static {
    SQL_ANNOTATION_TYPES.add(Select.class);
    SQL_ANNOTATION_TYPES.add(Insert.class);
    SQL_ANNOTATION_TYPES.add(Update.class);
    SQL_ANNOTATION_TYPES.add(Delete.class);

    SQL_PROVIDER_ANNOTATION_TYPES.add(SelectProvider.class);
    SQL_PROVIDER_ANNOTATION_TYPES.add(InsertProvider.class);
    SQL_PROVIDER_ANNOTATION_TYPES.add(UpdateProvider.class);
    SQL_PROVIDER_ANNOTATION_TYPES.add(DeleteProvider.class);
}

public MapperAnnotationBuilder(Configuration configuration, Class<?> type) {
    // 创建 MapperBuilderAssistant 对象
    String resource = type.getName().replace('.', '/') + ".java (best guess)";
    this.assistant = new MapperBuilderAssistant(configuration, resource);
    this.configuration = configuration;
    this.type = type;
}
````

### 17.1.2 parse ###
parse() 方法，解析注解。

````
public void parse() {
    // <1> 判断当前 Mapper 接口是否应加载过。
    String resource = type.toString();
    if (!configuration.isResourceLoaded(resource)) {
        // <2> 加载对应的 XML Mapper
        loadXmlResource();
        // <3> 标记该 Mapper 接口已经加载过
        configuration.addLoadedResource(resource);
        // <4> 设置 namespace 属性
        assistant.setCurrentNamespace(type.getName());
        // <5> 解析 @CacheNamespace 注解
        parseCache();
        // <6> 解析 @CacheNamespaceRef 注解
        parseCacheRef();
        // <7> 遍历每个方法，解析其上的注解
        Method[] methods = type.getMethods();
        for (Method method : methods) {
            try {
                if (!method.isBridge()) {
                    // <7.1> 执行解析
                    parseStatement(method);
                }
            } catch (IncompleteElementException e) {
                // <7.2> 解析失败，添加到 configuration 中
                configuration.addIncompleteMethod(new MethodResolver(this, method));
            }
        }
    }
    // <8> 解析待定的方法
    parsePendingMethods();
}
````

### 17.1.3 loadXmlResource ###
loadXmlResource() 方法，加载对应的 XML Mapper 。
````
private void loadXmlResource() {
    // <1> 判断 Mapper XML 是否已经加载过，如果加载过，就不加载了。
    // 此处，是为了避免和 XMLMapperBuilder#parse() 方法冲突，重复解析
    if (!configuration.isResourceLoaded("namespace:" + type.getName())) {
        // <2> 获得 InputStream 对象
        String xmlResource = type.getName().replace('.', '/') + ".xml";
        // #1347
        InputStream inputStream = type.getResourceAsStream("/" + xmlResource);
        if (inputStream == null) {
            try {
                inputStream = Resources.getResourceAsStream(type.getClassLoader(), xmlResource);
            } catch (IOException e2) {
                // ignore, resource is not required
            }
        }
        // 获得 InputStream 对象，然后创建 XMLMapperBuilder 对象，最后调用 XMLMapperBuilder#parse() 方法，执行解析。
这里，如果是先解析 Mapper 接口，那就会达到再解析对应的 Mapper XML 的情况。
        if (inputStream != null) {
            XMLMapperBuilder xmlParser = new XMLMapperBuilder(inputStream, assistant.getConfiguration(), xmlResource, configuration.getSqlFragments(), type.getName());
            xmlParser.parse();
        }
    }
}
````

### 17.1.4 parseCache ###
parseCache() 方法，解析 @CacheNamespace 注解。

````
private void parseCache() {
    // <1> 获得类上的 @CacheNamespace 注解
    CacheNamespace cacheDomain = type.getAnnotation(CacheNamespace.class);
    if (cacheDomain != null) {
        // <2> 获得各种属性
        Integer size = cacheDomain.size() == 0 ? null : cacheDomain.size();
        Long flushInterval = cacheDomain.flushInterval() == 0 ? null : cacheDomain.flushInterval();
        // <3> 调用 #convertToProperties(Property[] properties) 方法，将 @Property 注解数组，转换成 Properties 对象。
        Properties props = convertToProperties(cacheDomain.properties());
        // <4> 创建 Cache 对象
        assistant.useNewCache(cacheDomain.implementation(), cacheDomain.eviction(), flushInterval, size, cacheDomain.readWrite(), cacheDomain.blocking(), props);
    }
}
````

### 17.1.5 parseCacheRef ###
parseCacheRef() 方法，解析 @CacheNamespaceRef 注解。

````
private void parseCacheRef() {
    // 获得类上的 @CacheNamespaceRef 注解
    CacheNamespaceRef cacheDomainRef = type.getAnnotation(CacheNamespaceRef.class);
    if (cacheDomainRef != null) {
        // <2> 获得各种属性
        Class<?> refType = cacheDomainRef.value();
        String refName = cacheDomainRef.name();
        // <2> 校验，如果 refType 和 refName 都为空，则抛出 BuilderException 异常
        if (refType == void.class && refName.isEmpty()) {
            throw new BuilderException("Should be specified either value() or name() attribute in the @CacheNamespaceRef");
        }
        // <2> 校验，如果 refType 和 refName 都不为空，则抛出 BuilderException 异常
        if (refType != void.class && !refName.isEmpty()) {
            throw new BuilderException("Cannot use both value() and name() attribute in the @CacheNamespaceRef");
        }
        // <2> 获得最终的 namespace 属性
        String namespace = (refType != void.class) ? refType.getName() : refName;
        // <3> 获得指向的 Cache 对象
        assistant.useCacheRef(namespace);
    }
}
````

### 17.1.6 parseStatement ###
parseStatement(Method method) 方法，解析方法上的 SQL 操作相关的注解。

````
void parseStatement(Method method) {
    // <1> 获得参数的类型
    Class<?> parameterTypeClass = getParameterType(method);
    // <2> 获得 LanguageDriver 对象
    LanguageDriver languageDriver = getLanguageDriver(method);
    // <3> 获得 SqlSource 对象
    SqlSource sqlSource = getSqlSourceFromAnnotations(method, parameterTypeClass, languageDriver);
    if (sqlSource != null) {
        // <4> 获得各种属性
        Options options = method.getAnnotation(Options.class);
        final String mappedStatementId = type.getName() + "." + method.getName();
        Integer fetchSize = null;
        Integer timeout = null;
        StatementType statementType = StatementType.PREPARED;
        ResultSetType resultSetType = null;
        SqlCommandType sqlCommandType = getSqlCommandType(method);
        boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
        boolean flushCache = !isSelect;
        boolean useCache = isSelect;

        // <5> 获得 KeyGenerator 对象
        KeyGenerator keyGenerator;
        String keyProperty = null;
        String keyColumn = null;
        if (SqlCommandType.INSERT.equals(sqlCommandType) || SqlCommandType.UPDATE.equals(sqlCommandType)) { // 有
            // first check for SelectKey annotation - that overrides everything else
            // <5.1> 如果有 @SelectKey 注解，则进行处理
            SelectKey selectKey = method.getAnnotation(SelectKey.class);
            if (selectKey != null) {
                keyGenerator = handleSelectKeyAnnotation(selectKey, mappedStatementId, getParameterType(method), languageDriver);
                keyProperty = selectKey.keyProperty();
            // <5.2> 如果无 @Options 注解，则根据全局配置处理
            } else if (options == null) {
                keyGenerator = configuration.isUseGeneratedKeys() ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
            // <5.3> 如果有 @Options 注解，则使用该注解的配置处理
            } else {
                keyGenerator = options.useGeneratedKeys() ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
                keyProperty = options.keyProperty();
                keyColumn = options.keyColumn();
            }
        // <5.4> 无
        } else {
            keyGenerator = NoKeyGenerator.INSTANCE;
        }

        // <6> 初始化各种属性
        if (options != null) {
            if (FlushCachePolicy.TRUE.equals(options.flushCache())) {
                flushCache = true;
            } else if (FlushCachePolicy.FALSE.equals(options.flushCache())) {
                flushCache = false;
            }
            useCache = options.useCache();
            fetchSize = options.fetchSize() > -1 || options.fetchSize() == Integer.MIN_VALUE ? options.fetchSize() : null; //issue #348
            timeout = options.timeout() > -1 ? options.timeout() : null;
            statementType = options.statementType();
            resultSetType = options.resultSetType();
        }

        // <7> 获得 resultMapId 编号字符串
        String resultMapId = null;
        // <7.1> 如果有 @ResultMap 注解，使用该注解为 resultMapId 属性
        ResultMap resultMapAnnotation = method.getAnnotation(ResultMap.class);
        if (resultMapAnnotation != null) {
            String[] resultMaps = resultMapAnnotation.value();
            StringBuilder sb = new StringBuilder();
            for (String resultMap : resultMaps) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(resultMap);
            }
            resultMapId = sb.toString();
        // <7.2> 如果无 @ResultMap 注解，解析其它注解，作为 resultMapId 属性
        } else if (isSelect) {
            resultMapId = parseResultMap(method);
        }

        // 构建 MappedStatement 对象
        assistant.addMappedStatement(
                mappedStatementId,
                sqlSource,
                statementType,
                sqlCommandType,
                fetchSize,
                timeout,
                // ParameterMapID
                null,
                parameterTypeClass,
                resultMapId,
                getReturnType(method), // 获得返回类型
                resultSetType,
                flushCache,
                useCache,
                // TODO gcode issue #577
                false,
                keyGenerator,
                keyProperty,
                keyColumn,
                // DatabaseID
                null,
                languageDriver,
                // ResultSets
                options != null ? nullOrEmpty(options.resultSets()) : null);
    }
}
````

#### 17.1.6.1 getParameterType ####
调用 #getParameterType(Method method) 方法，获得参数的类型。
````
private Class<?> getParameterType(Method method) {
    Class<?> parameterType = null;
    // 遍历参数类型数组
    // 排除 RowBounds 和 ResultHandler 两种参数
    // 1. 如果是多参数，则是 ParamMap 类型
    // 2. 如果是单参数，则是该参数的类型
    Class<?>[] parameterTypes = method.getParameterTypes();
    for (Class<?> currentParameterType : parameterTypes) {
        if (!RowBounds.class.isAssignableFrom(currentParameterType) && !ResultHandler.class.isAssignableFrom(currentParameterType)) {
            if (parameterType == null) {
                parameterType = currentParameterType;
            } else {
                // issue #135
                parameterType = ParamMap.class;
            }
        }
    }
    return parameterType;
}
````
- 根据是否为多参数，返回是 ParamMap 类型，还是单参数对应的类型。

#### 17.1.6.2 getLanguageDriver ####
getLanguageDriver(Method method) 方法，获得 LanguageDriver 对象。
````
private LanguageDriver getLanguageDriver(Method method) {
    // 解析 @Lang 注解，获得对应的类型
    Lang lang = method.getAnnotation(Lang.class);
    Class<? extends LanguageDriver> langClass = null;
    if (lang != null) {
        langClass = lang.value();
    }
    // 获得 LanguageDriver 对象
    // 如果 langClass 为空，即无 @Lang 注解，则会使用默认 LanguageDriver 类型
    return assistant.getLanguageDriver(langClass);
}
````
调用 MapperBuilderAssistant#getLanguageDriver(Class<? extends LanguageDriver> langClass) 方法，获得 LanguageDriver 对象。

#### 17.1.6.3 getSqlSourceFromAnnotations ####
getSqlSourceFromAnnotations(Method method, Class<?> parameterType, LanguageDriver languageDriver) 方法，从注解中，获得 SqlSource 对象。

````
private SqlSource getSqlSourceFromAnnotations(Method method, Class<?> parameterType, LanguageDriver languageDriver) {
    try {
        // <1.1> <1.2> 获得方法上的 SQL_ANNOTATION_TYPES 和 SQL_PROVIDER_ANNOTATION_TYPES 对应的类型
        Class<? extends Annotation> sqlAnnotationType = getSqlAnnotationType(method);
        Class<? extends Annotation> sqlProviderAnnotationType = getSqlProviderAnnotationType(method);
        // <2> 如果 SQL_ANNOTATION_TYPES 对应的类型非空
        if (sqlAnnotationType != null) {
            // 如果 SQL_PROVIDER_ANNOTATION_TYPES 对应的类型非空，则抛出 BindingException 异常，因为冲突了。
            if (sqlProviderAnnotationType != null) {
                throw new BindingException("You cannot supply both a static SQL and SqlProvider to method named " + method.getName());
            }
            // <2.1> 获得 SQL_ANNOTATION_TYPES 对应的注解
            Annotation sqlAnnotation = method.getAnnotation(sqlAnnotationType);
            // <2.2> 获得 value 属性
            final String[] strings = (String[]) sqlAnnotation.getClass().getMethod("value").invoke(sqlAnnotation);
            // <2.3> 创建 SqlSource 对象
            return buildSqlSourceFromStrings(strings, parameterType, languageDriver);
        // <3> 如果 SQL_PROVIDER_ANNOTATION_TYPES 对应的类型非空
        } else if (sqlProviderAnnotationType != null) {
            // <3.1> 获得 SQL_PROVIDER_ANNOTATION_TYPES 对应的注解
            Annotation sqlProviderAnnotation = method.getAnnotation(sqlProviderAnnotationType);
            // <3.2> 创建 ProviderSqlSource 对象
            return new ProviderSqlSource(assistant.getConfiguration(), sqlProviderAnnotation, type, method);
        }
        // <4> 返回空
        return null;
    } catch (Exception e) {
        throw new BuilderException("Could not find value method on SQL annotation.  Cause: " + e, e);
    }
}
````

#### 17.1.6.4 handleSelectKeyAnnotation ####
handleSelectKeyAnnotation(SelectKey selectKeyAnnotation, String baseStatementId, Class<?> parameterTypeClass, LanguageDriver languageDriver) 方法，处理 @@SelectKey 注解，生成对应的 SelectKey 对象。

从实现逻辑上，我们会发现，和 XMLStatementBuilder#parseSelectKeyNode(String id, XNode nodeToHandle, Class<?> parameterTypeClass, LanguageDriver langDriver, String databaseId) 方法是一致的。

#### 17.1.6.5 getSqlCommandType ####
getSqlCommandType(Method method) 方法，获得方法对应的 SQL 命令类型。

#### 17.1.6.6 parseResultMap ####
parseResultMap(Method method) 方法，解析其它注解，返回 resultMapId 属性。

### 17.1.7 MethodResolver ###
org.apache.ibatis.builder.annotation.MethodResolver ，注解方法的处理器。

在 #resolve() 方法里，可以调用 MapperAnnotationBuilder#parseStatement(Method method) 方法，执行注解方法的解析。

### 17.1.8 parsePendingMethods ###

- 1）获得对应的集合；2）遍历集合，执行解析；3）执行成功，则移除出集合；4）执行失败，忽略异常。
- 当然，实际上，此处还是可能有执行解析失败的情况，但是随着每一个 Mapper 接口对应的 MapperAnnotationBuilder 执行一次这些方法，逐步逐步就会被全部解析完


----

**以上主要是MyBatis XML 配置和注解配置的解析，但不包括sql的解析。以下主要是动态sql的解析。**

-----

# 18 SQL 初始化（上）之 SqlNode #

- 拼凑 SQL 语句是一件烦琐且易出错的过程，为了将开发人员从这项枯燥无趣的工作中 解脱出来，MyBatis 实现动态 SQL 语句的功能，提供了多种动态 SQL语句对应的节点。例如<where> 节点、<if> 节点、<foreach> 节点等 。通过这些节点的组合使用， 开发人 员可以写出几乎满足所有需求的动态 SQL 语句。
- MyBatis 中的 scripting 模块，会根据用户传入的实参，解析映射文件中定义的动态 SQL 节点，并形成数据库可执行的 SQL 语句。之后会处理 SQL 语句中的占位符，绑定用户传入的实参。
- 总结来说，scripting 模块，最大的作用，就是实现了 MyBatis 的动态 SQL 语句的功能。关于这个功能，对应文档为http://www.mybatis.org/mybatis-3/zh/dynamic-sql.html

主要包含以下内容：
- LanguageDriver
- SqlSource
- SqlNode
- NodeHandler
- 基于 OGNL 表达式

## 18.1 LanguageDriver ##
org.apache.ibatis.scripting.LanguageDriver ，语言驱动接口。

### 18.1.1 XMLLanguageDriver ###
org.apache.ibatis.scripting.xmltags.XMLLanguageDriver ，实现 LanguageDriver 接口，XML 语言驱动实现类。

#### 18.1.1.1 createParameterHandler ####
createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) 方法，代码如下：
````
@Override
public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
    // 创建 DefaultParameterHandler 对象
    return new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
}
````
创建的是 DefaultParameterHandler 对象。

#### 18.1.1.2 createSqlSource ####
创建 XMLScriptBuilder 对象，执行 XMLScriptBuilder#parseScriptNode() 方法，执行解析。

#### 18.1.1.3 createSqlSource ####
createSqlSource(Configuration configuration, String script, Class<?> parameterType) 方法

````
@Override
public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
    // <1> 如果是 <script> 开头，使用 XML 配置的方式，使用动态 SQL 
    if (script.startsWith("<script>")) {
        // <1.1> 创建 XPathParser 对象，解析出 <script /> 节点
        XPathParser parser = new XPathParser(script, false, configuration.getVariables(), new XMLMapperEntityResolver());
        // <1.2> 调用上面的 #createSqlSource(...) 方法，创建 SqlSource 对象
        return createSqlSource(configuration, parser.evalNode("/script"), parameterType);
    // <2>
    } else {
        // <2.1> 变量替换
        script = PropertyParser.parse(script, configuration.getVariables());
        // <2.2> 创建 TextSqlNode 对象
        TextSqlNode textSqlNode = new TextSqlNode(script);
        // <2.3.1> 如果是动态 SQL ，则创建 DynamicSqlSource 对象
        if (textSqlNode.isDynamic()) {
            return new DynamicSqlSource(configuration, textSqlNode);
        // <2.3.2> 如果非动态 SQL ，则创建 RawSqlSource 对象
        } else {
            return new RawSqlSource(configuration, script, parameterType);
        }
    }
}
````

### 18.1.2 RawLanguageDriver ###
org.apache.ibatis.scripting.defaults.RawLanguageDriver ，继承 XMLLanguageDriver 类，RawSqlSource 语言驱动器实现类，确保创建的 SqlSource 是 RawSqlSource 类

````
ublic class RawLanguageDriver extends XMLLanguageDriver {

    @Override
    public SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType) {
        // 调用父类，创建 SqlSource 对象
        SqlSource source = super.createSqlSource(configuration, script, parameterType);
        // 校验创建的是 RawSqlSource 对象
        checkIsNotDynamic(source);
        return source;
    }

    @Override
    public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
        // 调用父类，创建 SqlSource 对象
        SqlSource source = super.createSqlSource(configuration, script, parameterType);
        // 校验创建的是 RawSqlSource 对象
        checkIsNotDynamic(source);
        return source;
    }

    /**
     * 校验是 RawSqlSource 对象
     *
     * @param source 创建的 SqlSource 对象
     */
    private void checkIsNotDynamic(SqlSource source) {
        if (!RawSqlSource.class.equals(source.getClass())) {
            throw new BuilderException("Dynamic content is not allowed when using RAW language");
        }
    }

}
````
先基于父方法，创建 SqlSource 对象，然后再调用 #checkIsNotDynamic(SqlSource source) 方法，进行校验是否为 RawSqlSource 对象。

### 18.1.3 LanguageDriverRegistry ###

这个类不是 LanguageDriver 的子类。

org.apache.ibatis.scripting.LanguageDriverRegistry ，LanguageDriver 注册表。

````
public class LanguageDriverRegistry {

    /**
     * LanguageDriver 映射
     */
    private final Map<Class<? extends LanguageDriver>, LanguageDriver> LANGUAGE_DRIVER_MAP = new HashMap<>();
    /**
     * 默认的 LanguageDriver 类
     */
    private Class<? extends LanguageDriver> defaultDriverClass;

    public void register(Class<? extends LanguageDriver> cls) {
        if (cls == null) {
            throw new IllegalArgumentException("null is not a valid Language Driver");
        }
        // 创建 cls 对应的对象，并添加到 LANGUAGE_DRIVER_MAP 中
        if (!LANGUAGE_DRIVER_MAP.containsKey(cls)) {
            try {
                LANGUAGE_DRIVER_MAP.put(cls, cls.newInstance());
            } catch (Exception ex) {
                throw new ScriptingException("Failed to load language driver for " + cls.getName(), ex);
            }
        }
    }

    public void register(LanguageDriver instance) {
        if (instance == null) {
            throw new IllegalArgumentException("null is not a valid Language Driver");
        }
        // 添加到 LANGUAGE_DRIVER_MAP 中
        Class<? extends LanguageDriver> cls = instance.getClass();
        if (!LANGUAGE_DRIVER_MAP.containsKey(cls)) {
            LANGUAGE_DRIVER_MAP.put(cls, instance);
        }
    }

    public LanguageDriver getDriver(Class<? extends LanguageDriver> cls) {
        return LANGUAGE_DRIVER_MAP.get(cls);
    }

    public LanguageDriver getDefaultDriver() {
        return getDriver(getDefaultDriverClass());
    }

    public Class<? extends LanguageDriver> getDefaultDriverClass() {
        return defaultDriverClass;
    }

    /**
     * 设置 {@link #defaultDriverClass}
     *
     * @param defaultDriverClass 默认的 LanguageDriver 类
     */
    public void setDefaultDriverClass(Class<? extends LanguageDriver> defaultDriverClass) {
        // 注册到 LANGUAGE_DRIVER_MAP 中
        register(defaultDriverClass);
        // 设置 defaultDriverClass 属性
        this.defaultDriverClass = defaultDriverClass;
    }

}
````

#### 18.1.3.1 初始化 ####
在 Configuration 的构造方法中，会进行初始化。

````
protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();

public Configuration() {
    // ... 省略其它代码
    
    // 注册到 languageRegistry 中
    languageRegistry.setDefaultDriverClass(XMLLanguageDriver.class);
    languageRegistry.register(RawLanguageDriver.class);
}
````
默认情况下，使用 XMLLanguageDriver 类。
大多数情况下，我们不会去设置使用的 LanguageDriver 类，而是使用 XMLLanguageDriver 类。从 #getLanguageDriver(Class<? extends LanguageDriver> langClass) 方法，可知。
````
public LanguageDriver getLanguageDriver(Class<? extends LanguageDriver> langClass) {
    // 获得 langClass 类
    if (langClass != null) {
        configuration.getLanguageRegistry().register(langClass);
    } else { // 如果为空，则使用默认类
        langClass = configuration.getLanguageRegistry().getDefaultDriverClass();
    }
    // 获得 LanguageDriver 对象
    return configuration.getLanguageRegistry().getDriver(langClass);
}
````

## 18.2 XMLScriptBuilder ##
org.apache.ibatis.scripting.xmltags.XMLScriptBuilder ，继承 BaseBuilder 抽象类，XML 动态语句( SQL )构建器，负责将 SQL 解析成 SqlSource 对象。

### 18.2.1 构造方法 ###
````
/**
 * 当前 SQL 的 XNode 对象
 */
private final XNode context;
/**
 * 是否为动态 SQL
 */
private boolean isDynamic;
/**
 * SQL 方法类型
 */
private final Class<?> parameterType;
/**
 * NodeNodeHandler 的映射
 */
private final Map<String, NodeHandler> nodeHandlerMap = new HashMap<>();

public XMLScriptBuilder(Configuration configuration, XNode context, Class<?> parameterType) {
    super(configuration);
    this.context = context;
    this.parameterType = parameterType;
    // 初始化 nodeHandlerMap 属性
    initNodeHandlerMap();
}
````
调用 #initNodeHandlerMap() 方法，初始化 nodeHandlerMap 属性。代码如下：
````
private void initNodeHandlerMap() {
    nodeHandlerMap.put("trim", new TrimHandler());
    nodeHandlerMap.put("where", new WhereHandler());
    nodeHandlerMap.put("set", new SetHandler());
    nodeHandlerMap.put("foreach", new ForEachHandler());
    nodeHandlerMap.put("if", new IfHandler());
    nodeHandlerMap.put("choose", new ChooseHandler());
    nodeHandlerMap.put("when", new IfHandler());
    nodeHandlerMap.put("otherwise", new OtherwiseHandler());
    nodeHandlerMap.put("bind", new BindHandler());
}
````
- 可以看到，nodeHandlerMap 的 KEY 是熟悉的 MyBatis 的自定义的 XML 标签。并且，每个标签对应专属的一个 NodeHandler 实现类。

### 18.2.2 parseScriptNode ###
parseScriptNode() 方法，负责将 SQL 解析成 SqlSource 对象。

````
public SqlSource parseScriptNode() {
    // <1> 解析 SQL
    MixedSqlNode rootSqlNode = parseDynamicTags(context);
    // <2> 创建 SqlSource 对象
    SqlSource sqlSource;
    if (isDynamic) {
        sqlSource = new DynamicSqlSource(configuration, rootSqlNode);
    } else {
        sqlSource = new RawSqlSource(configuration, rootSqlNode, parameterType);
    }
    return sqlSource;
}
````
<1> 方法，调用 #parseDynamicTags(XNode node) 方法，解析 SQL 成 MixedSqlNode 对象。
<2> 方法，根据是否是动态 SQL ，创建对应的 DynamicSqlSource 或 RawSqlSource 对象。

### 18.2.3 parseDynamicTags ###
parseDynamicTags(XNode node) 方法，解析 SQL 成 MixedSqlNode 对象。

````
protected MixedSqlNode parseDynamicTags(XNode node) {
    // <1> 创建 SqlNode 数组
    List<SqlNode> contents = new ArrayList<>();
    // <2> 遍历 SQL 节点的所有子节点
    NodeList children = node.getNode().getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
        // 当前子节点
        XNode child = node.newXNode(children.item(i));
        // <2.1> 如果类型是 Node.CDATA_SECTION_NODE 或者 Node.TEXT_NODE 时
        if (child.getNode().getNodeType() == Node.CDATA_SECTION_NODE || child.getNode().getNodeType() == Node.TEXT_NODE) {
            // <2.1.1> 获得内容
            String data = child.getStringBody("");
            // <2.1.2> 创建 TextSqlNode 对象
            TextSqlNode textSqlNode = new TextSqlNode(data);
            // <2.1.2.1> 如果是动态的 TextSqlNode 对象
            if (textSqlNode.isDynamic()) {
                // 添加到 contents 中
                contents.add(textSqlNode);
                // 标记为动态 SQL
                isDynamic = true;
            // <2.1.2.2> 如果是非动态的 TextSqlNode 对象
            } else {
                // <2.1.2> 创建 StaticTextSqlNode 添加到 contents 中
                contents.add(new StaticTextSqlNode(data));
            }
        // <2.2> 如果类型是 Node.ELEMENT_NODE
        } else if (child.getNode().getNodeType() == Node.ELEMENT_NODE) { // issue #628
            // <2.2.1> 根据子节点的标签，获得对应的 NodeHandler 对象
            String nodeName = child.getNode().getNodeName();
            NodeHandler handler = nodeHandlerMap.get(nodeName);
            if (handler == null) { // 获得不到，说明是未知的标签，抛出 BuilderException 异常
                throw new BuilderException("Unknown element <" + nodeName + "> in SQL statement.");
            }
            // <2.2.2> 执行 NodeHandler 处理
            handler.handleNode(child, contents);
            // <2.2.3> 标记为动态 SQL
            isDynamic = true;
        }
    }
    // <3> 创建 MixedSqlNode 对象
    return new MixedSqlNode(contents);
}
````

例如，在解析的时候生成的内容：

![](/picture/mybatis-dynamic-runtime-map.png)

## 18.3 NodeHandler ##
NodeHandler ，在 XMLScriptBuilder 类中，Node 处理器接口。


### 18.3.1 BindHandler ###
BindHandler ，实现 NodeHandler 接口，<bind /> 标签的处理器。

使用 con cat 函数连接字符串，在 MySQL 中，这个函数支持多个参数，但在 Oracle 中只 
支持两个参数。由于不 同数据库之间的语法差异 ，如果更换数据库，有些 SQL 语句可能就需要 
重写。针对这种情况，可 以使用 bind 标签来避免由于更换数据库带来的一些麻烦。

这个类的主要作用是:解析 name、value 属性，并创建 VarDeclSqlNode 对象，最后添加到 targetContents 中。

### 18.3.2 TrimHandler ###
TrimHandler ，实现 NodeHandler 接口，<trim /> 标签的处理器。

trim标记是一个格式化的标记，可以完成set或者是where标记的功能。
在红色标记的地方不存在逗号，而且自动加了一个set前缀和where后缀，上面三个属性的意义如下，其中prefix意义如上：
- suffixoverride：去掉最后一个逗号（也可以是其他的标记，就像是上面前缀中的and一样）
- suffix：后缀


### 18.3.3 WhereHandler ###
WhereHandler ，实现 NodeHandler 接口，<where /> 标签的处理器。
从实现逻辑的思路上，和 TrimHandler 是一个套路的。

where标记的作用类似于动态sql中的set标记，他的作用主要是用来简化sql语句中where条件判断的书写的
where 标记会自动将其后第一个条件的and或者是or给忽略掉

### 18.3.4 SetHandler ###
SetHandler ，实现 NodeHandler 接口，<set /> 标签的处理器。

set标记是mybatis提供的一个智能标记，我一般将其用在修改的sql中。set标记已经自动帮助我们把最后一个逗号给去掉了

### 18.3.5 ForEachHandler ###
MyBatis的foreach标签应用于多参数的交互如：多参数（相同参数）查询、循环插入数据等，foreach标签包含collection、item、open、close、index、separator，MyBatis的foreach标签与jstl标签的使用非常相似，以下为几个属性的意思解释：

collection：参数名称，根据Mapper接口的参数名确定，也可以使用@Param注解指定参数名

item：参数调用名称，通过此属性来获取集合单项的值

open：相当于prefix，即在循环前添加前缀

close：相当于suffix，即在循环后添加后缀

index：索引、下标

separator：分隔符，每次循环完成后添加此分隔符

### 18.3.6 IfHandler ###
IfHandler ，实现 NodeHandler 接口，<if /> 标签的处理器

### 18.3.7 OtherwiseHandler ###
OtherwiseHandler ，实现 NodeHandler 接口，<otherwise /> 标签的处理器。

当 choose 中所有 when 的条件都不满则时，则执行 otherwise 中的sql。类似于Java 的 switch 语句，choose 为 switch，when 为 case，otherwise 则为 default。

### 18.3.8 ChooseHandler ###
ChooseHandler ，实现 NodeHandler 接口，<choose /> 标签的处理器。

通过组合 IfHandler 和 OtherwiseHandler 两个处理器，实现对子节点们的解析。最终，生成 ChooseSqlNode 对象。

## 18.4 DynamicContext ##
org.apache.ibatis.scripting.xmltags.DynamicContext ，动态 SQL ，用于每次执行 SQL 操作时，记录动态 SQL 处理后的最终 SQL 字符串。

### 18.4.1 构造方法 ###
````
/**
 * {@link #bindings} _parameter 的键，参数
 */
public static final String PARAMETER_OBJECT_KEY = "_parameter";
/**
 * {@link #bindings} _databaseId 的键，数据库编号
 */
public static final String DATABASE_ID_KEY = "_databaseId";

static {
    // <1.2> 设置 OGNL 的属性访问器
    OgnlRuntime.setPropertyAccessor(ContextMap.class, new ContextAccessor());
}

/**
 * 上下文的参数集合
 */
private final ContextMap bindings;
/**
 * 生成后的 SQL
 */
private final StringBuilder sqlBuilder = new StringBuilder();
/**
 * 唯一编号。在 {@link org.apache.ibatis.scripting.xmltags.XMLScriptBuilder.ForEachHandler} 使用
 */
private int uniqueNumber = 0;

// 当需要使用到 OGNL 表达式时，parameterObject 非空
public DynamicContext(Configuration configuration, Object parameterObject) {
    // <1> 初始化 bindings 参数
    if (parameterObject != null && !(parameterObject instanceof Map)) {
        MetaObject metaObject = configuration.newMetaObject(parameterObject); // <1.1>
        bindings = new ContextMap(metaObject);
    } else {
        bindings = new ContextMap(null);
    }
    // <2> 添加 bindings 的默认值
    bindings.put(PARAMETER_OBJECT_KEY, parameterObject);
    bindings.put(DATABASE_ID_KEY, configuration.getDatabaseId());
}
````

### 18.4.2 bindings 属性相关的方法 ###

可以往 bindings 属性中，添加新的 KV 键值对。
````
public Map<String, Object> getBindings() {
    return bindings;
}

public void bind(String name, Object value) {
    bindings.put(name, value);
}
````
### 18.4.3 sqlBuilder 属性相关的方法 ###
可以不断向 sqlBuilder 属性中，添加 SQL 段。
````
public void appendSql(String sql) {
    sqlBuilder.append(sql);
    sqlBuilder.append(" ");
}

public String getSql() {
    return sqlBuilder.toString().trim();
}
````

### 18.4.4 uniqueNumber 属性相关的方法 ###
每次请求，获得新的序号。

#### 18.4.5 ContextMap ####
ContextMap ，是 DynamicContext 的内部静态类，继承 HashMap 类，上下文的参数集合。
````
static class ContextMap extends HashMap<String, Object> {

    private static final long serialVersionUID = 2977601501966151582L;

    /**
     * parameter 对应的 MetaObject 对象
     */
    private MetaObject parameterMetaObject;

    public ContextMap(MetaObject parameterMetaObject) {
        this.parameterMetaObject = parameterMetaObject;
    }

    @Override
    public Object get(Object key) {
        // 如果有 key 对应的值，直接获得
        String strKey = (String) key;
        if (super.containsKey(strKey)) {
            return super.get(strKey);
        }

        // 从 parameterMetaObject 中，获得 key 对应的属性
        if (parameterMetaObject != null) {
            // issue #61 do not modify the context when reading
            return parameterMetaObject.getValue(strKey);
        }

        return null;
    }
}
````
该类在 HashMap 的基础上，增加支持对 parameterMetaObject 属性的访问。

### 18.4.6 ContextAccessor ###
ContextAccessor ，是 DynamicContext 的内部静态类，实现 ognl.PropertyAccessor 接口，上下文访问器。


## 18.5 SqlNode ##

org.apache.ibatis.scripting.xmltags.SqlNode ，SQL Node 接口，每个 XML Node 会解析成对应的 SQL Node 对象。

### 18.5.1 VarDeclSqlNode ###
org.apache.ibatis.scripting.xmltags.VarDeclSqlNode ，实现 SqlNode 接口，<bind /> 标签的 SqlNode 实现类。

````
public class VarDeclSqlNode implements SqlNode {

    /** 名字 */
    private final String name;
    /** 表达式 */
    private final String expression;

    public VarDeclSqlNode(String var, String exp) {
        name = var;
        expression = exp;
    }
    @Override
    public boolean apply(DynamicContext context) {
        // 调用 OgnlCache#getValue(String expression, Object root) 方法，获得表达式对应的值。
        final Object value = OgnlCache.getValue(expression, context.getBindings());
        // 调用 DynamicContext#bind(String name, Object value) 方法，绑定到上下文。
        context.bind(name, value);
        return true;
    }
}
````

### 18.5.2 TrimSqlNode ###
org.apache.ibatis.scripting.xmltags.TrimSqlNode ，实现 SqlNode 接口，<trim /> 标签的 SqlNode 实现类。

#### 18.5.2.1 构造方法 ####
````
/**
 * 内含的 SqlNode 节点
 */
private final SqlNode contents;
/**
 * 前缀
 */
private final String prefix;
/**
 * 后缀
 */
private final String suffix;
/**
 * 需要被删除的前缀
 */
private final List<String> prefixesToOverride;
/**
 * 需要被删除的后缀
 */
private final List<String> suffixesToOverride;
private final Configuration configuration;

public TrimSqlNode(Configuration configuration, SqlNode contents, String prefix, String prefixesToOverride, String suffix, String suffixesToOverride) {
    this(configuration, contents, prefix, parseOverrides(prefixesToOverride), suffix, parseOverrides(suffixesToOverride));
}

protected TrimSqlNode(Configuration configuration, SqlNode contents, String prefix, List<String> prefixesToOverride, String suffix, List<String> suffixesToOverride) {
    this.contents = contents;
    this.prefix = prefix;
    this.prefixesToOverride = prefixesToOverride;
    this.suffix = suffix;
    this.suffixesToOverride = suffixesToOverride;
    this.configuration = configuration;
}
````
parseOverrides(String overrides) 方法，使用 | 分隔字符串成字符串数组，并都转换成大写。

#### 18.5.2.2 apply ####
````
@Override
public boolean apply(DynamicContext context) {
    // <1> 创建 FilteredDynamicContext 对象
    FilteredDynamicContext filteredDynamicContext = new FilteredDynamicContext(context);
    // <2> 执行 contents 的应用
    boolean result = contents.apply(filteredDynamicContext);
    // <3> 执行 FilteredDynamicContext 的应用
    filteredDynamicContext.applyAll();
    return result;
}
````

#### 18.5.2.3 FilteredDynamicContext ####
FilteredDynamicContext ，是 TrimSqlNode 的内部类，继承 DynamicContext 类，支持 trim 逻辑的 DynamicContext 实现类。

### 18.5.3 WhereSqlNode ###
org.apache.ibatis.scripting.xmltags.WhereSqlNode ，继承 TrimSqlNode 类，<where /> 标签的 SqlNode 实现类。

### 18.5.4 SetSqlNode ###
org.apache.ibatis.scripting.xmltags.SetSqlNode ，继承 TrimSqlNode 类，<set /> 标签的 SqlNode 实现类。

### 18.5.5 ForEachSqlNode ###
org.apache.ibatis.scripting.xmltags.ForEachSqlNode ，实现 SqlNode 接口，<foreach /> 标签的 SqlNode 实现类。

#### 18.5.5.1 构造方法 ####
````
private final ExpressionEvaluator evaluator;
/**
 * 集合的表达式
 */
private final String collectionExpression;
private final SqlNode contents;
private final String open;
private final String close;
private final String separator;
/**
 * 集合项
 */
private final String item;
/**
 * 索引变量
 */
private final String index;
private final Configuration configuration;

public ForEachSqlNode(Configuration configuration, SqlNode contents, String collectionExpression, String index, String item, String open, String close, String separator) {
    this.evaluator = new ExpressionEvaluator();
    this.collectionExpression = collectionExpression;
    this.contents = contents;
    this.open = open;
    this.close = close;
    this.separator = separator;
    this.index = index;
    this.item = item;
    this.configuration = configuration;
}
````

#### 18.5.5.2 apply ####
````
@Override
public boolean apply(DynamicContext context) {
    Map<String, Object> bindings = context.getBindings();
    // <1> 获得遍历的集合的 Iterable 对象，用于遍历。
    final Iterable<?> iterable = evaluator.evaluateIterable(collectionExpression, bindings);
    if (!iterable.iterator().hasNext()) {
        return true;
    }
    boolean first = true;
    // <2> 添加 open 到 SQL 中
    applyOpen(context);
    int i = 0;
    for (Object o : iterable) {
        // <3> 记录原始的 context 对象
        DynamicContext oldContext = context;
        // <4> 生成新的 context
        if (first || separator == null) {
            context = new PrefixedContext(context, "");
        } else {
            context = new PrefixedContext(context, separator);
        }
        // <5> 获得唯一编号
        int uniqueNumber = context.getUniqueNumber();
        // Issue #709
        // <6> 绑定到 context 中
        if (o instanceof Map.Entry) {
            @SuppressWarnings("unchecked")
            Map.Entry<Object, Object> mapEntry = (Map.Entry<Object, Object>) o;
            applyIndex(context, mapEntry.getKey(), uniqueNumber);
            applyItem(context, mapEntry.getValue(), uniqueNumber);
        } else {
            applyIndex(context, i, uniqueNumber);
            applyItem(context, o, uniqueNumber);
        }
        // <7> 执行 contents 的应用
        contents.apply(new FilteredDynamicContext(configuration, context, index, item, uniqueNumber));
        // <8> 判断 prefix 是否已经插入
        if (first) {
            first = !((PrefixedContext) context).isPrefixApplied();
        }
        // <9> 恢复原始的 context 对象
        context = oldContext;
        i++;
    }
    // <10> 添加 close 到 SQL 中
    applyClose(context);
    // <11> 移除 index 和 item 对应的绑定
    context.getBindings().remove(item);
    context.getBindings().remove(index);
    return true;
}
````

例如一个查询的sql为：
````
<select id="getSubjectList" parameterType="List" resultType="List">
    SELECT id FROM subject
    WHERE id IN
      <foreach collection="ids" index="idx" item="item" open="("  close=")" separator=",">
          #{item}
      </foreach>
</select>
````
生成的变量模型为：
![](/picture/mybatis-foreach-rumtime-map.png)

#### 18.5.5.3 PrefixedContext ####
PrefixedContext ，是 ForEachSqlNode 的内部类，继承 DynamicContext 类，支持添加 <foreach /> 标签中，多个元素之间的分隔符的 DynamicContext 实现类。

prefix 属性，虽然属性命名上是 prefix ，但是对应到 ForEachSqlNode 的 separator 属性。
重心在于 #appendSql(String sql) 方法的实现。逻辑还是比较简单的，就是判断之前是否添加过 prefix ，没有就进行添加。而判断的依据，就是 prefixApplied 标识。

### 18.5.6 IfSqlNode ###
org.apache.ibatis.scripting.xmltags.IfSqlNode ，实现 SqlNode 接口，<if /> 标签的 SqlNode 实现类。

````
public class IfSqlNode implements SqlNode {

    private final ExpressionEvaluator evaluator;
    /**
     * 判断表达式
     */
    private final String test;
    /**
     * 内嵌的 SqlNode 节点
     */
    private final SqlNode contents;

    public IfSqlNode(SqlNode contents, String test) {
        this.test = test;
        this.contents = contents;
        this.evaluator = new ExpressionEvaluator();
    }

    @Override
    public boolean apply(DynamicContext context) {
        // 调用 ExpressionEvaluator#evaluateBoolean(String expression, Object parameterObject) 方法，判断是否符合条件。
        if (evaluator.evaluateBoolean(test, context.getBindings())) {
            // 如果符合条件，则执行 contents 的应用，并返回成功 true 。
            contents.apply(context);
            // 返回成功
            return true;
        }
        // 如果不符条件，则返回失败 false 。
        return false;
    }
}
````

### 18.5.7 ChooseSqlNode ###
org.apache.ibatis.scripting.xmltags.ChooseSqlNode ，实现 SqlNode 接口，<choose /> 标签的 SqlNode 实现类。

````
public class ChooseSqlNode implements SqlNode {

    /**
     * <otherwise /> 标签对应的 SqlNode 节点
     */
    private final SqlNode defaultSqlNode;
    /**
     * <when /> 标签对应的 SqlNode 节点数组
     */
    private final List<SqlNode> ifSqlNodes;

    public ChooseSqlNode(List<SqlNode> ifSqlNodes, SqlNode defaultSqlNode) {
        this.ifSqlNodes = ifSqlNodes;
        this.defaultSqlNode = defaultSqlNode;
    }
    @Override
    public boolean apply(DynamicContext context) {
        // 判断 <when /> 标签中，是否有符合条件的节点。如果有，则进行应用。并且只因应用一个 SqlNode 对象。这里，我们就看到了，SqlNode#apply(context) 方法，返回 true 或 false 的用途了。
        // 如果有，则进行应用。并且只因应用一个 SqlNode 对象
        for (SqlNode sqlNode : ifSqlNodes) {
            if (sqlNode.apply(context)) {
                return true;
            }
        }
        // 再判断 <otherwise /> 标签，是否存在。如果存在，则进行应用。
        // 如果存在，则进行应用
        if (defaultSqlNode != null) {
            defaultSqlNode.apply(context);
            return true;
        }
        // 返回都失败。
        return false;
    }
}
````

### 18.5.8 StaticTextSqlNode ###
org.apache.ibatis.scripting.xmltags.StaticTextSqlNode ，实现 SqlNode 接口，静态文本的 SqlNode 实现类。
````
public class StaticTextSqlNode implements SqlNode {

    /**
     * 静态文本
     */
    private final String text;

    public StaticTextSqlNode(String text) {
        this.text = text;
    }
    @Override
    public boolean apply(DynamicContext context) {
        // 直接拼接到 context 中
        context.appendSql(text);
        return true;
    }
}
````

### 18.5.9 TextSqlNode ###

org.apache.ibatis.scripting.xmltags.TextSqlNode ，实现 SqlNode 接口，文本的 SqlNode 实现类。相比 StaticTextSqlNode 的实现来说，TextSqlNode 不确定是否为静态文本，所以提供 #isDynamic() 方法，进行判断是否为动态文本。

#### 18.5.9.1 构造方法 ####
````
/**
 * 文本
 */
private final String text;
/**
 * 目前该属性只在单元测试中使用，暂时无视
 */
private final Pattern injectionFilter;

public TextSqlNode(String text) {
    this(text, null);
}
public TextSqlNode(String text, Pattern injectionFilter) {
    this.text = text;
    this.injectionFilter = injectionFilter;
}
````

#### 18.5.9.2 isDynamic ####
isDynamic() 方法，判断是否为动态文本。
````
public boolean isDynamic() {
    // <1> 创建 DynamicCheckerTokenParser 对象
    DynamicCheckerTokenParser checker = new DynamicCheckerTokenParser();
    // <2> 创建 GenericTokenParser 对象
    GenericTokenParser parser = createParser(checker);
    // <3> 执行解析
    parser.parse(text);
    // <4> 判断是否为动态文本
    return checker.isDynamic();
}
```
#### 18.5.9.3 apply ####

````
@Override
public boolean apply(DynamicContext context) {
    // <1> 创建 BindingTokenParser 对象
    // <2> 创建 GenericTokenParser 对象
    GenericTokenParser parser = createParser(new BindingTokenParser(context, injectionFilter));
    // <3> 执行解析
    // <4> 将解析的结果，添加到 context 中
    context.appendSql(parser.parse(text));
    return true;
}
````
**最终的结果是把xml文件的内容解析成对象，以及表达式，在调用的时候，根据对象和表达式动态生成sql**

### 18.5.10 MixedSqlNode ###

org.apache.ibatis.scripting.xmltags.MixedSqlNode ，实现 SqlNode 接口，混合的 SqlNode 实现类

## 18.6 OGNL 相关 ##
### 18.6.1 OgnlCache ###
org.apache.ibatis.scripting.xmltags.OgnlCache ，OGNL 缓存类。

````
public final class OgnlCache {

    /** OgnlMemberAccess 单例 */
    private static final OgnlMemberAccess MEMBER_ACCESS = new OgnlMemberAccess();
    /** OgnlClassResolver 单例 */
    private static final OgnlClassResolver CLASS_RESOLVER = new OgnlClassResolver();

    /**
     * 表达式的缓存的映射
     * KEY：表达式
     * VALUE：表达式的缓存 @see #parseExpression(String)
     */
    private static final Map<String, Object> expressionCache = new ConcurrentHashMap<>();

    private OgnlCache() {
        // Prevent Instantiation of Static Class
    }

    public static Object getValue(String expression, Object root) {
        try {
            // <1> 创建 OGNL Context 对象
            Map context = Ognl.createDefaultContext(root, MEMBER_ACCESS, CLASS_RESOLVER, null);
            // <2> 解析表达式
            // <3> 获得表达式对应的值
            return Ognl.getValue(parseExpression(expression), context, root);
        } catch (OgnlException e) {
            throw new BuilderException("Error evaluating expression '" + expression + "'. Cause: " + e, e);
        }
    }

    private static Object parseExpression(String expression) throws OgnlException {
        Object node = expressionCache.get(expression);
        if (node == null) {
            node = Ognl.parseExpression(expression);
            expressionCache.put(expression, node);
        }
        return node;
    }
}
````

### 18.6.2 OgnlMemberAccess ###
org.apache.ibatis.scripting.xmltags.OgnlMemberAccess，实现 ognl.MemberAccess 接口，OGNL 成员访问器实现类。

### 18.6.3 OgnlClassResolver ###
org.apache.ibatis.scripting.xmltags.OgnlClassResolver，继承 ognl.DefaultClassResolver 类，OGNL 类解析器实现类。

### 18.6.4 ExpressionEvaluator ###
org.apache.ibatis.scripting.xmltags.ExpressionEvaluator ，OGNL 表达式计算器。

````
public class ExpressionEvaluator {

    /**
     * 判断表达式对应的值，是否为 true
     *
     * @param expression 表达式
     * @param parameterObject 参数对象
     * @return 是否为 true
     */
    public boolean evaluateBoolean(String expression, Object parameterObject) {
        // 获得表达式对应的值
        Object value = OgnlCache.getValue(expression, parameterObject);
        // 如果是 Boolean 类型，直接判断
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        // 如果是 Number 类型，则判断不等于 0
        if (value instanceof Number) {
            return new BigDecimal(String.valueOf(value)).compareTo(BigDecimal.ZERO) != 0;
        }
        // 如果是其它类型，判断非空
        return value != null;
    }

    /**
     * 获得表达式对应的集合
     *
     * @param expression 表达式
     * @param parameterObject 参数对象
     * @return 迭代器对象
     */
    public Iterable<?> evaluateIterable(String expression, Object parameterObject) {
        // 获得表达式对应的值
        Object value = OgnlCache.getValue(expression, parameterObject);
        if (value == null) {
            throw new BuilderException("The expression '" + expression + "' evaluated to a null value.");
        }
        // 如果是 Iterable 类型，直接返回
        if (value instanceof Iterable) {
            return (Iterable<?>) value;
        }
        // 如果是数组类型，则返回数组
        if (value.getClass().isArray()) {
            // the array may be primitive, so Arrays.asList() may throw
            // a ClassCastException (issue 209).  Do the work manually
            // Curse primitives! :) (JGB)
            int size = Array.getLength(value);
            List<Object> answer = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                Object o = Array.get(value, i);
                answer.add(o);
            }
            return answer;
        }
        // 如果是 Map 类型，则返回 Map.entrySet 集合
        if (value instanceof Map) {
            return ((Map) value).entrySet();
        }
        throw new BuilderException("Error evaluating expression '" + expression + "'.  Return value (" + value + ") was not iterable.");
    }
}
````













----