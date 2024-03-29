1 项目结构一览 #
## 1.1 整体架构 ##
MyBatis 的整体架构分为三层：

- 基础支持层
- 核心处理层
- 接口层

如下图表示：

![](/picture/mybatis-overall-architecture.png)

### 1.1.1 基础支持层 ###
基础支持层，包含整个 MyBatis 的基础模块，这些模块为核心处理层的功能提供了良好的支撑。

#### 1.1.1.1 反射模块 ####
对应 reflection 包。

#### 1.1.1.2 类型模块 ####
对应 type 包。
1. MyBatis 为简化配置文件提供了别名机制，该机制是类型转换模块的主要功能之一。
2. 类型转换模块的另一个功能是实现 JDBC 类型与 Java 类型之间的转换，该功能在为 SQL 语句绑定实参以及映射查询结果集时都会涉及：
	- 在为 SQL 语句绑定实参时，会将数据由 Java 类型转换成 JDBC 类型。
	- 而在映射结果集时，会将数据由 JDBC 类型转换成 Java 类型。

#### 1.1.1.3 日志模块 ####
对应 logging 包。

#### 1.1.1.4 IO 模块 ####
对应 io 包。

资源加载模块，主要是对类加载器进行封装，确定类加载器的使用顺序，并提供了加载类文件以及其他资源文件的功能 。

#### 1.1.1.5 解析器模块 ####
对应 parsing 包。
解析器模块，主要提供了两个功能:

- 一个功能，是对 XPath 进行封装，为 MyBatis 初始化时解析 mybatis-config.xml 配置文件以及映射配置文件提供支持。
- 另一个功能，是为处理动态 SQL 语句中的占位符提供支持。

#### 1.1.1.6 数据源模块 ####
对应 datasource 包。

数据源是实际开发中常用的组件之一。现在开源的数据源都提供了比较丰富的功能，例如，连接池功能、检测连接状态等，选择性能优秀的数据源组件对于提升 ORM 框架乃至整个应用的性能都是非常重要的。

#### 1.1.1.7 事务模块 ####
对应 transaction 包。

#### 1.1.1.8 缓存模块 ####
对应 cache 包。

在优化系统性能时，优化数据库性能是非常重要的一个环节，而添加缓存则是优化数据库时最有效的手段之一。正确、合理地使用缓存可以将一部分数据库请求拦截在缓存这一层。
MyBatis 中提供了一级缓存和二级缓存，而这两级缓存都是依赖于基础支持层中的缓 存模块实现的。这里需要读者注意的是，MyBatis 中自带的这两级缓存与 MyBatis 以及整个应用是运行在同一个 JVM 中的，共享同一块堆内存。如果这两级缓存中的数据量较大， 则可能影响系统中其他功能的运行，所以当需要缓存大量数据时，优先考虑使用 Redis、Memcache 等缓存产品。

#### 1.1.1.9 Binding 模块 ####
对应 binding 包。

在调用 SqlSession 相应方法执行数据库操作时，需要指定映射文件中定义的 SQL 节点，如果出现拼写错误，我们只能在运行时才能发现相应的异常。为了尽早发现这种错误，MyBatis 通过 Binding 模块，将用户自定义的 Mapper 接口与映射配置文件关联起来，系统可以通过调用自定义 Mapper 接口中的方法执行相应的 SQL 语句完成数据库操作，从而避免上述问题。

值得读者注意的是，开发人员无须编写自定义 Mapper 接口的实现，MyBatis 会自动为其创建动态代理对象。在有些场景中，自定义 Mapper 接口可以完全代替映射配置文件，但有的映射规则和 SQL 语句的定义还是写在映射配置文件中比较方便，例如动态 SQL 语句的定义。

#### 1.1.1.10 注解模块 ####
对应 annotations 包。

MyBatis 提供了注解的方式，使得我们方便的在 Mapper 接口上编写简单的数据库 SQL 操作代码，而无需像之前一样，必须编写 SQL 在 XML 格式的 Mapper 文件中。

#### 1.1.1.11 异常模块 ####
对应 exceptions 包。

定义了 MyBatis 专有的 PersistenceException 和 TooManyResultsException 异常。

### 1.1.2 核心处理层 ###
在核心处理层中，实现了 MyBatis 的核心处理流程，其中包括 MyBatis 的初始化以及完成一次数据库操作的涉及的全部流程 。

#### 1.1.2.1 配置解析 ####
对应 builder 和 mapping 模块。前者为配置解析过程，后者主要为 SQL 操作解析后的映射。

在 MyBatis 初始化过程中，会加载 mybatis-config.xml 配置文件、映射配置文件以及 Mapper 接口中的注解信息，解析后的配置信息会形成相应的对象并保存到 Configuration 对象中。例如：

- <resultMap>节点(即 ResultSet 的映射规则) 会被解析成 ResultMap 对象。
- <result> 节点(即属性映射)会被解析成 ResultMapping 对象。z

之后，利用该 Configuration 对象创建 SqlSessionFactory对象。待 MyBatis 初始化之后，开发人员可以通过初始化得到 SqlSessionFactory 创建 SqlSession 对象并完成数据库操作。

#### 1.1.2.2 SQL 解析 ####
对应 scripting 模块。

拼凑 SQL 语句是一件烦琐且易出错的过程，为了将开发人员从这项枯燥无趣的工作中 解脱出来，MyBatis 实现动态 SQL 语句的功能，提供了多种动态 SQL语句对应的节点。例如<where> 节点、<if> 节点、<foreach> 节点等 。通过这些节点的组合使用， 开发人 员可以写出几乎满足所有需求的动态 SQL 语句。

MyBatis 中的 scripting 模块，会根据用户传入的实参，解析映射文件中定义的动态 SQL 节点，并形成数据库可执行的 SQL 语句。之后会处理 SQL 语句中的占位符，绑定用户传入的实参。

#### 1.1.2.3 SQL 执行 ####
对应 executor 和 cursor 模块。前者对应执行器，后者对应执行结果的游标。

SQL 语句的执行涉及多个组件 ，其中比较重要的是 Executor、StatementHandler、ParameterHandler 和 ResultSetHandler 。

- Executor 主要负责维护一级缓存和二级缓存，并提供事务管理的相关操作，它会将数据库相关操作委托给 StatementHandler完成。
- StatementHandler 首先通过 ParameterHandler 完成 SQL 语句的实参绑定，然后通过 java.sql.Statement 对象执行 SQL 语句并得到结果集，最后通过 ResultSetHandler 完成结果集的映射，得到结果对象并返回。

整体过程如下图所示：
![](/picture/mybatis-sql-flow.png)

#### 1.1.2.4 插件层 ####
对应 plugin 模块。

Mybatis 自身的功能虽然强大，但是并不能完美切合所有的应用场景，因此 MyBatis 提供了插件接口，我们可以通过添加用户自定义插件的方式对 MyBatis 进行扩展。用户自定义插件也可以改变 Mybatis 的默认行为，例如，我们可以拦截 SQL 语句并对其进行重写。

### 1.1.3 接口层 ###
对应 session 模块。
接口层相对简单，其核心是 SqlSession 接口，该接口中定义了 MyBatis 暴露给应用程序调用的 API，也就是上层应用与 MyBatis 交互的桥梁。接口层在接收到调用请求时，会调用核心处理层的相应模块来完成具体的数据库操作。

### 1.1.4 其它层 ###
#### 1.1.4.1 JDBC 模块 ####
对应 jdbc 包。

#### 1.1.4.1 Lang 模块 ####
对应 lang 包。

------


# 2 解析器模块 #

解析器模块，主要提供了两个功能:

一个功能，是对 XPath 进行封装，为 MyBatis 初始化时解析 mybatis-config.xml 配置文件以及映射配置文件提供支持。
另一个功能，是为处理动态 SQL 语句中的占位符提供支持。

## 2.1 XPathParser ##
org.apache.ibatis.parsing.XPathParser ，基于 Java XPath 解析器，用于解析 MyBatis mybatis-config.xml 和 **Mapper.xml 等 XML 配置文件。

````
/**
 * XML Document 对象
 * XML 被解析后，生成的 org.w3c.dom.Document 对象。
 */
private final Document document;
/**
 * 是否校验
 */
private boolean validation;
/**
 * XML 实体解析器
 * org.xml.sax.EntityResolver 对象，XML 实体解析器。默认情况下，对 XML 进行校验时，会基于 XML 文档开始位置指定的 DTD 文件或 XSD 文件。例如说，解析 mybatis-config.xml 配置文件时，会加载 http://mybatis.org/dtd/mybatis-3-config.dtd 这个 DTD 文件。但是，如果每个应用启动都从网络加载该 DTD 文件，势必在弱网络下体验非常下，甚至说应用部署在无网络的环境下，还会导致下载不下来，那么就会出现 XML 校验失败的情况。所以，在实际场景下，MyBatis 自定义了 EntityResolver 的实现，达到使用本地 DTD 文件，从而避免下载网络 DTD 文件的效果。
 */
private EntityResolver entityResolver;
/**
 * 变量 Properties 对象
 * 用来替换需要动态配置的属性值。
 * variables 的来源，即可以在常用的 Java Properties 文件中配置，也可以使用 MyBatis <property /> 标签中配置。
 */
private Properties variables;
/**
 * Java XPath 对象
 * javax.xml.xpath.XPath 对象，用于查询 XML 中的节点和元素。
 */
private XPath xpath;
````

### 2.1.1 构造方法 ###

````
/**
 * 构造 XPathParser 对象
 *
 * @param xml XML 文件地址
 * @param validation 是否校验 XML
 * @param variables 变量 Properties 对象
 * @param entityResolver XML 实体解析器
 */
public XPathParser(String xml, boolean validation, Properties variables, EntityResolver entityResolver) {
    commonConstructor(validation, variables, entityResolver);
    this.document = createDocument(new InputSource(new StringReader(xml)));
}
````
基本逻辑：
1. 调用 #commonConstructor(boolean validation, Properties variables, EntityResolver entityResolver) 方法，公用的构造方法逻辑。
2. commonConstructor(validation, variables, entityResolver) 调用 #createDocument(InputSource inputSource) 方法，将 XML 文件解析成 Document 对象。

### 2.1.2 eval 方法族 ###
XPathParser 提供了一系列的 #eval* 方法，用于获得 Boolean、Short、Integer、Long、Float、Double、String、Node 类型的元素或节点的“值”。当然，虽然方法很多，但是都是基于 #evaluate(String expression, Object root, QName returnType) 方法

evaluate(String expression, Object root, QName returnType)函数：调用 xpath 的 evaluate(String expression, Object root, QName returnType) 方法，获得指定元素或节点的值。

#### 2.1.2.1 eval 元素 ####
eval 元素的方法，用于获得 Boolean、Short、Integer、Long、Float、Double、String 类型的元素的值。例如#evalString(Object root, String expression)函数

````
public String evalString(Object root, String expression) {
    String result = (String) evaluate(expression, root, XPathConstants.STRING);
    result = PropertyParser.parse(result, variables);
    return result;
}
````
基本流程为：
1. 调用 #evaluate(String expression, Object root, QName returnType) 方法，获得值。其中，returnType 方法传入的是 XPathConstants.STRING ，表示返回的值是 String 类型。
2. 调用 PropertyParser#parse(String string, Properties variables) 方法，基于 variables 替换动态值，如果 result 为动态值。这就是 MyBatis 如何替换掉 XML 中的动态值实现的方式。

#### 2.1.2.2 eval 节点 ####
eval 元素的方法，用于获得 Node 类型的节点的值。

主要流程：
1. 返回结果有 Node 对象和数组两种情况，根据方法参数 expression 需要获取的节点不同。
2. 最终结果会将 Node 封装成 org.apache.ibatis.parsing.XNode 对象，主要为了动态值的替换。

## 2.2 XMLMapperEntityResolver ##
org.apache.ibatis.builder.xml.XMLMapperEntityResolver ，实现 EntityResolver 接口，MyBatis 自定义 EntityResolver 实现类，用于加载本地的 mybatis-3-config.dtd 和 mybatis-3-mapper.dtd 这两个 DTD 文件。

## 2.3 GenericTokenParser ##

org.apache.ibatis.parsing.GenericTokenParser ，通用的 Token 解析器。

即通配符解析器。#和$匹配，比如openToken为"${" ，closeToken为"}",openToken和closeToken中间截取的值为表达式，根据表达式来获取实际的值，并把值放入字符串中，组成实际的值。

## 2.4 PropertyParser ##
org.apache.ibatis.parsing.PropertyParser ，动态属性解析器。

parse(String string, Properties variables)函数：
基本流程：
1. 创建 VariableTokenHandler 对象。
2. 创建 GenericTokenParser 对象。
3. 调用 GenericTokenParser#parse(String text) 方法，执行解析。

## 2.5 TokenHandler ##
org.apache.ibatis.parsing.TokenHandler ，Token 处理器接口。

handleToken(String content) 方法，处理 Token 

TokenHandler 有四个子类实现

### 2.5.1 VariableTokenHandler ###
VariableTokenHandler ，是 PropertyParser 的内部静态类，变量 Token 处理器。

#### 2.5.1.1 构造方法 ####

------

# 3 反射模块 #

## 3.1 Reflector ##
org.apache.ibatis.reflection.Reflector ，反射器，每个 Reflector 对应一个类。Reflector 会缓存反射操作需要的类的信息，例如：构造方法、属性名、setting / getting 方法等等。


````
public class Reflector {

    /**
     * 每个 Reflector 对应的类
     */
    private final Class<?> type;
    /**
     * 可读属性数组
     */
    private final String[] readablePropertyNames;
    /**
     * 可写属性集合
     */
    private final String[] writeablePropertyNames;
    /**
     * 属性对应的 setting 方法的映射。
     *
     * key 为属性名称
     * value 为 Invoker 对象
     */
    private final Map<String, Invoker> setMethods = new HashMap<>();
    /**
     * 属性对应的 getting 方法的映射。
     *
     * key 为属性名称
     * value 为 Invoker 对象
     */
    private final Map<String, Invoker> getMethods = new HashMap<>();
    /**
     * 属性对应的 setting 方法的方法参数类型的映射。{@link #setMethods}
     *
     * key 为属性名称
     * value 为方法参数类型
     */
    private final Map<String, Class<?>> setTypes = new HashMap<>();
    /**
     * 属性对应的 getting 方法的返回值类型的映射。{@link #getMethods}
     *
     * key 为属性名称
     * value 为返回值的类型
     */
    private final Map<String, Class<?>> getTypes = new HashMap<>();
    /**
     * 默认无参构造方法
     */
    private Constructor<?> defaultConstructor;
    /**
     * 不区分大小写的属性集合
     */
    private Map<String, String> caseInsensitivePropertyMap = new HashMap<>();

    public Reflector(Class<?> clazz) {
        // 设置对应的类
        type = clazz;
        // <1> 初始化 defaultConstructor
        addDefaultConstructor(clazz);
        // <2> // 初始化 getMethods 和 getTypes ，通过遍历 getting 方法
        addGetMethods(clazz);
        // <3> // 初始化 setMethods 和 setTypes ，通过遍历 setting 方法。
        addSetMethods(clazz);
        // <4> // 初始化 getMethods + getTypes 和 setMethods + setTypes ，通过遍历 fields 属性。
        addFields(clazz);
        // <5> 初始化 readablePropertyNames、writeablePropertyNames、caseInsensitivePropertyMap 属性
        readablePropertyNames = getMethods.keySet().toArray(new String[getMethods.keySet().size()]);
        writeablePropertyNames = setMethods.keySet().toArray(new String[setMethods.keySet().size()]);
        for (String propName : readablePropertyNames) {
            caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
        }
        for (String propName : writeablePropertyNames) {
            caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
        }
    }
    
    // ... 省略一些方法
}
````

### 3.1.1 addDefaultConstructor ###
addDefaultConstructor(Class<?> clazz) 方法，查找默认无参构造方法。
````
private void addDefaultConstructor(Class<?> clazz) {
    // 获得所有构造方法
    Constructor<?>[] consts = clazz.getDeclaredConstructors();
    // 遍历所有构造方法，查找无参的构造方法
    for (Constructor<?> constructor : consts) {
        // 判断无参的构造方法
        if (constructor.getParameterTypes().length == 0) {
            // 设置构造方法可以访问，避免是 private 等修饰符
            if (canControlMemberAccessible()) {
                try {
                    constructor.setAccessible(true);
                } catch (Exception e) {
                    // Ignored. This is only a final precaution, nothing we can do.
                }
            }
            // 如果构造方法可以访问，赋值给 defaultConstructor
            if (constructor.isAccessible()) {
                this.defaultConstructor = constructor;
            }
        }
    }
}

/**
 * Checks whether can control member accessible.
 *
 * 判断，是否可以修改可访问性
 *
 * @return If can control member accessible, it return {@literal true}
 * @since 3.5.0
 */
public static boolean canControlMemberAccessible() {
    try {
        SecurityManager securityManager = System.getSecurityManager();
        if (null != securityManager) {
            securityManager.checkPermission(new ReflectPermission("suppressAccessChecks"));
        }
    } catch (SecurityException e) {
        return false;
    }
    return true;
}
````
设置class的默认构造方法，为public修饰并且赋值给defaultConstructor属性。

### 3.1.2 addGetMethods ###
addGetMethods(Class<?> cls) 方法，初始化 getMethods 和 getTypes ，通过遍历 getting 方法。

````
private void addGetMethods(Class<?> cls) {
    // <1> 属性与其 getting 方法的映射。conflictingGetters 变量，属性与其 getting 方法的映射。因为父类和子类都可能定义了相同属性的 getting 方法，所以 VALUE 会是个数组。
    Map<String, List<Method>> conflictingGetters = new HashMap<>();
    // <2> 调用 #getClassMethods(Class<?> cls) 方法，获得所有方法。
    Method[] methods = getClassMethods(cls);
    // <3> 遍历所有方法，挑选符合的 getting 方法，添加到 conflictingGetters 中。
    for (Method method : methods) {
        // <3.1> 参数大于 0 ，说明不是 getting 方法，忽略
        if (method.getParameterTypes().length > 0) {
            continue;
        }
        // <3.2> 以 get 和 is 方法名开头，说明是 getting 方法
        String name = method.getName();
        if ((name.startsWith("get") && name.length() > 3)
                || (name.startsWith("is") && name.length() > 2)) {
            // <3.3> 获得属性
            name = PropertyNamer.methodToProperty(name);
            // <3.4> 添加到 conflictingGetters 中
            addMethodConflict(conflictingGetters, name, method);
        }
    }
    // <4> 解决 getting 冲突方法
    resolveGetterConflicts(conflictingGetters);
}
````

````
/**
 * 解决方法冲突
private void addMethodConflict(Map<String, List<Method>> conflictingMethods, String name, Method method) {
    List<Method> list = conflictingMethods.computeIfAbsent(name, k -> new ArrayList<>());
    list.add(method);
}
````
代码说明：如果获取的name的值为空，则用k代替。Map.computeIfAbsent()函数会检测第一个参数的值，如果为空，则用第二个参数代替

#### 3.1.2.1 getClassMethods ####
getClassMethods(Class<?> cls) 方法，获得所有方法，包括父类的所有方法，

#### 3.1.2.2 resolveGetterConflicts ####

resolveGetterConflicts(Map<String, List<Method>>) 方法，解决 getting 冲突方法。最终，一个属性，只保留一个对应的方法。

````
private void resolveGetterConflicts(Map<String, List<Method>> conflictingGetters) {
    // 遍历每个属性，查找其最匹配的方法。因为子类可以覆写父类的方法，所以一个属性，可能对应多个 getting 方法
    for (Entry<String, List<Method>> entry : conflictingGetters.entrySet()) {
        Method winner = null; // 最匹配的方法
        String propName = entry.getKey();
        for (Method candidate : entry.getValue()) {
            // winner 为空，说明 candidate 为最匹配的方法
            if (winner == null) {
                winner = candidate;
                continue;
            }
            // <1> 基于返回类型比较
            Class<?> winnerType = winner.getReturnType();
            Class<?> candidateType = candidate.getReturnType();
            // 类型相同
            if (candidateType.equals(winnerType)) {
                // 返回值选哪个相同，应该在 getClassMethods 方法中，已经合并。所以抛出 ReflectionException 异常
                if (!boolean.class.equals(candidateType)) {
                    throw new ReflectionException(
                            "Illegal overloaded getter method with ambiguous type for property "
                                    + propName + " in class " + winner.getDeclaringClass()
                                    + ". This breaks the JavaBeans specification and can cause unpredictable results.");
                // 选择 boolean 类型的 is 方法
                } else if (candidate.getName().startsWith("is")) {
                    winner = candidate;
                }
            // 不符合选择子类
            } else if (candidateType.isAssignableFrom(winnerType)) {
                // OK getter type is descendant
            // <1.1> 符合选择子类。因为子类可以修改放大返回值。例如，父类的一个方法的返回值为 List ，子类对该方法的返回值可以覆写为 ArrayList 。
            } else if (winnerType.isAssignableFrom(candidateType)) {
                winner = candidate;
            // <1.2> 返回类型冲突，抛出 ReflectionException 异常
            } else {
                throw new ReflectionException(
                        "Illegal overloaded getter method with ambiguous type for property "
                                + propName + " in class " + winner.getDeclaringClass()
                                + ". This breaks the JavaBeans specification and can cause unpredictable results.");
            }
        }
        // <2> 添加到 getMethods 和 getTypes 中
        addGetMethod(propName, winner);
    }
}
````
测试代码：
org.apache.ibatis.reflection.ReflectorTest#shouldAllowTwoBooleanGetters()可以看出，当有get和is两种方法，则选择is方法。


<1> 处，基于返回类型比较。重点在 <1.1> 和 <1.2> 的情况，因为子类可以修改放大返回值，所以在出现这个情况时，选择子类的该方法。例如，父类的一个方法的返回值为 List ，子类对该方法的返回值可以覆写为 ArrayList 。代码如下：

````
public class A {
    List<String> getXXXX();
}
public class B extends B {
    ArrayList<String> getXXXX(); // 选择它
}
````
<2> 处，调用 #addGetMethod(String name, Method method) 方法，添加方法到 getMethods 和 getTypes 中。
````
private void addGetMethod(String name, Method method) {
    // <2.1> 判断是合理的属性名
    if (isValidPropertyName(name)) {
        // <2.2> 添加到 getMethods 中
        getMethods.put(name, new MethodInvoker(method));
        // <2.3> 添加到 getTypes 中
        Type returnType = TypeParameterResolver.resolveReturnType(method, type);
        getTypes.put(name, typeToClass(returnType));
    }
}
````

typeToClass(Type src) 方法，获得 java.lang.reflect.Type 真正对应的类

````
private Class<?> typeToClass(Type src) {
    Class<?> result = null;
    // 普通类型，直接使用类
    if (src instanceof Class) {
        result = (Class<?>) src;
    // 泛型类型，使用泛型
    } else if (src instanceof ParameterizedType) {
        result = (Class<?>) ((ParameterizedType) src).getRawType();
    // 泛型数组，获得具体类
    } else if (src instanceof GenericArrayType) {
        Type componentType = ((GenericArrayType) src).getGenericComponentType();
        if (componentType instanceof Class) { // 普通类型
            result = Array.newInstance((Class<?>) componentType, 0).getClass();
        } else {
            Class<?> componentClass = typeToClass(componentType); // 递归该方法，返回类
            result = Array.newInstance(componentClass, 0).getClass();
        }
    }
    // 都不符合，使用 Object 类
    if (result == null) {
        result = Object.class;
    }
    return result;
}
````

### 3.1.3 addSetMethods ###
addSetMethods(Class<?> cls) 方法，初始化 setMethods 和 setTypes ，通过遍历 setting 方法。

````
private void addSetMethods(Class<?> cls) {
    // 属性与其 setting 方法的映射。
    Map<String, List<Method>> conflictingSetters = new HashMap<>();
    // 获得所有方法
    Method[] methods = getClassMethods(cls);
    // 遍历所有方法
    for (Method method : methods) {
        String name = method.getName();
        // <1> 方法名为 set 开头
        // 参数数量为 1
        if (name.startsWith("set") && name.length() > 3) {
            if (method.getParameterTypes().length == 1) {
                // 获得属性
                name = PropertyNamer.methodToProperty(name);
                // 添加到 conflictingSetters 中
                addMethodConflict(conflictingSetters, name, method);
            }
        }
    }
    // <2> 解决 setting 冲突方法
    resolveSetterConflicts(conflictingSetters);
}
````

总体逻辑和 #addGetMethods(Class<?> cls) 方法差不多。主要差异点在 <1> 和 <2> 处。因为 <1> 一眼就能明白，所以我们只看 <2> ，调用 #resolveSetterConflicts(Map<String, List<Method>> conflictingSetters) 方法，解决 setting 冲突方法。

#### 3.1.3.1 resolveSetterConflicts ####
resolveSetterConflicts(Map<String, List<Method>> conflictingSetters) 方法，解决 setting 冲突方法
````
private void resolveSetterConflicts(Map<String, List<Method>> conflictingSetters) {
    // 遍历每个属性，查找其最匹配的方法。因为子类可以覆写父类的方法，所以一个属性，可能对应多个 setting 方法
    for (String propName : conflictingSetters.keySet()) {
        List<Method> setters = conflictingSetters.get(propName);
        Class<?> getterType = getTypes.get(propName);
        Method match = null;
        ReflectionException exception = null;
        // <1> 解决冲突 setting 方法的方式，实际和 getting 方法的方式是不太一样的。首先，多的就是考虑了对应的 getterType 为优先级最高。其次，#pickBetterSetter(Method setter1, Method setter2, String property) 方法，选择一个更加匹配的，和 getting 方法是相同的，因为要选择精准的方法。
        for (Method setter : setters) {
            Class<?> paramType = setter.getParameterTypes()[0];
            // 和 getterType 相同，直接使用
            if (paramType.equals(getterType)) {
                // should be the best match
                match = setter;
                break;
            }
            if (exception == null) {
                try {
                    // 选择一个更加匹配的
                    match = pickBetterSetter(match, setter, propName);
                } catch (ReflectionException e) {
                    // there could still be the 'best match'
                    match = null;
                    exception = e;
                }
            }
        }
        // <2> 添加到 setMethods 和 setTypes 中
        if (match == null) {
            throw exception;
        } else {
            addSetMethod(propName, match);
        }
    }
}
````

### 3.1.4 addFields ###
addFields(Class<?> clazz) 方法，初始化 getMethods + getTypes 和 setMethods + setTypes ，通过遍历 fields 属性。实际上，它是 #addGetMethods(...) 和 #addSetMethods(...) 方法的补充，因为有些 field ，不存在对应的 setting 或 getting 方法，所以直接使用对应的 field ，而不是方法。
````
private void addFields(Class<?> clazz) {
    // 获得所有 field 们
    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
        // 设置 field 可访问
        if (canControlMemberAccessible()) {
            try {
                field.setAccessible(true);
            } catch (Exception e) {
                // Ignored. This is only a final precaution, nothing we can do.
            }
        }
        if (field.isAccessible()) {
            // <1> 若 setMethods 不存在，则调用 #addSetField(Field field) 方法，添加到 setMethods 和 setTypes 中。
            if (!setMethods.containsKey(field.getName())) {
                // issue #379 - removed the check for final because JDK 1.5 allows
                // modification of final fields through reflection (JSR-133). (JGB)
                // pr #16 - final static can only be set by the classloader
                int modifiers = field.getModifiers();
                if (!(Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers))) {
                    addSetField(field);
                }
            }
            // 添加到 getMethods 和 getTypes 中
            if (!getMethods.containsKey(field.getName())) {
                addGetField(field);
            }
        }
    }
    // 递归，处理父类
    if (clazz.getSuperclass() != null) {
        addFields(clazz.getSuperclass());
    }
}
````
### 3.1.5 其它方法 ###
Reflector 中，还有其它方法，用于对它的属性进行访问。


## 3.2 ReflectorFactory ##
org.apache.ibatis.reflection.ReflectorFactory ，Reflector 工厂接口，用于创建和缓存 Reflector 对象。

### 3.2.1 DefaultReflectorFactory ###
org.apache.ibatis.reflection.DefaultReflectorFactory ，实现 ReflectorFactory 接口，默认的 ReflectorFactory 实现类。

## 3.3 Invoker ##
org.apache.ibatis.reflection.invoker.Invoker ，调用者接口。

````
public interface Invoker {

    /**
     * 执行调用
     * @param target 目标
     * @param args 参数
     * @return 结果
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException;

    /**
     * @return 类
     */
    Class<?> getType();

}
````
- 核心是 invoke(Object target, Object[] args) 方法，执行一次调用。而具体调用什么方法，由子类来实现。

### 3.3.1 GetFieldInvoker ###
org.apache.ibatis.reflection.invoker.GetFieldInvoker ，实现 Invoker 接口，获得 Field 调用者。

### 3.3.2 SetFieldInvoker ###
org.apache.ibatis.reflection.invoker.SetFieldInvoker ，实现 Invoker 接口，设置 Field 调用者。

### 3.3.3 MethodInvoker ###
org.apache.ibatis.reflection.invoker.MethodInvoker ，实现 Invoker 接口，指定方法的调用器。

````
public class MethodInvoker implements Invoker {

    /**
     * 类型
     */
    private final Class<?> type;
    /**
     * 指定方法
     */
    private final Method method;
    public MethodInvoker(Method method) {
        this.method = method;

        // 参数大小为 1 时，一般是 setting 方法，设置 type 为方法参数[0]
        if (method.getParameterTypes().length == 1) {
            type = method.getParameterTypes()[0];
        // 否则，一般是 getting 方法，设置 type 为返回类型
        } else {
            type = method.getReturnType();
        }
    }
    // 执行指定方法
    @Override
    public Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException {
        return method.invoke(target, args);
    }
    @Override
    public Class<?> getType() {
        return type;
    }
}
````

## 3.4 ObjectFactory ##
org.apache.ibatis.reflection.factory.ObjectFactory ，Object 工厂接口，用于创建指定类的对象。

````
public interface ObjectFactory {

  /**
   * 设置 Properties
   */
  void setProperties(Properties properties);

  /**
   * 创建指定类的对象，使用默认构造方法
   */
  <T> T create(Class<T> type);

  /**
   * 创建指定类的对象，使用特定的构造方法
   */
  <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);
  
  /**
   * 判断指定类是否为集合类
   */
  <T> boolean isCollection(Class<T> type);
}
````
### 3.4.1 DefaultObjectFactory ###
org.apache.ibatis.reflection.factory.DefaultObjectFactory ，实现 ObjectFactory、Serializable 接口，默认 ObjectFactory 实现类。

#### 3.4.1.1 create ####
create(Class<T> type, ...) 方法，创建指定类的对象。

主要的函数为：

````
public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
    // <1> 获得需要创建的类
    Class<?> classToCreate = resolveInterface(type);
    // <2> 创建指定类的对象
    return (T) instantiateClass(classToCreate, constructorArgTypes, constructorArgs);
}
````

调用 #instantiateClass(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) 方法，创建指定类的对象。代码如下：

````
private <T> T instantiateClass(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
    try {
        Constructor<T> constructor;
        // <x1> 通过无参构造方法，创建指定类的对象
        if (constructorArgTypes == null || constructorArgs == null) {
            constructor = type.getDeclaredConstructor();
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            return constructor.newInstance();
        }
        // <x2> 使用特定构造方法，创建指定类的对象
        constructor = type.getDeclaredConstructor(constructorArgTypes.toArray(new Class[constructorArgTypes.size()]));
        if (!constructor.isAccessible()) {
            constructor.setAccessible(true);
        }
        return constructor.newInstance(constructorArgs.toArray(new Object[constructorArgs.size()]));
    } catch (Exception e) {
        // 拼接 argTypes
        StringBuilder argTypes = new StringBuilder();
        if (constructorArgTypes != null && !constructorArgTypes.isEmpty()) {
            for (Class<?> argType : constructorArgTypes) {
                argTypes.append(argType.getSimpleName());
                argTypes.append(",");
            }
            argTypes.deleteCharAt(argTypes.length() - 1); // remove trailing ,
        }
        // 拼接 argValues
        StringBuilder argValues = new StringBuilder();
        if (constructorArgs != null && !constructorArgs.isEmpty()) {
            for (Object argValue : constructorArgs) {
                argValues.append(String.valueOf(argValue));
                argValues.append(",");
            }
            argValues.deleteCharAt(argValues.length() - 1); // remove trailing ,
        }
        // 抛出 ReflectionException 异常
        throw new ReflectionException("Error instantiating " + type + " with invalid types (" + argTypes + ") or values (" + argValues + "). Cause: " + e, e);
    }
}
````

主要是根据传入的构造函数创建对象，主要步骤为：
1. 如果传入的是无参构造函数，则用无参构造函数创建对象
2. 如果传入的是有参数的构造函数，则根据传入的参数创建对象。

#### 3.4.1.2 isCollection ####
isCollection(Class<T> type) 方法，判断指定类是否为集合类。

## 3.5 Property 工具类 ##
org.apache.ibatis.reflection.property 包下，提供了 PropertyCopier、PropertyNamer、PropertyTokenizer 三个属性相关的工具类。
 
### 3.5.1 PropertyCopier ###
org.apache.ibatis.reflection.property.PropertyCopier ，属性复制器。

主要功能是从原始类中，把所有信息都复制到目标类里面。类似于浅拷贝。但是两个类必须是继承关系。

主要是下面的代码：
````
    /**
     * 将 sourceBean 的属性，复制到 destinationBean 中
     * @param type 指定类
     * @param sourceBean 来源 Bean 对象
     * @param destinationBean 目标 Bean 对象
     */
    public static void copyBeanProperties(Class<?> type, Object sourceBean, Object destinationBean) {
        // 循环，从当前类开始，不断复制到父类，直到父类不存在
        Class<?> parent = type;
        while (parent != null) {
            // 获得当前 parent 类定义的属性
            final Field[] fields = parent.getDeclaredFields();
            for (Field field : fields) {
                try {
                    // 设置属性可访问
                    field.setAccessible(true);
                    // 从 sourceBean 中，复制到 destinationBean 去
                    field.set(destinationBean, field.get(sourceBean));
                } catch (Exception e) {
                    // Nothing useful to do, will only fail on final fields, which will be ignored.
                }
            }
            // 获得父类
            parent = parent.getSuperclass();
        }
    }
````

### 3.5.2 PropertyNamer ###
org.apache.ibatis.reflection.property.PropertyNamer ，属性名相关的工具类方法。主要判断是否是属性设置或获取或判断的static方法。


### 3.5.3 PropertyTokenizer ###
org.apache.ibatis.reflection.property.PropertyTokenizer ，实现 Iterator 接口，属性分词器，支持迭代器的访问方式。
举个例子，在访问 "order[0].item[0].name" 时，我们希望拆分成 "order[0]"、"item[0]"、"name" 三段，那么就可以通过 PropertyTokenizer 来实现。

#### 3.5.3.1 构造方法 ####

````
/**
 * 当前字符串
 */
private String name;
/**
 * 索引的 {@link #name} ，因为 {@link #name} 如果存在 {@link #index} 会被更改
 */
private final String indexedName;
/**
 * 编号。
 *
 * 对于数组 name[0] ，则 index = 0
 * 对于 Map map[key] ，则 index = key
 */
private String index;
/**
 * 剩余字符串
 */
private final String children;

public PropertyTokenizer(String fullname) {
    // <1> 初始化 name、children 字符串，使用 . 作为分隔
    int delim = fullname.indexOf('.');
    if (delim > -1) {
        name = fullname.substring(0, delim);
        children = fullname.substring(delim + 1);
    } else {
        name = fullname;
        children = null;
    }
    // <2> 记录当前 name
    indexedName = name;
    // 若存在 [ ，则获得 index ，并修改 name 。
    delim = name.indexOf('[');
    if (delim > -1) {
        index = name.substring(delim + 1, name.length() - 1);
        name = name.substring(0, delim);
    }
}
````

#### 3.5.3.2 next ####
next() 方法，迭代获得下一个 PropertyTokenizer 对象。

#### 3.5.3.3 hasNext ####
hasNext() 方法，判断是否有下一个元素。

## 3.6 MetaClass ##
org.apache.ibatis.reflection.MetaClass ，类的元数据，基于 Reflector 和 PropertyTokenizer ，提供对指定类的各种操作。

### 3.6.1 构造方法 ###

````
private final ReflectorFactory reflectorFactory;
private final Reflector reflector;

private MetaClass(Class<?> type, ReflectorFactory reflectorFactory) {
    this.reflectorFactory = reflectorFactory;
    this.reflector = reflectorFactory.findForClass(type);
} 
````
通过构造方法，我们可以看出，一个 MetaClass 对象，对应一个 Class 对象。

### 3.6.2 findProperty ###
findProperty(String name, boolean useCamelCaseMapping) 方法，根据表达式，获得属性名。

主要是通过创建的MetaClass获取对应的正确属性名。

### 3.6.3 hasGetter ###
hasGetter(String name) 方法，判断指定属性是否有 getting 方法。

如果name的字符串为"richType.richType.richType.richType"，则会不断的创建MetaClass，直到最后一个richType对应的MetaClass对象中的Reflector对象的hasGetter方法，并且获取"richType"对应的type。

主要代码为：
````
private MetaClass metaClassForProperty(PropertyTokenizer prop) {
    // 【调用】获得 getting 方法返回的类型
    Class<?> propType = getGetterType(prop);
    // 创建 MetaClass 对象
    return MetaClass.forClass(propType, reflectorFactory);
}

private Class<?> getGetterType(PropertyTokenizer prop) {
    // 获得返回类型
    Class<?> type = reflector.getGetterType(prop.getName());
    // 如果获取数组的某个位置的元素，则获取其泛型。例如说：list[0].field ，那么就会解析 list 是什么类型，这样才好通过该类型，继续获得 field
    if (prop.getIndex() != null && Collection.class.isAssignableFrom(type)) {
        // 【调用】获得返回的类型
        Type returnType = getGenericGetterType(prop.getName());
        // 如果是泛型，进行解析真正的类型
        if (returnType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) returnType).getActualTypeArguments();
            if (actualTypeArguments != null && actualTypeArguments.length == 1) { // 为什么这里判断大小为 1 呢，因为 Collection 是 Collection<T> ，至多一个。
                returnType = actualTypeArguments[0];
                if (returnType instanceof Class) {
                    type = (Class<?>) returnType;
                } else if (returnType instanceof ParameterizedType) {
                    type = (Class<?>) ((ParameterizedType) returnType).getRawType();
                }
            }
        }
    }
    return type;
}

private Type getGenericGetterType(String propertyName) {
    try {
        // 获得 Invoker 对象
        Invoker invoker = reflector.getGetInvoker(propertyName);
        // 如果 MethodInvoker 对象，则说明是 getting 方法，解析方法返回类型
        if (invoker instanceof MethodInvoker) {
            Field _method = MethodInvoker.class.getDeclaredField("method");
            _method.setAccessible(true);
            Method method = (Method) _method.get(invoker);
            return TypeParameterResolver.resolveReturnType(method, reflector.getType());
        // 如果 GetFieldInvoker 对象，则说明是 field ，直接访问
        } else if (invoker instanceof GetFieldInvoker) {
            Field _field = GetFieldInvoker.class.getDeclaredField("field");
            _field.setAccessible(true);
            Field field = (Field) _field.get(invoker);
            return TypeParameterResolver.resolveFieldType(field, reflector.getType());
        }
    } catch (NoSuchFieldException | IllegalAccessException ignored) {
    }
    return null;
}
````

### 3.6.4 getGetterType ###
getGetterType(String name) 方法，获得指定属性的 getting 方法的返回值的类型。

````
public Class<?> getGetterType(String name) {
    // 创建 PropertyTokenizer 对象，对 name 进行分词
    PropertyTokenizer prop = new PropertyTokenizer(name);
    // 有子表达式
    if (prop.hasNext()) {
        // 创建 MetaClass 对象
        MetaClass metaProp = metaClassForProperty(prop);
        // 递归判断子表达式 children ，获得返回值的类型
        return metaProp.getGetterType(prop.getChildren());
    }
    // 直接获得返回值的类型
    return getGetterType(prop);
}
````

例如：返回的field类型为String，则返回的类型为String.class

## 3.7 ObjectWrapper ##
org.apache.ibatis.reflection.wrapper.ObjectWrapper ，对象包装器接口，基于 MetaClass 工具类，定义对指定对象的各种操作。或者可以说，ObjectWrapper 是 MetaClass 的指定类的具象化。

主要是对 MetaObject 方法的调用

### 3.7.1 BaseWrapper ###
org.apache.ibatis.reflection.wrapper.BaseWrapper ，实现 ObjectWrapper 接口，ObjectWrapper 抽象类，为子类 BeanWrapper 和 MapWrapper 提供属性值的获取和设置的公用方法。

主要的函数为：

````
public abstract class BaseWrapper implements ObjectWrapper {

    protected static final Object[] NO_ARGUMENTS = new Object[0];

    /**
     * MetaObject 对象
     */
    protected final MetaObject metaObject;

    protected BaseWrapper(MetaObject metaObject) {
        this.metaObject = metaObject;
    }

    /**
     * 获得指定属性的值
     * @param prop PropertyTokenizer 对象
     * @param object 指定 Object 对象
     * @return 值
     */
    protected Object resolveCollection(PropertyTokenizer prop, Object object) {
        if ("".equals(prop.getName())) {
            return object;
        } else {
            return metaObject.getValue(prop.getName());
        }
    }

    /**
     * 获得集合中指定位置的值
     * @param prop PropertyTokenizer 对象
     * @param collection 集合
     * @return 值
     */
    protected Object getCollectionValue(PropertyTokenizer prop, Object collection) {
        if (collection instanceof Map) {
            return ((Map) collection).get(prop.getIndex());
        } else {
            int i = Integer.parseInt(prop.getIndex());
            if (collection instanceof List) {
                return ((List) collection).get(i);
            } else if (collection instanceof Object[]) {
                return ((Object[]) collection)[i];
            } else if (collection instanceof char[]) {
                return ((char[]) collection)[i];
            } else if (collection instanceof boolean[]) {
                return ((boolean[]) collection)[i];
            } else if (collection instanceof byte[]) {
                return ((byte[]) collection)[i];
            } else if (collection instanceof double[]) {
                return ((double[]) collection)[i];
            } else if (collection instanceof float[]) {
                return ((float[]) collection)[i];
            } else if (collection instanceof int[]) {
                return ((int[]) collection)[i];
            } else if (collection instanceof long[]) {
                return ((long[]) collection)[i];
            } else if (collection instanceof short[]) {
                return ((short[]) collection)[i];
            } else {
                throw new ReflectionException("The '" + prop.getName() + "' property of " + collection + " is not a List or Array.");
            }
        }
    }

    /**
     * 设置集合中指定位置的值
     * @param prop PropertyTokenizer 对象
     * @param collection 集合
     * @param value 值
     */
    protected void setCollectionValue(PropertyTokenizer prop, Object collection, Object value) {
        if (collection instanceof Map) {
            ((Map) collection).put(prop.getIndex(), value);
        } else {
            int i = Integer.parseInt(prop.getIndex());
            if (collection instanceof List) {
                ((List) collection).set(i, value);
            } else if (collection instanceof Object[]) {
                ((Object[]) collection)[i] = value;
            } else if (collection instanceof char[]) {
                ((char[]) collection)[i] = (Character) value;
            } else if (collection instanceof boolean[]) {
                ((boolean[]) collection)[i] = (Boolean) value;
            } else if (collection instanceof byte[]) {
                ((byte[]) collection)[i] = (Byte) value;
            } else if (collection instanceof double[]) {
                ((double[]) collection)[i] = (Double) value;
            } else if (collection instanceof float[]) {
                ((float[]) collection)[i] = (Float) value;
            } else if (collection instanceof int[]) {
                ((int[]) collection)[i] = (Integer) value;
            } else if (collection instanceof long[]) {
                ((long[]) collection)[i] = (Long) value;
            } else if (collection instanceof short[]) {
                ((short[]) collection)[i] = (Short) value;
            } else {
                throw new ReflectionException("The '" + prop.getName() + "' property of " + collection + " is not a List or Array.");
            }
        }
    }
}
````

#### 3.7.1.1 BeanWrapper ####
org.apache.ibatis.reflection.wrapper.BeanWrapper ，继承 BaseWrapper 抽象类，普通对象的 ObjectWrapper 实现类，例如 User、Order 这样的 POJO 类。

````
/**
 * 普通对象
 */
private final Object object;
private final MetaClass metaClass;

public BeanWrapper(MetaObject metaObject, Object object) {
    super(metaObject);
    this.object = object;
    // 创建 MetaClass 对象
    this.metaClass = MetaClass.forClass(object.getClass(), metaObject.getReflectorFactory());
}
````

##### 3.7.1.1.1 get #####
get(PropertyTokenizer prop) 方法，获得指定属性的值。

例如：
````
 meta.getValue("richList[0]")
````

````
@Override
public Object get(PropertyTokenizer prop) {
    // <1> 获得集合类型的属性的指定位置的值。例如说：User 对象的 list[0] 。所调用的方法，都是 BaseWrapper 所提供的公用方法。
    if (prop.getIndex() != null) {
        // 获得集合类型的属性
        Object collection = resolveCollection(prop, object);
        // 获得指定位置的值
        return getCollectionValue(prop, collection);
    // <2> 调用 #getBeanProperty(PropertyTokenizer prop, Object object) 方法，获得属性的值。
    } else {
        return getBeanProperty(prop, object);
    }
}
````

通过调用 Invoker 方法，获得属性的值。
````
private Object getBeanProperty(PropertyTokenizer prop, Object object) {
    try {
        Invoker method = metaClass.getGetInvoker(prop.getName());
        try {
            return method.invoke(object, NO_ARGUMENTS);
        } catch (Throwable t) {
            throw ExceptionUtil.unwrapThrowable(t);
        }
    } catch (RuntimeException e) {
        throw e;
    } catch (Throwable t) {
        throw new ReflectionException("Could not get property '" + prop.getName() + "' from " + object.getClass() + ".  Cause: " + t.toString(), t);
    }
}
````

##### 3.7.1.1.2 set #####
set(PropertyTokenizer prop, Object value) 方法，设置指定属性的值。

例如：
````
meta.setValue("richList[0]", "foo");
````

````
@Override
public void set(PropertyTokenizer prop, Object value) {
    // 设置集合类型的属性的指定位置的值
    if (prop.getIndex() != null) {
        // 获得集合类型的属性
        Object collection = resolveCollection(prop, object);
        // 设置指定位置的值
        setCollectionValue(prop, collection, value);
    // 设置属性的值
    } else {
        setBeanProperty(prop, object, value);
    }
}

private void setBeanProperty(PropertyTokenizer prop, Object object, Object value) {
    try {
        Invoker method = metaClass.getSetInvoker(prop.getName());
        Object[] params = {value};
        try {
            method.invoke(object, params);
        } catch (Throwable t) {
            throw ExceptionUtil.unwrapThrowable(t);
        }
    } catch (Throwable t) {
        throw new ReflectionException("Could not set property '" + prop.getName() + "' of '" + object.getClass() + "' with value '" + value + "' Cause: " + t.toString(), t);
    }
}
````

##### 3.7.1.1.3 getGetterType #####
getGetterType(String name) 方法，获得指定属性的 getting 方法的返回值 type。

大体过程和MetaClass 的 #getGetterType(String name) 方法是一致的。
````
@Override
public Class<?> getGetterType(String name) {
    // 创建 PropertyTokenizer 对象，对 name 进行分词
    PropertyTokenizer prop = new PropertyTokenizer(name);
    // 有子表达式
    if (prop.hasNext()) {
        // <1> 创建 MetaObject 对象
        MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
        // 如果 metaValue 为空，则基于 metaClass 获得返回类型
        if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
            return metaClass.getGetterType(name);
        // 如果 metaValue 非空，则基于 metaValue 获得返回类型。
        // 例如：richType.richMap.nihao ，其中 richMap 是 Map 类型，而 nihao 的类型，需要获得到 nihao 的具体值，才能做真正的判断。
        } else {
            // 递归判断子表达式 children ，获得返回值的类型
            return metaValue.getGetterType(prop.getChildren());
        }
    // 有子表达式
    } else {
        // 直接获得返回值的类型
        return metaClass.getGetterType(name);
    }
}
````

##### 3.7.1.1.4 hasGetter #####
hasGetter(String name) 方法，是否有指定属性的 getting 方法。

例如
````
meta.hasGetter("richType")
````

````
@Override
public boolean hasGetter(String name) {
    // 创建 PropertyTokenizer 对象，对 name 进行分词
    PropertyTokenizer prop = new PropertyTokenizer(name);
    // 有子表达式
    if (prop.hasNext()) {
        // 判断是否有该属性的 getting 方法
        if (metaClass.hasGetter(prop.getIndexedName())) {
            // 创建 MetaObject 对象
            MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
            // 如果 metaValue 为空，则基于 metaClass 判断是否有该属性的 getting 方法
            if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
                return metaClass.hasGetter(name);
            // 如果 metaValue 非空，则基于 metaValue 判断是否有 getting 方法。
            } else {
                // 递归判断子表达式 children ，判断是否有 getting 方法
                return metaValue.hasGetter(prop.getChildren());
            }
        } else {
            return false;
        }
    // 有子表达式
    } else {
        // 判断是否有该属性的 getting 方法
        return metaClass.hasGetter(name);
    }
}
````

##### 3.7.1.1.5 instantiatePropertyValue #####
instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory) 方法，创建指定属性的值。

````
@Override
public MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory) {
    MetaObject metaValue;
    // 获得 setting 方法的方法参数类型
    Class<?> type = getSetterType(prop.getName());
    try {
        // 创建对象
        Object newObject = objectFactory.create(type);
        // 创建 MetaObject 对象
        metaValue = MetaObject.forObject(newObject, metaObject.getObjectFactory(), metaObject.getObjectWrapperFactory(), metaObject.getReflectorFactory());
        // <1> 设置当前对象的值
        set(prop, newObject);
    } catch (Exception e) {
        throw new ReflectionException("Cannot set value of property '" + name + "' because '" + name + "' is null and cannot be instantiated on instance of " + type.getName() + ". Cause:" + e.toString(), e);
    }
    return metaValue;
}
````

##### 3.7.1.1.6 isCollection #####
返回false


#### 3.7.1.2 MapWrapper ####
org.apache.ibatis.reflection.wrapper.MapWrapper ，继承 BaseWrapper 抽象类，Map 对象的 ObjectWrapper 实现类。

MapWrapper 和 BeanWrapper 的大体逻辑是一样的

### 3.7.2 CollectionWrapper ###
org.apache.ibatis.reflection.wrapper.CollectionWrapper ，实现 ObjectWrapper 接口，集合 ObjectWrapper 实现类。

## 3.8 ObjectWrapperFactory ##
org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory ，ObjectWrapper 工厂接口。

默认不使用ObjectWrapperFactory
### 3.8.1 DefaultObjectWrapperFactory ###
org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory ，实现 ObjectWrapperFactory 接口，默认 ObjectWrapperFactory 实现类。

## 3.9 MetaObject ##
org.apache.ibatis.reflection.MetaObject ，对象元数据，提供了对象的属性值的获得和设置等等方法。

### 3.9.1 构造方法 ###

````
/**
 * 原始 Object 对象
 */
private final Object originalObject;
/**
 * 封装过的 Object 对象
 */
private final ObjectWrapper objectWrapper;
private final ObjectFactory objectFactory;
private final ObjectWrapperFactory objectWrapperFactory;
private final ReflectorFactory reflectorFactory;

private MetaObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
    this.originalObject = object;
    this.objectFactory = objectFactory;
    this.objectWrapperFactory = objectWrapperFactory;
    this.reflectorFactory = reflectorFactory;

    // 会根据 object 类型的不同，创建对应的 ObjectWrapper 对象。
    if (object instanceof ObjectWrapper) {
        this.objectWrapper = (ObjectWrapper) object;
    } else if (objectWrapperFactory.hasWrapperFor(object)) { // <2>
        // 创建 ObjectWrapper 对象
        this.objectWrapper = objectWrapperFactory.getWrapperFor(this, object);
    } else if (object instanceof Map) {
        // 创建 MapWrapper 对象
        this.objectWrapper = new MapWrapper(this, (Map) object);
    } else if (object instanceof Collection) {
        // 创建 CollectionWrapper 对象
        this.objectWrapper = new CollectionWrapper(this, (Collection) object);
    } else {
        // 创建 BeanWrapper 对象
        this.objectWrapper = new BeanWrapper(this, object);
    }
}
````

### 3.9.2 forObject ###
forObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) 静态方法，创建 MetaObject 对象。

````
/**
 * 创建 MetaObject 对象
 * @param object 原始 Object 对象
 * @param objectFactory
 * @param objectWrapperFactory
 * @param reflectorFactory
 * @return MetaObject 对象
 */
public static MetaObject forObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
    if (object == null) {
        return SystemMetaObject.NULL_META_OBJECT;
    } else {
        return new MetaObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
    }
}
````
如果 object 为空的情况下，返回 SystemMetaObject.NULL_META_OBJECT 。

### 3.9.3 metaObjectForProperty ###
metaObjectForProperty(String name) 方法，创建指定属性的 MetaObject 对象。

````
public MetaObject metaObjectForProperty(String name) {
    // 获得属性值
    Object value = getValue(name);
    // 创建 MetaObject 对象
    return MetaObject.forObject(value, objectFactory, objectWrapperFactory, reflectorFactory);
}
````

### 3.9.4 getValue ###
getValue(String name) 方法，获得指定属性的值。

````
public Object getValue(String name) {
    // 创建 PropertyTokenizer 对象，对 name 分词
    PropertyTokenizer prop = new PropertyTokenizer(name);
    // 有子表达式
    if (prop.hasNext()) {
        // 创建 MetaObject 对象
        MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
        // <2> 递归判断子表达式 children ，获取值
        if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
            return null;
        } else {
            return metaValue.getValue(prop.getChildren());
        }
    // 无子表达式
    } else {
        // <1> 获取值
        return objectWrapper.get(prop);
    }
}
````

### 3.9.5 setValue ###
setValue(String name, Object value) 方法，设置指定属性的指定值。

````
public void setValue(String name, Object value) {
    // 创建 PropertyTokenizer 对象，对 name 分词
    PropertyTokenizer prop = new PropertyTokenizer(name);
    // 有子表达式
    if (prop.hasNext()) {
        // 创建 MetaObject 对象
        MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
        // 递归判断子表达式 children ，设置值
        if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
            if (value == null) {
                // don't instantiate child path if value is null
                return;
            } else {
                // <1> 创建值
                metaValue = objectWrapper.instantiatePropertyValue(name, prop, objectFactory);
            }
        }
        // 设置值
        metaValue.setValue(prop.getChildren(), value);
    // 无子表达式
    } else {
        // <1> 设置值
        objectWrapper.set(prop, value);
    }
}
````

### 3.9.6 isCollection ###
isCollection() 方法，判断是否为集合。

## 3.10 SystemMetaObject ##
org.apache.ibatis.reflection.SystemMetaObject ，系统级的 MetaObject 对象，主要提供了 ObjectFactory、ObjectWrapperFactory、空 MetaObject 的单例。

````
public final class SystemMetaObject {

    /**
     * ObjectFactory 的单例
     */
    public static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
    /**
     * ObjectWrapperFactory 的单例
     */
    public static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();

    /**
     * 空对象的 MetaObject 对象单例
     */
    public static final MetaObject NULL_META_OBJECT = MetaObject.forObject(NullObject.class, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());

    private SystemMetaObject() {
        // Prevent Instantiation of Static Class
    }

    private static class NullObject {
    }

    /**
     * 创建 MetaObject 对象
     *
     * @param object 指定对象
     * @return MetaObject 对象
     */
    public static MetaObject forObject(Object object) {
        return MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
    }
}
````
核心就是 #forObject(Object object) 方法，创建指定对象的 MetaObject 对象。

## 3.11 ParamNameUtil ##
org.apache.ibatis.reflection.ParamNameUtil ，参数名工具类，获得构造方法、普通方法的参数列表。

````
public class ParamNameUtil {

    /**
     * 获得普通方法的参数列表
     *
     * @param method 普通方法
     * @return 参数集合
     */
    public static List<String> getParamNames(Method method) {
        return getParameterNames(method);
    }

    /**
     * 获得构造方法的参数列表
     *
     * @param constructor 构造方法
     * @return 参数集合
     */
    public static List<String> getParamNames(Constructor<?> constructor) {
        return getParameterNames(constructor);
    }

    private static List<String> getParameterNames(Executable executable) {
        final List<String> names = new ArrayList<>();
        // 获得 Parameter 数组
        final Parameter[] params = executable.getParameters();
        // 获得参数名，并添加到 names 中
        for (Parameter param : params) {
            names.add(param.getName());
        }
        return names;
    }

    private ParamNameUtil() {
        super();
    }

}
````

## 3.12 ParamNameResolver ##
org.apache.ibatis.reflection.ParamNameResolver ，参数名解析器。

````
/*
 * 参数名映射
 *
 * KEY：参数顺序
 * VALUE：参数名
 */
private final SortedMap<Integer, String> names;
/**
 * 是否有 {@link Param} 注解的参数
 */
private boolean hasParamAnnotation;

public ParamNameResolver(Configuration config, Method method) {
    final Class<?>[] paramTypes = method.getParameterTypes();
    final Annotation[][] paramAnnotations = method.getParameterAnnotations();
    final SortedMap<Integer, String> map = new TreeMap<>();
    int paramCount = paramAnnotations.length;
    // get names from @Param annotations
    for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
        // 忽略，如果是特殊参数
        if (isSpecialParameter(paramTypes[paramIndex])) {
            // skip special parameters
            continue;
        }
        String name = null;
        // 首先，从 @Param 注解中获取参数
        for (Annotation annotation : paramAnnotations[paramIndex]) {
            if (annotation instanceof Param) {
                hasParamAnnotation = true;
                name = ((Param) annotation).value();
                break;
            }
        }
        if (name == null) {
            // @Param was not specified.
            // 其次，获取真实的参数名
            if (config.isUseActualParamName()) { // 默认开启
                name = getActualParamName(method, paramIndex);
            }
            // 最差，使用 map 的顺序，作为编号
            if (name == null) {
                // use the parameter index as the name ("0", "1", ...)
                // gcode issue #71
                name = String.valueOf(map.size());
            }
        }
        // 添加到 map 中
        map.put(paramIndex, name);
    }
    // 构建不可变集合
    names = Collections.unmodifiableSortedMap(map);
}

private String getActualParamName(Method method, int paramIndex) {
    return ParamNameUtil.getParamNames(method).get(paramIndex);
}

private static boolean isSpecialParameter(Class<?> clazz) {
    return RowBounds.class.isAssignableFrom(clazz) || ResultHandler.class.isAssignableFrom(clazz);
}
````

### 3.12.1 getNamedParams ###
getNamedParams(Object[] args) 方法，获得参数名与值的映射。

````
private static final String GENERIC_NAME_PREFIX = "param";

/**
 * 获得参数名与值的映射
 */
public Object getNamedParams(Object[] args) {
    final int paramCount = names.size();
    // 无参数，则返回 null
    if (args == null || paramCount == 0) {
        return null;
    // 只有一个非注解的参数，直接返回首元素
    } else if (!hasParamAnnotation && paramCount == 1) {
        return args[names.firstKey()];
    } else {
        // 集合。
        // 组合 1 ：KEY：参数名，VALUE：参数值
        // 组合 2 ：KEY：GENERIC_NAME_PREFIX + 参数顺序，VALUE ：参数值
        final Map<String, Object> param = new ParamMap<>();
        int i = 0;
        // 遍历 names 集合
        for (Map.Entry<Integer, String> entry : names.entrySet()) {
            // 组合 1 ：添加到 param 中
            param.put(entry.getValue(), args[entry.getKey()]);
            // add generic param names (param1, param2, ...)
            // 组合 2 ：添加到 param 中
            final String genericParamName = GENERIC_NAME_PREFIX + String.valueOf(i + 1);
            // ensure not to overwrite parameter named with @Param
            if (!names.containsValue(genericParamName)) {
                param.put(genericParamName, args[entry.getKey()]);
            }
            i++;
        }
        return param;
    }
}
````

## 3.13 TypeParameterResolver ##
org.apache.ibatis.reflection.TypeParameterResolver ，工具类，java.lang.reflect.Type 参数解析器

//  todo 

## 3.14 ArrayUtil ##
org.apache.ibatis.reflection.ArrayUtil ，数组工具类。

## 3.15 ExceptionUtil ##
org.apache.ibatis.reflection.ExceptionUtil ，异常工具类。

----

# 4 异常模块 #

## 4.1 exceptions 包 ##
### 4.1.1 IbatisException ###
org.apache.ibatis.exceptions.IbatisException ，实现 RuntimeException 类，IBatis 的异常基类。

- IbatisException 已经在 2015 年被废弃，取代它的是 PersistenceException 类。

### 4.1.2 PersistenceException ###

org.apache.ibatis.exceptions.PersistenceException ，继承 IbatisException 类，目前 MyBatis 真正的异常基类。

#### 4.1.2.1 ExceptionFactory ####

org.apache.ibatis.exceptions.ExceptionFactory ，异常工厂。

### 4.1.3 TooManyResultsException ###

org.apache.ibatis.exceptions.TooManyResultsException ，继承 PersistenceException 类，查询返回过多结果的异常。期望返回一条，实际返回了多条。

## 4.2 parsing 包 ##
### 4.2.1 ParsingException ###
org.apache.ibatis.parsing.ParsingException ，继承 PersistenceException 类，解析异常。

## 4.3 其它包 ##
- reflection 包：ReflectionException
- logging 包：LogException
- builder 包：BuilderException、IncompleteElementException
- scripting 包：ScriptingException
- binding 包：BindingException
- type 包：TypeException
- session 包：SqlSessionException
- cache 包：CacheException
- transaction 包：TransactionException
- datasource 包：DataSourceException
- executor 包：ResultMapException、ExecutorException、BatchExecutorException
- plugin 包：PluginException

----

# 5 数据源模块 #
- 数据源是实际开发中常用的组件之一。现在开源的数据源都提供了比较丰富的功能，例如，连接池功能、检测连接状态等，选择性能优秀的数据源组件对于提升 ORM 框架乃至整个应用的性能都是非常重要的。
- MyBatis 自身提供了相应的数据源实现，当然 MyBatis 也提供了与第三方数据源集成的接口，这些功能都位于数据源模块之中。

## 5.1 DataSourceFactory ##
org.apache.ibatis.datasource.DataSourceFactory ，javax.sql.DataSourceFactory 工厂接口。

### 5.1.1 UnpooledDataSourceFactory ###
org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory ，实现 DataSourceFactory 接口，非池化的 DataSourceFactory 实现类。

UNPOOLED– 这个数据源的实现只是每次被请求时打开和关闭连接。虽然有点慢，但对于在数据库连接可用性方面没有太高要求的简单应用程序来说，是一个很好的选择。 不同的数据库在性能方面的表现也是不一样的，对于某些数据库来说，使用连接池并不重要，这个配置就很适合这种情形。UNPOOLED 类型的数据源仅仅需要配置以下 5 种属性：

- driver – 这是 JDBC 驱动的 Java 类的完全限定名（并不是 JDBC 驱动中可能包含的数据源类）。
- url – 这是数据库的 JDBC URL 地址。
- username – 登录数据库的用户名。
- password – 登录数据库的密码。
- defaultTransactionIsolationLevel – 默认的连接事务隔离级别。

作为可选项，你也可以传递属性给数据库驱动。要这样做，属性的前缀为“driver.”，例如：

- driver.encoding=UTF8

这将通过 DriverManager.getConnection(url,driverProperties) 方法传递值为 UTF8 的 encoding 属性给数据库驱动。

#### 5.1.1.1 构造方法 ####

````
/**
 * DataSource 对象
 */
protected DataSource dataSource;

public UnpooledDataSourceFactory() {
    // 创建 UnpooledDataSource 对象
    this.dataSource = new UnpooledDataSource();
}
````

创建默认的 UnpooledDataSource 对象。

#### 5.1.1.2 getDataSource ####
getDataSource() 方法，返回 DataSource 对象。

#### 5.1.1.3 setProperties ####
setProperties(Properties properties) 方法，将 properties 的属性，初始化到 dataSource 中。

````
@Override
public void setProperties(Properties properties) {
    Properties driverProperties = new Properties();
    // 创建 dataSource 对应的 MetaObject 对象
    MetaObject metaDataSource = SystemMetaObject.forObject(dataSource);
    // 遍历 properties 属性，初始化到 driverProperties 和 MetaObject 中
    for (Object key : properties.keySet()) {
        String propertyName = (String) key;
        // 初始化到 driverProperties 中
        if (propertyName.startsWith(DRIVER_PROPERTY_PREFIX)) { // 以 "driver." 开头的配置
            String value = properties.getProperty(propertyName);
            driverProperties.setProperty(propertyName.substring(DRIVER_PROPERTY_PREFIX_LENGTH), value);
        // 初始化到 MetaObject 中
        } else if (metaDataSource.hasSetter(propertyName)) {
            String value = (String) properties.get(propertyName);
			// 调用 #convertValue(MetaObject metaDataSource, String propertyName, String value) 方法，将字符串转化成对应属性的类型。
            Object convertedValue = convertValue(metaDataSource, propertyName, value);
            metaDataSource.setValue(propertyName, convertedValue);
        } else {
            throw new DataSourceException("Unknown DataSource property: " + propertyName);
        }
    }
    // 设置 driverProperties 到 MetaObject 中
    if (driverProperties.size() > 0) {
        metaDataSource.setValue("driverProperties", driverProperties);
    }
}

private Object convertValue(MetaObject metaDataSource, String propertyName, String value) {
    Object convertedValue = value;
    // 获得该属性的 setting 方法的参数类型
    Class<?> targetType = metaDataSource.getSetterType(propertyName);
    // 转化
    if (targetType == Integer.class || targetType == int.class) {
        convertedValue = Integer.valueOf(value);
    } else if (targetType == Long.class || targetType == long.class) {
        convertedValue = Long.valueOf(value);
    } else if (targetType == Boolean.class || targetType == boolean.class) {
        convertedValue = Boolean.valueOf(value);
    }
    // 返回
    return convertedValue;
}
````

### 5.1.2 PooledDataSourceFactory ###
org.apache.ibatis.datasource.pooled.PooledDataSourceFactory ，继承 UnpooledDataSourceFactory 类，池化的 DataSourceFactory 实现类。

POOLED– 这种数据源的实现利用“池”的概念将 JDBC 连接对象组织起来，避免了创建新的连接实例时所必需的初始化和认证时间。 这是一种使得并发 Web 应用快速响应请求的流行处理方式。

除了上述提到 UNPOOLED 下的属性外，还有更多属性用来配置 POOLED 的数据源：

- poolMaximumActiveConnections – 在任意时间可以存在的活动（也就是正在使用）连接数量，默认值：10
- poolMaximumIdleConnections – 任意时间可能存在的空闲连接数。
- poolMaximumCheckoutTime – 在被强制返回之前，池中连接被检出（checked out）时间，默认值：20000 毫秒（即 20 秒）
- poolTimeToWait – 这是一个底层设置，如果获取连接花费了相当长的时间，连接池会打印状态日志并重新尝试获取一个连接（避免在误配置的情况下一直安静的失败），默认值：20000 毫秒（即 20 秒）。
- poolMaximumLocalBadConnectionTolerance – 这是一个关于坏连接容忍度的底层设置， 作用于每一个尝试从缓存池获取连接的线程. 如果这个线程获取到的是一个坏的连接，那么这个数据源允许这个线程尝试重新获取一个新的连接，但是这个重新尝试的次数不应该超过 poolMaximumIdleConnections 与 poolMaximumLocalBadConnectionTolerance 之和。 默认值：3 (新增于 3.4.5)
- poolPingQuery – 发送到数据库的侦测查询，用来检验连接是否正常工作并准备接受请求。默认是“NO PING QUERY SET”，这会导致多数数据库驱动失败时带有一个恰当的错误消息。
- poolPingEnabled – 是否启用侦测查询。若开启，需要设置 poolPingQuery 属性为一个可执行的 SQL 语句（最好是一个速度非常快的 SQL 语句），默认值：false。
- poolPingConnectionsNotUsedFor – 配置 poolPingQuery 的频率。可以被设置为和数据库连接超时时间一样，来避免不必要的侦测，默认值：0（即所有连接每一时刻都被侦测 — 当然仅当 poolPingEnabled 为 true 时适用）。

PooledDataSource 比 UnpooledDataSource 的配置项多很多。

真正的池化逻辑，在 PooledDataSource 对象中。

### 5.1.3 JndiDataSourceFactory ###

org.apache.ibatis.datasource.jndi.JndiDataSourceFactory ，实现 DataSourceFactory 接口，基于 JNDI 的 DataSourceFactory 实现类。

JNDI – 这个数据源的实现是为了能在如 EJB 或应用服务器这类容器中使用，容器可以集中或在外部配置数据源，然后放置一个 JNDI 上下文的引用。这种数据源配置只需要两个属性：

- initial_context – 这个属性用来在 InitialContext 中寻找上下文（即，initialContext.lookup(initial_context)）。这是个可选属性，如果忽略，那么 data_source 属性将会直接从 InitialContext 中寻找。
- data_source – 这是引用数据源实例位置的上下文的路径。提供了 initial_context 配置时会在其返回的上下文中进行查找，没有提供时则直接在 InitialContext 中查找。
和其他数据源配置类似，可以通过添加前缀“env.”直接把属性传递给初始上下文。比如：

env.encoding=UTF8
这就会在初始上下文（InitialContext）实例化时往它的构造方法传递值为 UTF8 的 encoding 属性。

#### 5.1.3.1 构造方法 ####
不同于 UnpooledDataSourceFactory 和 PooledDataSourceFactory ，dataSource 不在构造方法中创建，而是在 #setProperties(Properties properties) 中。

#### 5.1.3.2 getDataSource ####
getDataSource() 方法，返回 DataSource 对象。

#### 5.1.3.3 setProperties ####

setProperties(Properties properties) 方法，从上下文中，获得 DataSource 对象。

````
public static final String INITIAL_CONTEXT = "initial_context";
public static final String DATA_SOURCE = "data_source";
public static final String ENV_PREFIX = "env.";

@Override
public void setProperties(Properties properties) {
    try {
        InitialContext initCtx;
        // <1> 获得系统 Properties 对象
        Properties env = getEnvProperties(properties);
        // 创建 InitialContext 对象
        if (env == null) {
            initCtx = new InitialContext();
        } else {
            initCtx = new InitialContext(env);
        }

        // 从 InitialContext 上下文中，获取 DataSource 对象
        if (properties.containsKey(INITIAL_CONTEXT)
                && properties.containsKey(DATA_SOURCE)) {
            Context ctx = (Context) initCtx.lookup(properties.getProperty(INITIAL_CONTEXT));
            dataSource = (DataSource) ctx.lookup(properties.getProperty(DATA_SOURCE));
        } else if (properties.containsKey(DATA_SOURCE)) {
            dataSource = (DataSource) initCtx.lookup(properties.getProperty(DATA_SOURCE));
        }
    } catch (NamingException e) {
        throw new DataSourceException("There was an error configuring JndiDataSourceTransactionPool. Cause: " + e, e);
    }
}
````

## 5.2 DataSource ##
javax.sql.DataSource ，在其上可以衍生出数据连接池、分库分表、读写分离等等功能。

### 5.2.1 UnpooledDataSource ###
org.apache.ibatis.datasource.unpooled.UnpooledDataSource ，实现 DataSource 接口，非池化的 DataSource 对象。

#### 5.2.1.1 构造方法 ####

主要对属性赋值。

主要有这些属性：
````
/** Driver 类加载器 */
private ClassLoader driverClassLoader;
/** Driver 属性 */
private Properties driverProperties;
/** Driver 类名 */
private String driver;
/** 数据库 URL */
private String url;
/** 数据库用户名 */
private String username;
/** 数据库密码 */
private String password;
/** 是否自动提交事务 */
private Boolean autoCommit;
/** 默认事务隔离级别 */
private Integer defaultTransactionIsolationLevel;
````

#### 5.2.1.2 getConnection ####
getConnection(...) 方法，获得 Connection 连接。

````
@Override
public Connection getConnection() throws SQLException {
    return doGetConnection(username, password);
}

@Override
public Connection getConnection(String username, String password) throws SQLException {
    return doGetConnection(username, password);
}
````
都是调用 #doGetConnection(String username, String password) 方法，获取 Connection 连接

````
private Connection doGetConnection(String username, String password) throws SQLException {
    // 创建 Properties 对象
    Properties props = new Properties();
    // 设置 driverProperties 到 props 中
    if (driverProperties != null) {
        props.putAll(driverProperties);
    }
    // 设置 user 和 password 到 props 中
    if (username != null) {
        props.setProperty("user", username);
    }
    if (password != null) {
        props.setProperty("password", password);
    }
    // 执行获得 Connection 连接
    return doGetConnection(props);
}

private Connection doGetConnection(Properties properties) throws SQLException {
    // 调用 #initializeDriver() 方法，初始化 Driver 。
    initializeDriver();
    // 调用 java.sql.DriverManager#getConnection(String url, Properties info) 方法，获得 Connection 对象。
    Connection connection = DriverManager.getConnection(url, properties);
    // 调用 #configureConnection(Connection conn) 方法，配置 Connection 对象。
    configureConnection(connection);
    return connection;
}
````

##### 5.2.1.2.1 initializeDriver #####
initializeDriver() 方法，初始化 Driver 。

````
private synchronized void initializeDriver() throws SQLException { // synchronized 锁的粒度太大，可以减小到基于 registeredDrivers 来同步，并且很多时候，不需要加锁。
    // 判断 registeredDrivers 是否已经存在该 driver ，若不存在，进行初始化
    if (!registeredDrivers.containsKey(driver)) {
        Class<?> driverType;
        try {
            // 获得 driver 类，实际上，就是我们常见的 "Class.forName("com.mysql.jdbc.Driver")" 。
            if (driverClassLoader != null) {
                driverType = Class.forName(driver, true, driverClassLoader);
            } else {
                driverType = Resources.classForName(driver);
            }
            // 创建 Driver 对象，并注册到 DriverManager 中，以及添加到 registeredDrivers 中。
            Driver driverInstance = (Driver) driverType.newInstance();
            // 创建 DriverProxy 对象，并注册到 DriverManager 中
            DriverManager.registerDriver(new DriverProxy(driverInstance));
            // 添加到 registeredDrivers 中
            registeredDrivers.put(driver, driverInstance);
        } catch (Exception e) {
            throw new SQLException("Error setting driver on UnpooledDataSource. Cause: " + e);
        }
    }
}
````

##### 5.2.1.2.2 configureConnection #####
configureConnection(Connection conn) 方法，配置 Connection 对象。

````
private void configureConnection(Connection conn) throws SQLException {
    // 设置自动提交
    if (autoCommit != null && autoCommit != conn.getAutoCommit()) {
        conn.setAutoCommit(autoCommit);
    }
    // 设置事务隔离级别
    if (defaultTransactionIsolationLevel != null) {
        conn.setTransactionIsolation(defaultTransactionIsolationLevel);
    }
}
````

### 5.2.2 PooledDataSource ###
org.apache.ibatis.datasource.pooled.PooledDataSource ，实现 DataSource 接口，池化的 DataSource 实现类。

- 实际场景下，我们基本不用 MyBatis 自带的数据库连接池的实现。

#### 5.2.2.1 构造方法 ####
````
/**
 * PoolState 对象，记录池化的状态
 */
private final PoolState state = new PoolState(this);

/**
 * UnpooledDataSource 对象
 */
private final UnpooledDataSource dataSource;

// OPTIONAL CONFIGURATION FIELDS
/**
 * 在任意时间可以存在的活动（也就是正在使用）连接数量
 */
protected int poolMaximumActiveConnections = 10;
/**
 * 任意时间可能存在的空闲连接数
 */
protected int poolMaximumIdleConnections = 5;
/**
 * 在被强制返回之前，池中连接被检出（checked out）时间。单位：毫秒
 */
protected int poolMaximumCheckoutTime = 20000;
/**
 * 这是一个底层设置，如果获取连接花费了相当长的时间，连接池会打印状态日志并重新尝试获取一个连接（避免在误配置的情况下一直安静的失败）。单位：毫秒
 */
protected int poolTimeToWait = 20000;
/**
 * 这是一个关于坏连接容忍度的底层设置，作用于每一个尝试从缓存池获取连接的线程. 如果这个线程获取到的是一个坏的连接，那么这个数据源允许这个线程尝试重新获取一个新的连接，但是这个重新尝试的次数不应该超过 poolMaximumIdleConnections 与 poolMaximumLocalBadConnectionTolerance 之和。
 */
protected int poolMaximumLocalBadConnectionTolerance = 3;
/**
 * 发送到数据库的侦测查询，用来检验连接是否正常工作并准备接受请求。
 */
protected String poolPingQuery = "NO PING QUERY SET";
/**
 * 是否启用侦测查询。若开启，需要设置 poolPingQuery 属性为一个可执行的 SQL 语句（最好是一个速度非常快的 SQL 语句）
 */
protected boolean poolPingEnabled;
/**
 * 配置 poolPingQuery 的频率。可以被设置为和数据库连接超时时间一样，来避免不必要的侦测，默认值：0（即所有连接每一时刻都被侦测 — 当然仅当 poolPingEnabled 为 true 时适用）
 */
protected int poolPingConnectionsNotUsedFor;

/**
 * 期望 Connection 的类型编码，通过 {@link #assembleConnectionTypeCode(String, String, String)} 计算。
 */
private int expectedConnectionTypeCode;

public PooledDataSource() {
    dataSource = new UnpooledDataSource();
}

public PooledDataSource(UnpooledDataSource dataSource) {
    this.dataSource = dataSource;
}

public PooledDataSource(String driver, String url, String username, String password) {
    dataSource = new UnpooledDataSource(driver, url, username, password);
    expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
}

public PooledDataSource(String driver, String url, Properties driverProperties) {
    dataSource = new UnpooledDataSource(driver, url, driverProperties);
    expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
}

public PooledDataSource(ClassLoader driverClassLoader, String driver, String url, String username, String password) {
    dataSource = new UnpooledDataSource(driverClassLoader, driver, url, username, password);
    expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
}

public PooledDataSource(ClassLoader driverClassLoader, String driver, String url, Properties driverProperties) {
    // 创建 UnpooledDataSource 对象
    dataSource = new UnpooledDataSource(driverClassLoader, driver, url, driverProperties);
    // 计算  expectedConnectionTypeCode 的值
    expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
}
````

#### 5.2.2.2 getConnection ####

getConnection(...) 方法，获得 Connection 连接。

````

/**
 * 调用 #popConnection(String username, String password) 方法，获取 org.apache.ibatis.datasource.pooled.PooledConnection 对象，这是一个池化的连接。
 */
@Override
public Connection getConnection() throws SQLException {
    return popConnection(dataSource.getUsername(), dataSource.getPassword()).getProxyConnection();
}

/**
 * 调用 PooledConnection#getProxyConnection() 方法，返回代理的 Connection 对象。这样，每次对数据库的操作，才能被 PooledConnection 的  invoke 代理拦截。
 */
@Override
public Connection getConnection(String username, String password) throws SQLException {
    return popConnection(username, password).getProxyConnection();
}
````

##### 5.2.2.2.1 popConnection #####
popConnection(String username, String password) 方法，获取 PooledConnection 对象。

整体流程如下：

![](/picture/mybatis-pool-popConnection.png)


具体代码为：
````
private PooledConnection popConnection(String username, String password) throws SQLException {
    boolean countedWait = false; // 标记，获取连接时，是否进行了等待
    PooledConnection conn = null; // 最终获取到的链接对象
    long t = System.currentTimeMillis(); // 记录当前时间
    int localBadConnectionCount = 0; // 记录当前方法，获取到坏连接的次数

    // 循环，获取可用的 Connection 连接
    while (conn == null) {
        synchronized (state) {
            // 空闲连接非空
            if (!state.idleConnections.isEmpty()) {
                // Pool has available connection
                // 通过移除的方式，获得首个空闲的连接
                conn = state.idleConnections.remove(0);
                if (log.isDebugEnabled()) {
                    log.debug("Checked out connection " + conn.getRealHashCode() + " from pool.");
                }
            // 无空闲空闲连接
            } else {
                // Pool does not have available connection
                // 激活的连接数小于 poolMaximumActiveConnections
                if (state.activeConnections.size() < poolMaximumActiveConnections) {
                    // Can create new connection
                    // 创建新的 PooledConnection 连接对象
                    conn = new PooledConnection(dataSource.getConnection(), this);
                    if (log.isDebugEnabled()) {
                        log.debug("Created connection " + conn.getRealHashCode() + ".");
                    }
                } else {
                    // Cannot create new connection
                    // 获得首个激活的 PooledConnection 对象
                    PooledConnection oldestActiveConnection = state.activeConnections.get(0);
                    // 检查该连接是否超时
                    long longestCheckoutTime = oldestActiveConnection.getCheckoutTime();
                    if (longestCheckoutTime > poolMaximumCheckoutTime) { // 检查到超时
                        // Can claim overdue connection
                        // 对连接超时的时间的统计
                        state.claimedOverdueConnectionCount++;
                        state.accumulatedCheckoutTimeOfOverdueConnections += longestCheckoutTime;
                        state.accumulatedCheckoutTime += longestCheckoutTime;
                        // 从活跃的连接集合中移除
                        state.activeConnections.remove(oldestActiveConnection);
                        // 如果非自动提交的，需要进行回滚。即将原有执行中的事务，全部回滚。
                        if (!oldestActiveConnection.getRealConnection().getAutoCommit()) {
                            try {
                                oldestActiveConnection.getRealConnection().rollback();
                            } catch (SQLException e) {
                                /*
                                   Just log a message for debug and continue to execute the following
                                   statement like nothing happened.
                                   Wrap the bad connection with a new PooledConnection, this will help
                                   to not interrupt current executing thread and give current thread a
                                   chance to join the next competition for another valid/good database
                                   connection. At the end of this loop, bad {@link @conn} will be set as null.
                                */
                                log.debug("Bad connection. Could not roll back");
                            }
                        }
                        // 创建新的 PooledConnection 连接对象
                        conn = new PooledConnection(oldestActiveConnection.getRealConnection(), this);
                        conn.setCreatedTimestamp(oldestActiveConnection.getCreatedTimestamp());
                        conn.setLastUsedTimestamp(oldestActiveConnection.getLastUsedTimestamp());
                        // 设置 oldestActiveConnection 为无效
                        oldestActiveConnection.invalidate();
                        if (log.isDebugEnabled()) {
                            log.debug("Claimed overdue connection " + conn.getRealHashCode() + ".");
                        }
                    } else { // 检查到未超时
                        // Must wait
                        try {
                            // 对等待连接进行统计。通过 countedWait 标识，在这个循环中，只记录一次。
                            if (!countedWait) {
                                state.hadToWaitCount++;
                                countedWait = true;
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("Waiting as long as " + poolTimeToWait + " milliseconds for connection.");
                            }
                            // 记录当前时间
                            long wt = System.currentTimeMillis();
                            // 等待，直到超时，或 pingConnection 方法中归还连接时的唤醒
                            state.wait(poolTimeToWait);
                            // 统计等待连接的时间
                            state.accumulatedWaitTime += System.currentTimeMillis() - wt;
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }
            // 获取到连接
            if (conn != null) {
                // ping to server and check the connection is valid or not
                // 通过 ping 来测试连接是否有效
                if (conn.isValid()) {
                    // 如果非自动提交的，需要进行回滚。即将原有执行中的事务，全部回滚。
                    // 这里又执行了一次，有点奇怪。目前猜测，是不是担心上一次适用方忘记提交或回滚事务 TODO 1001 芋艿
                    if (!conn.getRealConnection().getAutoCommit()) {
                        conn.getRealConnection().rollback();
                    }
                    // 设置获取连接的属性
                    conn.setConnectionTypeCode(assembleConnectionTypeCode(dataSource.getUrl(), username, password));
                    conn.setCheckoutTimestamp(System.currentTimeMillis());
                    conn.setLastUsedTimestamp(System.currentTimeMillis());
                    // 添加到活跃的连接集合
                    state.activeConnections.add(conn);
                    // 对获取成功连接的统计
                    state.requestCount++;
                    state.accumulatedRequestTime += System.currentTimeMillis() - t;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("A bad connection (" + conn.getRealHashCode() + ") was returned from the pool, getting another connection.");
                    }
                    // 统计获取到坏的连接的次数
                    state.badConnectionCount++;
                    // 记录获取到坏的连接的次数【本方法】
                    localBadConnectionCount++;
                    // 将 conn 置空，那么可以继续获取
                    conn = null;
                    // 如果超过最大次数，抛出 SQLException 异常
                    // 为什么次数要包含 poolMaximumIdleConnections 呢？相当于把激活的连接，全部遍历一次。
                    if (localBadConnectionCount > (poolMaximumIdleConnections + poolMaximumLocalBadConnectionTolerance)) {
                        if (log.isDebugEnabled()) {
                            log.debug("PooledDataSource: Could not get a good connection to the database.");
                        }
                        throw new SQLException("PooledDataSource: Could not get a good connection to the database.");
                    }
                }
            }
        }
    }

    // 获取不到连接，抛出 SQLException 异常
    if (conn == null) {
        if (log.isDebugEnabled()) {
            log.debug("PooledDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
        }
        throw new SQLException("PooledDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
    }

    return conn;
}
````

#### 5.2.2.3 pushConnection ####
pushConnection(PooledConnection conn) 方法，将使用完的连接，添加回连接池中。

整体流程如下图：

![](/picture/mybatis-pool-pushConnection.png)

代码如下：

````
protected void pushConnection(PooledConnection conn) throws SQLException {
    synchronized (state) {
        // 从激活的连接集合中移除该连接
        state.activeConnections.remove(conn);
        // 通过 ping 来测试连接是否有效
        if (conn.isValid()) { // 有效
            // 判断是否超过空闲连接上限，并且和当前连接池的标识匹配
            if (state.idleConnections.size() < poolMaximumIdleConnections && conn.getConnectionTypeCode() == expectedConnectionTypeCode) {
                // 统计连接使用时长
                state.accumulatedCheckoutTime += conn.getCheckoutTime();
                // 回滚事务，避免适用房未提交或者回滚事务
                if (!conn.getRealConnection().getAutoCommit()) {
                    conn.getRealConnection().rollback();
                }
                // 创建 PooledConnection 对象，并添加到空闲的链接集合中
                PooledConnection newConn = new PooledConnection(conn.getRealConnection(), this);
                state.idleConnections.add(newConn);
                newConn.setCreatedTimestamp(conn.getCreatedTimestamp());
                newConn.setLastUsedTimestamp(conn.getLastUsedTimestamp());
                // 设置原连接失效
                // 为什么这里要创建新的 PooledConnection 对象呢？避免使用方还在使用 conn ，通过将它设置为失效，万一再次调用，会抛出异常
                conn.invalidate();
                if (log.isDebugEnabled()) {
                    log.debug("Returned connection " + newConn.getRealHashCode() + " to pool.");
                }
                // 唤醒正在等待连接的线程
                state.notifyAll();
            } else {
                // 统计连接使用时长
                state.accumulatedCheckoutTime += conn.getCheckoutTime();
                // 回滚事务，避免适用房未提交或者回滚事务
                if (!conn.getRealConnection().getAutoCommit()) {
                    conn.getRealConnection().rollback();
                }
                // 关闭真正的数据库连接
                conn.getRealConnection().close();
                if (log.isDebugEnabled()) {
                    log.debug("Closed connection " + conn.getRealHashCode() + ".");
                }
                // 设置原连接失效
                conn.invalidate();
            }
        } else { // 失效
            if (log.isDebugEnabled()) {
                log.debug("A bad connection (" + conn.getRealHashCode() + ") attempted to return to the pool, discarding connection.");
            }
            // 统计获取到坏的连接的次数
            state.badConnectionCount++;
        }
    }
}
````
该方法会被 PooledConnection 的 invoke 在 methodName = close 方法的情况下时被调用。

#### 5.2.2.4 pingConnection ####
pingConnection(PooledConnection conn) 方法，通过向数据库发起 poolPingQuery 语句来发起“ping”操作，以判断数据库连接是否有效。

````
protected boolean pingConnection(PooledConnection conn) {
    // 记录是否 ping 成功
    boolean result;

    // 判断真实的连接是否已经关闭。若已关闭，就意味着 ping 肯定是失败的。
    try {
        result = !conn.getRealConnection().isClosed();
    } catch (SQLException e) {
        if (log.isDebugEnabled()) {
            log.debug("Connection " + conn.getRealHashCode() + " is BAD: " + e.getMessage());
        }
        result = false;
    }

    if (result) {
        // 是否启用侦测查询
        if (poolPingEnabled) {
            // 判断是否长时间未使用。若是，才需要发起 ping
            if (poolPingConnectionsNotUsedFor >= 0 && conn.getTimeElapsedSinceLastUse() > poolPingConnectionsNotUsedFor) {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Testing connection " + conn.getRealHashCode() + " ...");
                    }
                    // 通过执行 poolPingQuery 语句来发起 ping
                    Connection realConn = conn.getRealConnection();
                    try (Statement statement = realConn.createStatement()) {
                        statement.executeQuery(poolPingQuery).close();
                    }
                    if (!realConn.getAutoCommit()) {
                        realConn.rollback();
                    }
                    // 标记执行成功
                    result = true;
                    if (log.isDebugEnabled()) {
                        log.debug("Connection " + conn.getRealHashCode() + " is GOOD!");
                    }
                } catch (Exception e) {
                    // 关闭数据库真实的连接
                    log.warn("Execution of ping query '" + poolPingQuery + "' failed: " + e.getMessage());
                    try {
                        conn.getRealConnection().close();
                    } catch (Exception e2) {
                        //ignore
                    }
                    // 标记执行失败
                    result = false;
                    if (log.isDebugEnabled()) {
                        log.debug("Connection " + conn.getRealHashCode() + " is BAD: " + e.getMessage());
                    }
                }
            }
        }
    }
    return result;
}
````

#### 5.2.2.5 forceCloseAll ####
forceCloseAll() 方法，关闭所有的 activeConnections 和 idleConnections 的连接。

````
public void forceCloseAll() {
    synchronized (state) {
        // 计算 expectedConnectionTypeCode
        expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
        // 遍历 activeConnections ，进行关闭
        for (int i = state.activeConnections.size(); i > 0; i--) {
            try {
                // 设置为失效
                PooledConnection conn = state.activeConnections.remove(i - 1);
                conn.invalidate();

                // 回滚事务，如果有事务未提交或回滚
                Connection realConn = conn.getRealConnection();
                if (!realConn.getAutoCommit()) {
                    realConn.rollback();
                }
                // 关闭真实的连接
                realConn.close();
            } catch (Exception e) {
                // ignore
            }
        }
        // 遍历 idleConnections ，进行关闭
        //【实现代码上，和上面是一样的】
        for (int i = state.idleConnections.size(); i > 0; i--) {
            try {
                // 设置为失效
                PooledConnection conn = state.idleConnections.remove(i - 1);
                conn.invalidate();

                // 回滚事务，如果有事务未提交或回滚
                Connection realConn = conn.getRealConnection();
                if (!realConn.getAutoCommit()) {
                    realConn.rollback();
                }
                // 关闭真实的连接
                realConn.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }
    if (log.isDebugEnabled()) {
        log.debug("PooledDataSource forcefully closed/removed all connections.");
    }
}
````

#### 5.2.2.6 unwrapConnection ####
unwrapConnection(Connection conn) 方法，获取真实的数据库连接。

````
public static Connection unwrapConnection(Connection conn) {
    // 如果传入的是被代理的连接
    if (Proxy.isProxyClass(conn.getClass())) {
        // 获取 InvocationHandler 对象
        InvocationHandler handler = Proxy.getInvocationHandler(conn);
        // 如果是 PooledConnection 对象，则获取真实的连接
        if (handler instanceof PooledConnection) {
            return ((PooledConnection) handler).getRealConnection();
        }
    }
    return conn;
}
````

## 5.3 PoolState ##

org.apache.ibatis.datasource.pooled.PoolState ，连接池状态，记录空闲和激活的 PooledConnection 集合，以及相关的数据统计。

主要的属性有：

````
/**
 * 所属的 PooledDataSource 对象
 */
protected PooledDataSource dataSource;

/**
 * 空闲的 PooledConnection 集合
 */
protected final List<PooledConnection> idleConnections = new ArrayList<>();
/**
 * 激活的的 PooledConnection 集合
 */
protected final List<PooledConnection> activeConnections = new ArrayList<>();

/**
 * 全局统计 - 获取连接的次数
 */
protected long requestCount = 0;
/**
 * 全局统计 - 获取连接的时间
 */
protected long accumulatedRequestTime = 0;
/**
 * 全局统计 - 获取到连接非超时 + 超时的占用时长
 *
 * 所以，包括 {@link #accumulatedCheckoutTimeOfOverdueConnections} 部分
 */
protected long accumulatedCheckoutTime = 0;
/**
 * 全局统计 - 获取到连接超时的次数
 */
protected long claimedOverdueConnectionCount = 0;
/**
 * 全局统计 - 获取到连接超时的占用时长
 */
protected long accumulatedCheckoutTimeOfOverdueConnections = 0;
/**
 * 全局统计 - 等待连接的时间
 */
protected long accumulatedWaitTime = 0;
/**
 * 全局统计 - 等待连接的次数
 */
protected long hadToWaitCount = 0;
/**
 * 全局统计 - 获取到坏的连接的次数
 */
protected long badConnectionCount = 0;
````

## 5.4 PooledConnection ##
org.apache.ibatis.datasource.pooled.PooledConnection ，实现 InvocationHandler 接口，池化的 Connection 对象。

### 5.4.1 构造方法 ###

````
/**
 * 关闭 Connection 方法名
 */
private static final String CLOSE = "close";

/**
 * JDK Proxy 的接口
 */
private static final Class<?>[] IFACES = new Class<?>[]{Connection.class};

/**
 * 对象的标识，基于 {@link #realConnection} 求 hashCode
 */
private final int hashCode;
/**
 * 所属的 PooledDataSource 对象
 */
private final PooledDataSource dataSource;
/**
 * 真实的 Connection 连接
 */
private final Connection realConnection;
/**
 * 代理的 Connection 连接，即 {@link PooledConnection} 这个动态代理的 Connection 对象
 */
private final Connection proxyConnection;
/**
 * 从连接池中，获取走的时间戳
 */
private long checkoutTimestamp;
/**
 * 对象创建时间
 */
private long createdTimestamp;
/**
 * 最后更新时间
 */
private long lastUsedTimestamp;
/**
 * 连接的标识，即 {@link PooledDataSource#expectedConnectionTypeCode}
 */
private int connectionTypeCode;
/**
 * 是否有效
 */
private boolean valid;

/**
 * Constructor for SimplePooledConnection that uses the Connection and PooledDataSource passed in
 *
 * @param connection - the connection that is to be presented as a pooled connection
 * @param dataSource - the dataSource that the connection is from
 */
public PooledConnection(Connection connection, PooledDataSource dataSource) {
    this.hashCode = connection.hashCode();
    this.realConnection = connection;
    this.dataSource = dataSource;
    this.createdTimestamp = System.currentTimeMillis();
    this.lastUsedTimestamp = System.currentTimeMillis();
    this.valid = true;
    // <1> 创建代理的 Connection 对象
    this.proxyConnection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), IFACES, this);
}
````

### 5.4.2 invoke ###
invoke(Object proxy, Method method, Object[] args) 方法，代理调用方法。

````
@Override
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    String methodName = method.getName();
    // 判断调用的方法是不是 Connection#close() 方法，如果是，则调用 PooledDataSource#pushConnection(PooledConnection conn) 方法，将该连接放回到连接池中，从而避免连接被关闭。
    if (CLOSE.hashCode() == methodName.hashCode() && CLOSE.equals(methodName)) {
        dataSource.pushConnection(this);
        return null;
    } else {
        try {
            // 判断非 Object 的方法，则额外调用 #checkConnection() 方法，则先检查连接是否可用。
            if (!Object.class.equals(method.getDeclaringClass())) {
                // issue #579 toString() should never fail
                // throw an SQLException instead of a Runtime
                checkConnection();
            }
            // 反射调用对应的方法。
            return method.invoke(realConnection, args);
        } catch (Throwable t) {
            throw ExceptionUtil.unwrapThrowable(t);
        }
    }
}
````

### 5.4.3 isValid ###
isValid() 方法，校验连接是否可用。

- 当连接有效时，调用 PooledDataSource#pingConnection(PooledConnection conn) 方法，向数据库发起 “ping” 请求，判断连接是否真正有效。

### 5.4.4 invalidate ###
invalidate() 方法，设置连接无效。

````
public boolean isValid() {
    return valid && realConnection != null && dataSource.pingConnection(this);
}
````

----