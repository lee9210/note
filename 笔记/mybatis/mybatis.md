SqlSessionFactory是MyBatis的关键对象,它是个单个数据库映射关系经过编译后的内存镜像.
SqlSession是MyBatis的关键对象,是执行持久化操作的独享,类似于JDBC中的Connection.它是应用程序与持久层之间执行交互操作的一个单线程对象,也是MyBatis执行持久化操作的关键对象.SqlSession对象完全包含以数据库为背景的所有执行SQL操作的方法,它的底层封装了JDBC连接,可以用SqlSession实例来直接执行被映射的SQL语句.每个线程都应该有它自己的SqlSession实例.SqlSession的实例不能被共享,同时SqlSession也是线程不安全的,绝对不能讲SqlSeesion实例的引用放在一个类的静态字段甚至是实例字段中.也绝不能将SqlSession实例的引用放在任何类型的管理范围中,比如Servlet当中的HttpSession对象中.使用完SqlSeesion之后关闭Session很重要,应该确保使用finally块来关闭它.

## 构建SqlSessionFactory过程 ##
构造分两步：
1. 通过org.apache.ibatis.builder.xml.XMLConfigBuilder解析配置的xml文件，读出配置参数，并将读取的数据存入这个org.apache.ibatis.session.Configuration类中。mybatis几乎所有的配置都是存在这里的。
2. 使用Configuration对象去创建SqlSessionFactory。mybatis中的SqlSessionFactory是一个接口类，而不是实现类，默认的实现类是org.apache.ibatis.session.defaults.DefaultSqlSessionFactory。大部分情况下都没有必要自己去创建新的SqlSessionFactory的实现。

### 构建Configuration ###
在SqlSessionFactory构建中，Configuration是最总要的，它的作用如下：
- 读取配置文件，包括基础配置的xml文件和映射器的xml文件
- 初始化基础配置，比如mybatis的别名等，一些重要的类对象，例如，插、映射器、ObjectFactory和typeHandler对象
- 提供单例，为后续创建SessionFactory服务提供配置的参数。
- 执行一些重要的对象方法，初始化配置信息。

Configuration通过XMLConfigBuilder构建，主要做如下初始化：
- properties全局参数
- settings设置
- typeAliases别名
- typeHandler类型处理器
- ObjectFactory对象
- plugin插件
- environment环境
- DatabaseIdProvider数据库标识
- Mapper映射器

### 映射器的内部组成 ###
映射器由3个部分组成
- MappedStatement,保存了映射器的一个节点（select|insert|delete|update）。包括了我们配置的SQL、SQL的id、缓存信息、resultMap、parameterType、resultType、languageDriver等重要配置内容
- SqlSource,提供BoundSql对象的地方，是MappedStatement的一个属性
- BoundSql,建立SQL和参数的地方。有3个常用属性：SQL,parameterObject、parameterMappings

对于参数和sql而言，主要的规则都反映在BoundSql类对象上，在插件中往往需要拿到它进而可以拿到当前运行的sql和参数以及参数规则，做出适当的修改。
BoundSql会提供3个主要属性：parameterMappings、parameterObject和sql。

parameterObject为参数本身。可以传递简单对象，POJO、Map或者@Param注解的参数。
#### parameterObject传递规则 ####
- 传递简单对象（int,String,float,double等），传递的是封装的对象，int封装成Integer传递...
- 传递的是POJO或Map，parameterObject就是传入的POJO或者Map不变
- 传递多个参数，如果没有用@Param注解，就会把parameterObject变成一个Map<String,Object>对象，键值类似于{"1":p1,...,"param1":p1,...}。可以用#{param1}或#{1}获取
- 用@Param注解，parameterObject变成一个Map<String,Object>对象，把key值变成@Param的key

parameterMappings，是一个list，每个元素都是ParameterMapping的对象。这个对象描述参数。参数包括属性、名称、表达式、javaType、jdbcType、typeHandler等重要信息。通过它可以实现参数和sql的结合，以便PreparedStatement能够通过它找到parameterObject对象的属性并设置参数，使程序准确运行。 

SQL属性是我们书写在映射器里面的一条SQL

**映射器的XML文件的命名空间对应的便是这个接口的全路径，根据全路径和方法名，便能够绑定起来，通过动态代理技术，让这个接口跑起来。**

### SqlSession下的四大对象 ###
Mapper执行的过程是通过Executor、StatementHandler、ParameterHandler和ResultHandler来完成数据库操作和结果返回的
- Executor代表执行器，由它来调度StatementHandler、ParameterHandler和ResultHandler来执行对应的sql
- StatementHandler的作用是使用数据库的Statement(PreparedStatement)执行操作，
- ParameterHandler用于SQL对参数的处理
- ResultHandler是进行最后数据集的封装返回处理。

#### Executor ####
执行java和数据库交互。在mybatis中存在三种执行器
- SIMPLE，简单执行器，不配置它就是默认执行器
- REUSE，执行器重用预处理语句
- BATCH，执行器重用语句和批量更新，针对批量专用的执行器

mybatis根据congifuration来构建statementHandler，然后调用prepareStatement方法，对sql编译并对参数进行初始化，resultHandler再组装查询结果并返回给调用者来完成一次查询。

#### 数据库会话器 ####
主要作用是编译sql，把参数和sql语句合并成一条可执行的sql。
statementHandler分为三种：SimpleStatementHandler、PreparedStatementHandler、CallableStatementHandler。

**查询一条sql的执行过程。**
Executor会先调用StatementHandler的prepare()方法预编译SQL语句，同时设置一些基本运行的参数。然后调用parameterise()方法启用ParameterHandler设置参数，完成预编译，跟着就执行查询。最后如果需要，就用ResultSetHandler封装结构返回给调用者。

### 参数处理器 ###
Mybatis通过参数处理器（ParameterHandler）对预编译语句进行设置。
主要作用是对参数进行处理，方便StatementHandler取出参数。
#### 结果处理器 ####








123




