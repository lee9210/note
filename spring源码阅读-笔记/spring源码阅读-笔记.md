##1. bean标签的解析##
1. bean基础属性
BeanDefinition的作用是描述一个bean实例，它具有属性值、构造函数参数值和具体实现提供的进一步信息。
BeanDefinitionHolder包含一个BeanDefinition实例，并包含有beanName,beanAliass(别名)。可以注册为内部bean的占位符。
BeanDefinition是一个接口，在spring中有三种实现:RootBeanDefinition.ChildBeanDefinition.GenericBeanDefinition.三种实现均继承了AbstractBeanDefinition,其中，BeanDefinition是配置文件<bean>元素标签在容器中的内部表示。  
RootBeanDefinition是最常用的实现类。GenericBeanDefinition是一站式服务类。其中有没有父类或者父bean标签用RootBeanDefinition表示，子类用ChildBeanDefinition表示。    
AnnotatedGenericBeanDefinition:以@Configuration注解标记的会解析为AnnotatedGenericBeanDefinition  
ConfigurationClassBeanDefinition:以@Bean注解标记的会解析为ConfigurationClassBeanDefinition    
ScannedGenericBeanDefinition:以@Component注解标记的会解析为ScannedGenericBeanDefinition  
BeanDefinitionBuilder:可以使用BeanDefinitionBuilder来构建BeanDefinition
[详细介绍](https://www.jianshu.com/p/a6a03d94d6f7)  
**其中GenericBeanDefinition可以代替两者。ChildBeanDefinition不允许动态修改 parentName【父BD】，所以已经逐渐弃用，转而用GenericBeanDefinition。** 

2. bean子标签属性
	1. parent:为了简化属性配置，采用继承的方式，获取父类的属性的值，或者覆盖父类的值。父类不能被实例化，也不能引用[详细介绍](https://www.cnblogs.com/huang0925/p/3644096.html)
	2. lookup-method:动态配置返回的数据，[详细介绍](https://www.cnblogs.com/ViviChan/p/4981619.html)
	3. replace-method:用新的逻辑代替原来的逻辑[详细介绍](https://www.cnblogs.com/mjorcen/p/3647234.html)
	4. property:设置对应属性的值
	5. qualifier:设置此bean对应的具体的实例化bean  
	6. constructor-arg:指定构造函数的参数。
tip：
	property和qualifier的区别：property设置的是filed，qualifier设置的是本bean对应的实例。

3. bean标签属性
	1. scope:[详细介绍](https://www.cnblogs.com/liaojie970/p/8302749.html)
		1. singleton:单例
		2. prototype:每次请求的时候创建一个新的实例
		3. request:只适用于web程序，通常是和XmlWebApplicationContext共同使用，为每个请求创建一个新的实例
		4. session:只适用于web程序，Spring容器会为每个独立的session创建属于自己的全新的UserPreferences实例，比request scope的bean会存活更长的时间
		5. globalsession:只适用于web程序,global session只有应用在基于porlet的web应用程序中才有意义，它映射到porlet的global范围的session，如果普通的servlet的web 应用中使用了这个scope，容器会把它作为普通的session的scope对待。
	2. abstract:表示当前bean是一个抽象的bean，从而不会为它生成实例化对象。(强行生成会报错)
	3. lazy-init:不会在ApplicationContext启动时提前被实例化，而是第一次向容器通过getBean索取bean时实例化的。
	4. autowire: 自动装配。有5种方式:no.byType.byName.constractor.defalut[详细介绍](https://www.cnblogs.com/ViviChan/p/4981539.html)
	5. depends-on:一个bean对其他bean的依赖关系.<ref/>标签是一个bean对其他bean的引用，而depends-on属性只是表明依赖关系（不一定会引用），这个依赖关系决定了被依赖的bean必定会在依赖bean之前被实例化，反过来，容器关闭时，依赖bean会在被依赖的bean之前被销毁。[详细介绍](https://www.cnblogs.com/ViviChan/p/4981518.html)
	6. autowire-candidate:设置当前bean在被其他对象作为自动注入对象的时候，是否作为候选bean，默认值是true。属性有3个可选值:default.true(为候选者).false(不作为候选者)
	7. primary:有多个实现类，默认的用primary
	8. init-method: 用于在bean初始化时指定执行方法
	9. destory-method:用于在bean销毁的时候执行的方法，一般用于关闭资源连接
	10. factory-bean：用于实例化工厂类.（和factory-method区分）
	11. factory-method：用于调用工厂类静态方法，创建实例。[详细介绍](https://www.cnblogs.com/vickylinj/p/9474597.html)
	
4. bean标签的解析及注册步骤：
	1. 首先委托BeanDefinitionDelegate类的parseBeanDefinitionElement方法进行元素解析，返回BeanDefinitionHolder实例bdHolder，此时bdHolder已经包含配置文件中的各种属性，例如class，name,id,alias
	2. 当返回的bdHolder不为空的情况下若存在默认标签下的子节点下有自定义属性，需要再次解析。
	3. 完成解析后，对bdHolder进行注册，
	4. 发出响应事件，通知相关监听器，这个bean已经加载完成。（spring没有实现响应逻辑，用户自己扩展）
	
5. bean的注册详细步骤：
	1. 对AbstractBeanDefinition的校验。
	2. 对beanName已经注册的情况处理。如果设置了不允许bean的覆盖，则抛出异常，否则直接覆盖。
	3. 加入map缓存。
	4. 清除解析之前留下的对应的beanName缓存。
	
6. 通过别名注册BeanDefinition：
	1. alias与beanName相同情况处理。如果beanName与alias相同的话不记录alias，并删除对应的alias
	2. alias覆盖处理。如果alias不允许覆盖，则抛出异常
	3. alias覆盖检查。当A->B存在时，若再出现A->C->B则抛出异常
	4. 注册alias。

##2. 自定义标签解析 ##
spring提供了可扩展的Schema的支持，使用需要以下几个步骤：
	1. 创建一个可扩展的组件
	2. 定义一个XSD文件描述组件内容
	3. 创建一个文件，实现BeanDefinitionParser接口，用来解析XSD文件中的定义和组件定义
	4. 创建一个Handler文件，扩展自NamespaceHandlerSupport,目的是将组件注册到spring容器
	5. 编写Spring.handlers和Spring.schemas文件
	
1. 自定义标签解析步骤：BeanDefinitionParserDelegate.parseCustomElement(Element ele, @Nullable BeanDefinition containingBd) 
	1. 获取对应的命名空间
	2. 根据命名空间找到对应的NamespaceHandler
	3. 调用自定义的NamespaceHandler进行解析

2. 获取命名空间处理器步骤：DefaultNamespaceHandlerResolver.resolve()
	1. 获取所有已配置的handler映射
	2. 根据命名空间找到对应的信息
	3. 已经做过解析的情况，直接从缓存读取;没有做过解析,调用自定义NamespaceHandler初始化方法,记录缓存,返回NamespaceHandler

总结来说：通过自定义XSD文件以及自定义解析逻辑，对自定义的标签进行解析。有了对自定义标签，我们可以在Spring的xml文件中根据自己的需要实现自己的处理逻辑。另外需要说明的是，Spring源码中也大量使用了自定义标签，比如spring的AOP的定义，其标签为<aspectj-autoproxy />

##3. bean的加载 ##

1. bean加载过程：
	1. 转换对应beanName。解析内容包括以下几个内容
		1. 取出FactoryBean的修饰符，即如果是name="&aa",那么会首先去除&而使name="aa"。
		2. 取指定alias所表示的最终beanName,例如别名A指向名称为B的bean，则返回B；若别名A指向别名B，别名B又指向名称为C的bean，则返回C
	2. 尝试从缓存中加载单列。单例在spring的同一个容器中只会被创建一次，后续再获取bean，就直接从单列缓存中获取。这儿是尝试获取，如果加载不成功，则再次尝试从singletonFactories中加载。
		因为在创建单例bean的时候会存在依赖注入的情况，而在创建依赖的时候，为了避免循环依赖，在spring中创建bean的原则是不等bean创建完成就会将创建bean的ObjectFactory提早曝光加入到缓存中。
		一旦下一个bean创建时候需要依赖上一个bean则直接使用ObjectFactory
	3. bean的实例化。如果从缓存中得到了bean的原始状态，则需要对bean进行实例化。因为缓存中记录的只是最原始的bean状态，并不一定是我们最终想要的bean。
		例如，加入我们需要对工厂进行处理，那么这里得到的其实是工厂bean的初始状态，但是我们真正需要的是工厂bean中定义的factory-method方法中返回的bean。而getObjectForBeanInstance就是完成这个工作的。
	4. 原始模型的依赖检查。只有在单例情况下才会尝试解决循环依赖。
	5. 检测parentBeanFactory
	6. 将存储XML配置文件的GernericBeanDefinition转换为RootBeanDefinition。因为从XML配置文件中读取到的bean信息是存储在GernericBeanDefinition重点 ，但是所有的bean后续处理都是针对RootBeanDefinition。所以这里需要进行一个转换，转换的同事如果父类bean不为空的话，则会一并合并父类的属性。
	7. 寻找依赖。
	8. 针对不同的scope进行bean的创建。
	9. 类型转换。
	
2. FactoryBean

用户可以通过实现该接口定制实例化bean的逻辑。它因此昂了实例化一些负载bean的细节，给上层应用带来便利。
可以自己定义一个bean该怎么实例化，以及实例化的逻辑
````
public interface FactoryBean<T> {

	/**
	 * 返回由FactoryBean创建的实例，如果isSingleton()返回true，则该实例会放到spring容器中单例缓存池中。
	 */
	@Nullable
	T getObject() throws Exception;

	/**
	 * 返回FactoryBean创建的bean类型
	 */
	@Nullable
	Class<?> getObjectType();

	/**
	 * 返回由FactoryBean创建的bean实例的作用于是singleton还是prototype
	 */
	default boolean isSingleton() {		return true;	}

}
````
3. 缓存中获取单例bean。
	如果在创建单例bean的时候存在依赖注入的情况，而在创建依赖的时候，为了避免循环依赖，spring创建bean的原则是不等bean创建完成就会将创建bean的ObjectFactory提早曝光加入到缓存中，一旦下一个bean创建时需要依赖上个bean，则直接使用ObjectFactory.

DefaultSingletonBeanRegistry中各个map的解释
1. singletonObjects:用于保存BeanName和创建bean实例之间的关系，bean name -> bean instance
2. singletonFactories:用于保存BeanName和创建bean的工厂之间的关系， bean name -> ObjectFactory
3. earlySingletonObjects:也是保存BeanName和创建bean实例之间的关系，与singletonObjects的不同之处在于当一个单例bean被放大这个里面后，那么当bean还在创建过程中，就可以通过getBean方法获取到了，其目的是用来检测循环引用。
4. registedSingletons:用来保存当前所有已注册的bean

4. 获取单例函数DefaultSingletonBeanRegistry.getSingleton(String beanName, ObjectFactory<?> singletonFactory)的作用是使用回调方法，使得程序可以在创建单例前后做一些准备及处理操作。而真正获取单例的操作不是在这个方法中实现的，其实现逻辑是在ObjectFactory类型的实例singletonFactory中实现的。
相当于一个获取bean的前后处理程序。

实例化前的后处理程序：bean实例化前调用，即把AbstractBeanDefinition转换成BeanWrapper前的处理。给子类一个修改BeanDefinition的机会。经过这个处理后，bean有可能不是我们认识的bean了，有可能是CGLib生成的，也有可能是其他生成的。
实例化后的后处理程序：主要是调用postProcessAfterInitialization方法

### 循环依赖的几种情况 ###
testA->testB->testC->testA
**构造器**
抛出异常，spring不能解决
1. spring容器创建"testA" bean，首先去"当前创建bean池"查找是否当前bean正在创建，如果没有发现，则继续准备其需要的构造器参数"testB",并将"testA"标识符放到"当前创建bean池"。
2. spring容器创建"testB" bean，首先去"当前创建bean池"查找是否当前bean正在创建，如果没有发现，则继续准备其需要的构造器参数"testC",并将"testB"标识符放到"当前创建bean池"。
3. spring容器创建"testB" bean，首先去"当前创建bean池"查找是否当前bean正在创建，如果没有发现，则继续准备其需要的构造器参数"testA",并将"testC"标识符放到"当前创建bean池"。
4. 到此为止，spring容器要去创建"testA" bean，发现该bean标识符在"当前创建bean池"中，因为表示循环依赖，抛出BeanCurrentlyCreationException

**setter依赖**
1. spring容器创建"testA" bean，首先根据无参构造器创建bean，并暴露一个"ObjectFactory"用于方法一个提前暴露一个创建中的bean，并将"testA"标识符放到"当前创建bean池"，然后setter注入"testB".
2. spring容器创建"testB" bean，首先根据无参构造器创建bean，并暴露一个"ObjectFactory"用于方法一个提前暴露一个创建中的bean，并将"testA"标识符放到"当前创建bean池"，然后setter注入"testC".
3. spring容器创建"testC" bean，首先根据无参构造器创建bean，并暴露一个"ObjectFactory"用于方法一个提前暴露一个创建中的bean，并将"testA"标识符放到"当前创建bean池"，然后setter注入"testA".进行注入"testA"时由于提前暴露了"ObjectFactory"工厂，从而使用它返回提前暴露一个创建中的bean。
4. 最后依赖注入testB和testA，完成setter注入

**prototype范围依赖**
对于prototype作用于bean，spring容器无法完成依赖注入，因为spring容器不进行缓存"prototype"作用域的bean，因此无法提前暴露一个创建中的bean。

创建bean（在doCreateBean中完成）的步骤：
1. 如果是单例则需要首先清除缓存
2. 实例化bean，将BeanDefinition转换为BeanWrapper.转换是一个复杂的过程，
	1. 如果存在工厂方法，则使用工厂方法进行初始化
	2. 一个类有多个构造函数，每个构造函数都有不同的参数，所有需要根据参数锁定构造函数并进行初始化。
	3. 如果即不存在工厂方法也不存在带有参数的构造函数，则使用默认的构造函数进行bean的实例化
3. MergedBeanDefinitionPostProcessor的应用。bean合并后的处理，Autowired主机正式通过此方法实现注入类型的预解析
4. 依赖处理。在此处解决循环依赖的问题
5. 属性填充。将所有属性填充至bean的实例中
6. 循环依赖检查。在此处检查已经加载的bean是否已经出现了依赖循环，并判断是否需要抛出异常。
7. 注册DisposableBean，如果配置了destroy-method,这里需要注册以便于在销毁的时候调用。
8. 完成创建并返回。

创建bean逻辑(createBeanInstance)
1. 如果在RootBeanDefinition中存在factoryMethodName属性，或者说在配置文件中配置了factory-method,那么spring会尝试使用instantiateUsingFactoryMethod(beanName,mbd,args)方法根据RootBeanDefinition中的配置生成bean实例
2. 解析构造函数并进行构造函数的实例化。因为一个bean对应的雷中可能会有多个构造函数，而每个构造函数的参数不同，spring在根据参数及类型去判断最终会使用哪个构造函数进行实例化。但是判断的过程是个比较消耗性能个步骤，所以采用缓存机制，如果已经解析过则不需要重复解析而是直接从RootBeanDefinition中的属性resolvedConstrutorOrFactoryMethod缓存值去取，否则需要再次解析，并将解析的结果添加至RootBeanDefinition中的属性resolvedConstructorOrFactoryMethod中。

创建spring分两种情况，一种是通用实例化，另一种是带有参数的实例化。

带有参数的实例化：autowireConstructor
此函数主要考虑以下几个方面。
1. 构造函数参数的确定。
	1. 根据explictArgs参数判断。如果传入的参数explictArgs不为空，那可以直接确定参数。因为explictArgs参数是在调用bean的时候用户指定的。
	2. 缓存中获取。
	3. 配置文件获取。
2. 构造函数的确定。
3. 根据确定的构造函数转回对应的参数类型
4. 构造函数不确定性的验证。
5. 根据实例化策略以及得到的构造函数及构造函数参数实例化bean

不带参数的构造函数实例化:instantiateBean

在bean初始化过程中，需要处理的有属性注入，init-method运行,destory-method的注册，以及一些AOP逻辑的处理。

##容器的扩展功能##
BeanFactory和ApplicationContext都用于加载bean。相比之下，ApplicationContext提供了更多的扩展功能



##AOP##
JDK动态代理:其代理对象必须是某个接口的实现，他是通过在运行期间创建一个接口的实现类来完成对目标对象的代理。
CGLib代理:实现原理类似于JDK动态代理，他是在运行期间生成的代理对象是针对目标类扩展的子类.

基本术语（一些名词）：
（1）切面(Aspect)
切面泛指[*交叉业务逻辑*]。事务处理和日志处理可以理解为切面。常用的切面有通知(Advice)与顾问(Advisor)。实际就是对主业务逻辑的一种增强。

(2)织入（Weaving）
织入是指将切面代码插入到目标对象的过程。代理的invoke方法完成的工作，可以称为织入。

（3） 连接点(JoinPoint)
连接点是指可以被切面织入的方法。通常业务接口的方法均为连接点

（4）切入点(PointCut)
切入点指切面具体织入的方法
注意：被标记为final的方法是不能作为连接点与切入点的。因为最终的是不能被修改的，不能被增强的。

(5)目标对象（Target）
目标对象指将要被增强的对象。即包含主业务逻辑的类的对象。

（6）通知（Advice）
通知是切面的一种实现，可以完成简单的织入功能。通知定义了增强代码切入到目标代码的时间点，是目标方法执行之前执行，还是执行之后执行等。切入点定义切入的位置，通知定义切入的时间。

（7）顾问(Advisor)
顾问是切面的另一种实现，能够将通知以更为复杂的方式织入到目标对象中，是将通知包装为更复杂切面的装配器。


##springMVC##
在spring的mvc中，rootContext其实是spring自己创建的webApplicationContext;

Servlet中设置的初始化参数：
contextAttribute:在ServletContext中，要用作WebApplicationContext的属性名称
contextClass:创建WebApplicationContext的类型
contextConfigLocation:spring MVC配置文件的位置。
publishContext:是否将webApplicationContext设置到ServletContext的属性

###DispatcherServlet###
DispatcherServlet的创建过程主要是对九大组件进行初始化。包括：HandlerMapping、HandlerAdapter、HandlerExceptionResolver、ViewResolver、RequestToViewNameTranslator、LocaleResolver、ThemeResolver、MultipartResolver、FlashMapManager
属性：
- HandlerMapping：用于 handlers 映射请求和一系列的对于拦截器的前处理和后处理，大部分用 @Controller 注解。
- HandlerAdapter：帮助 DispatcherServlet 处理映射请求处理程序的适配器，而不用考虑实际调用的是哪个处理程序。
- ViewResolver：根据配置解析实际的 View 类型。
- ThemeResolver：解决 Web 应用程序可以使用的主题，例如提供个性化布局。
- MultipartResolver：解析多部分请求，以支持从 HTML 表单上传文件。
- FlashMapManager：存储并检索可用于将一个请求属性传递到另一个请求的 input 和 output 的 FlashMap，通常用于重定向。

在 Web MVC 框架中，每个 DispatcherServlet 都拥有自己的 WebApplicationContext，它继承了 ApplicationContext。WebApplicationContext 包含了其上下文和 Servlet 实例之间共享的所有 beans。

各个属性作用：
1. HandlerMapping：处理请求的映射。
	1. SimpleUrlHandlerMapping：通过配置文件把 URL 映射到 Controller 类。
	2. DefaultAnnotationHandlerMapping：通过注解把 URL 映射到 Controller 类。
2. HandlerAdapter：处理请求映射
	1. AnnotationMethodHandlerAdapter：通过注解，把请求 URL 映射到 Controller 类的方法上。
3. HandlerExceptionResolver：异常处理
	1. SimpleMappingExceptionResolver：通过配置文件进行异常处理。
	2. AnnotationMethodHandlerExceptionResolver：通过注解进行异常处理。
4. ViewResolver：解析 View 视图
	1. UrlBasedViewResolver：通过配置文件，把一个视图名交给一个 View 来处理。

####doDispatch函数####
doDispatch从顶层设置了整个请求处理的过程。主要涉及四个步骤
````
// 1. 根据request找到handler: 
mappedHandler = getHandler(processedRequest);
// 2. 根据handler找到HandlerAdapter
HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());
// 3. 用HandlerAdapter处理handler
mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
// 4. 调用processDispatchResult方法处理上面处理之后的结果(包含找到view并渲染输出给用户)
processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
````
**其中的概念**
handler:处理器，他直接对应着MVC中的C，也就是Controller层，他的具体表现形式有很多，可以是类，也可以是方法，他的类型是Object。
	比如类中标注了@RequestMapping的所有方法都可以看成一个handler,只要可以实际处理请求就可以是handler
HandlerMapping:用来查找handler的。在spring MVC中会处理很多请求，每个请求都需要一个handler来处理，具体接收到一个请求后，使用哪个handler来处理就是HandlerMapping来做的事情。
HandlerAdapter:适配器。因为spring MVC中的handler可以是任意形式的，只要能处理请求就OK，但是servlet需要的处理方法的结构确实固定的，可以是request和response为参数的方法(如doService方法)。这就是HandlerAdapter需要处理的事情。

通常来说，handler是用来干活的工具，HandlerMapping用于根据需要干的活找到相应的工具，HandlerAdapter是使用干活的人。所以不同的handler需要不同的HandlerAdapter去使用。
所以，上面的代码大概意思就是：使用HandlerMapping找到干活的handler，找到使用handler的HandlerAdapter，让HandlerAdapter使用handler干活，干完活后将结果写个报告交上去(通过view展示给用户)

另外，View和ViewResoler的原理与handler和HandlerMapping的原理类似。

doDispatch大体可以分为两部分：处理请求和渲染页面。开头部分先定义了几个变量，如下：
HttpServletRequest processedRequest:实际处理时所用的request，如果不是上传请求，则直接使用接收到的request,否则封装为上传类型的request。
HandlerExecutionChain mappedHandler:处理请求的处理器链(包含处理器和对应的Interceptor)
boolean multipartRequestParsed:是不是上传请求的标志
ModelAndView mv:封装Modle和View的容器
Exception dispatchException:处理请求过程中抛出的异常(不包含渲染过程抛出的异常)。
流程图：
 ![](/picture/doDispatcher-flow.png)


###FrameworkServlet###
在FrameworkServlet中重写了service、doGet、doPost、doPut、doDelete、doOptions、doTrace方法（除了doHead的所有处理请求的方法）。
在service方法中增加了对PATCH类型的处理，其他类型的请求直接交给了父类进行处理；
doOptions和doTrace方法可以通过设置dispatchOptionsRequest和dopatchTraceRequest参数决定是自己处理还是交给父类处理（默认交给父类处理，doOptions会在父类的处理结果中增加PATCH类型）；
doGet、doPost、doPut、doDelete都是自己处理。所有需要自己处理的请求都交给了processRequest方法进行统一处理。
在processRequest中的主要涉及到两个属性的设置：
1. LocaleContext:主要包含的是本地化信息，如zh-cn
2. ServletRequestAttributes:是一个spring的接口，通过它可以get/set/removeAttribute,根据scope参数判断操作request还是session。在本方法中具体使用的是ServletRequestAttributes类

###spring MVC 组件###
####HandlerMapping####
根据request找到相应的处理器Handler和Interceptors。
查找handler是按照顺序遍历所有的HandlerMapping，当找到一个HandlerMapping后立即停止查找并返回。
只有一个函数，作用是使用request返回HandlerExecutionChain
````
HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception;
````

####HandlerAdapter####
适配器。
````
// 判断是否可以使用某个handler
boolean supports(Object handler);
// 具体使用handler干活
ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;
// 获取资源的Last-Modified
long getLastModified(HttpServletRequest request, Object handler);
````
HandlerAdapter需要注册到Spring MVC的容器里，注册方法和HandlerMapping一样，只要配置一个bean就可以了。Handler是从HandlerMapping里返回的。

####HandlerExceptionResolver####
专门对异常进行处理。此组件的作用是根据异常设置ModelAndView，之后再交给render方法进行渲染。render只负责将ModelAndView渲染成页面，具体ModelAndView怎么来的render不关心。

如果需要统一异常处理，可以继承这个类，然后实现，这种方式可以进行全局的异常控制。
[使用方式](https://www.cnblogs.com/shanheyongmu/p/5872442.html)
####ViewResolver####
InternalResourceViewResolver:针对jsp类型的视屏
FreeMarkerViewResolver:针对FreeMarker
VelocityViewResolver:针对velocity
ResourceBundleViewResolver、XmlViewResolver、BeanNameViewResolver等解析器可以同时解析多种类型的视图。

####RequestToViewNameTranslator####
ViewResolver是根据ViewName查找View,但是有的handler处理完后没有设置view也没有设置viewName,这时就需要从request获取viewName。
RequestToViewNameTranslator就是做这件事的

####LocaleResolver####
解析仕途需要两个参数，一个是视图名，一个是Locale。
spring MVC主要两个地方用到了Locale：
1. ViewResolver解析视图的时候
2. 使用到国际化资源或者主题的时候。

####ThemeResolver####
解析主题用的

####MultipartResolver####
用于处理上传请求，处理方法是将普通的request包装成MultipartHttpServletRequest，后者可以直接调用getFile方法获取到File。

####FlashMapManager####
主要用在redirect中传递参数，FlashMapManager用来管理FlashMap


###HandlerMapping###
AbstractHandlerMapping中有三个list属性:
1. interceptors:用于适配Spring MVC的拦截器。有两种设置方式:1. 注册HandlerMapping是通过属性设置；2. 通过子类的extendInterceptors钩子方法进行设置。Interceptors并不会直接使用。
	而是通过initInterceptors方法按类型分配到mappedInterceptors和adaptedInterceptors中进行使用，Interceptors只用于配置。
2. mappedInterceptors:此类Interceptor在使用时需要与请求的url进行匹配，只有匹配成功后才会添加到getHandler的返回值HandlerExecutionChain里。
	他有两种获取途径：1. 从interceptors获取或注册到spring的容器中，通过detectMappedInterceptors方法获取
3. adaptedInterceptors:这种类型的Interceptor不需要进行匹配，在getHandler中会全部添加到返回值HandlerExecutionChain里面，他只能从interceptors中获取。

AbstractHandlerMapping的创建过程其实是初始化这三个Interceptor

子类作用：
1. AbstractUrlHandlerMapping:通过url来进行匹配的
	1. SimpleUrlHandlerMapping:直接将配置的内容注册到AbstractUrlHandlerMapping中
	2. AbstractDetectingUrlHandlerMapping:通过重写initApplicationContext。将容器中的所有bean都拿出来，按照一定规则注册到父类的map中
		1. ControllerClassNameHandlerMapping:用className作为url
		2. ControllerBeanNameHandlerMapping:用spring容器中的beanName作为url
	3. AbstractHandlerMethodMapping:将method作为handler来使用。这里面有三个map.1. handlerMethods:保存匹配条件和HandlerMethod的对应关系；2. urlMap:保存着url与匹配条件的对应关系;3. nameMap:保存着name与HandlerMethod的对应关系。
		1. RequestMappingInfoHandlerMapping:
		2. RequestMappingHandlerMapping:检查类前是否有@Controller或者@RequestMapping注解。

###HandlerAdapter###
HandlerAdapter封装了使用handler的方法

HttpRequestHandlerAdapter:适配HttpRequestHandler类型的handler
SimpleServletHandlerAdapter:适配Servlet类型的handler
SimpleControllerHandlerAdapter:适配Controller类型的handler


####RequestMappingHandlerAdapter####
作用是使用处理器处理请求。它使用的处理器是HandlerMethod类型，处理请求的过程分为三步：绑定参数、执行请求和处理返回值。
所绑定的参数的来源有6个地方：
1. request中相关的参数，主要包括url中的参数、post过来的参数以及请求头中的值
2. cookie中的参数
3. session中的参数
4. 设置到FlashMap中的参数(主要用于redirect的参数传递)
5. SessionAttributes传递的参数
6. 通过相应的注解了@ModelAndViewAttribute的方法进行设置的参数。

前面三个参数通过request管理，在request中获取，不需要做过多准备工作
第四类参数是直接在RequestMappingHandlerAdapter中进行处理，在请求之前将保存的设置到model，请求处理完成后如果需要将model中的值设置到FlashMap
后两类参数使用ModelFactory管理
后面三类通过Model管理。

准备好参数源后使用ServletInvocableHandlerMethod组件具体执行请求，其中包括使用ArgumentResolver解析参数、执行请求、使用ReturnValueHandler处理返回值等内容。
ServletInvocableHandlerMethod执行完请求处理后的工作是Model中参数的缓存和ModelAndView的创建。


RequestMappingHandlerAdapter的创建主要是初始化了下面几个属性：
1. argumentResolvers:用于给处理器方法和注释了@ModelAttribute的方法设置参数
2. initBinderArgumentResolvers:用于给注释了initBinder的方法设置参数
3. returnValueHandlers:用于将处理器的返回值处理成ModelAndView的类型
4. modelAttributeAdviceCache和initBinderAdviceCache:分别用于缓存@ControllerAdvice注释的类里面注释了@ModelAttribute和@InitBinder的方法，也就是全局的@ModelAttribute和@InitBinder方法。
	每个处理器自己的@ModelAttribute和@InitBinder方法是在第一次使用处理器处理请求时缓存起来的，这种做法不需要启动时就花时间遍历每个Controller查找@ModelAttribute和@InitBinder方法，
	又能在调用相同处理器处理请求时不需要再次查找从而从缓存中获取。
5. responseBodyAdvice:用于保存实现了ResponseBodyAdvice接口、可以修改返回的ResponseBody的类。

invokeHandleMethod函数就是对请求的具体执行过程。主要是执行ServletInvocableHandlerMethod

####ModelAndViewContainer####
ModelAndViewContainer承担着整个请求过程中数据的传递工作。它除了保存Model和view外还有一些别的功能。定义的属性：
1. view: 视图，Object类型的，可以是实际视图，也可以是String类型的逻辑视图
2. defaultModel:默认使用的Model
3. redirectModel:redirect类型的Model
4. ignoreDefaultModelOnRedirect:如果为true则在处理器返回redirect视图时一定不使用defaultModel
5. redrectModelScenario:处理器返回redirect视图的标志
6. requestHandled:请求是否已经处理完成的标志

返回defaultModel的情况：
1. 处理器返回的不是redirect视图
2. 处理器返回的是redirect视图，但是redirectModel为null，而且ignoreDefaultModelOnRedirect为true

返回redirectModel的情况：
1. 处理器返回redirect视图，并且redirectModel不为null
2. 处理器返回redirect视图，并且ignoreDefaultModelOnRedirect为true

####ModelFactory####
用来维护Model的，包含两个功能：1. 初始化Model；2. 处理器执行后将Model中相应的参数更新到SessionAttributes中

Model中参数的优先级：
1. FlashMap中保存的参数优先级坐高，他在ModelFactory前面执行；
2. SessionAttributes的方法设置的参数优先级第二，它不可以覆盖FlashMap中设置的参数；
3. 通过注解了@ModelAttribute的方法设置的参数优先级第三；、
4. 注解了@ModelAttribute而且从别的处理器的SessionAttributes中获取的参数优先级最低


####ServletInvocableHandlerMethod####
继承自InvocableHandlerMethod。InvocableHandlerMethod可以直接调用内部属性method对应的方法
ServletInvocableHandlerMethod继承自InvocableHandlerMethod，在父类基础上增加了三个功能
1. 对@ResponseStatus注解的支持
2. 对返回值的处理
3. 对异步处理结果的处理

主要是请求参数的解析

请求解析器：
- AbstractMessageConverterMethodArgumentResolver:使用HttpMessageConverter解析request body类型参数的基类
- AbstractMessageConvertMethodProcessor:定义相关工具，不直接解析参数
- HttpEntityMethodProcessor:解析HttpEntity和RequestEntity类型的参数
- RequestResponseBodyMethodProcessor:解析注解@RequestBody类型的参数
- RequestPartMethodArgumentResolver:解析注解了@RequstPart,MultipartFile类型以及javax.servlet.http.Part类型的参数
- AbstractNamedValueMethodArgumentResolver:解析namedValue类型的参数(有name的参数，如cookie,requestParam,requestHeader,pathVariavle等)的基类，主要功能有：
	1. 获取name;
	2. resolveDefaultValue,handleMissingValue,handleNullValue;
	3. 调用模板方法resolveName,handleResolvedValue具体解析
- AbstractCookieVauleMethodArgumentResolver:解析注解了CookieValue的参数的基类
- ServletCookieValueMethodArgumentResolver:实现resolveName方法，具体解析cookieValue
- ExpressionValueMethodArgumentResolver:解析注解@Value表达式的参数，主要设置了beanFactory，并用他完成具体解析，解析过程在父类完成
- MatrixVariableMethodArgumentResolver:解析注解@MartrixVariable而且不是Map类型的参数(Map类型使用MatrixVariableMapMethodArgumentResolver解析)
- PathVariableMethodArgumentResolver:解析注解@PathVariable而且不是Map类型的参数(Map类型使用PathVariableMapMethodArgumentResolver)
- RequestHeaderMethodArgumentResolver:解析注解@RequestHeader而且不是Map类型的参数(Map类型用RequestHeaderMapMethodArgumentResolver)
- RequestParamMethodArgumentResolver:解析注解@RequstParam的参数、MultipartFile类型的参数和没有注解的通用类型(int,long)的参数。如果注解了@RequestParam的map类型的参数，则注解必须有name值(否则使用RequestParamMapMethodArgumentResolver解析)
- AbstractWebArgumentResolverAdapter:用作WebArgumentResolver解析器的适配器
- ServletWebArgumentResolverAdapte:给父类提供request
- ErrorsMethodArgumentResolver:解析Errors类型的参数（一般是Errors或BindingResult）,当一个参数绑定出现异常时会自动将异常设置到其向领导 下一个Errors类型的参数，设置方法就是使用这个解析器，内部是直接从model中获取的
- HandlerMethodArgumentResolverComposite:argumentResolver的容器，可以封装多个Resolver,具体解析由封装的Resolver完成，主要为了方便调用
- MapMethodProcessor:解析Map类型参数(包括ModelMap类型)，直接返回container中的Model
- MatrixVariableMapMethodArgumentResolver:解析注解@MatrixVariable的Map类型参数
- ModelAttributeMethodProcessor:解析注解@ModelAttribute的参数，如果其中的annotationNotRequired属性为true还可以解析没有注解的非通用类型的参数(RequestParamMethodArgumentResolver解析没有注解的通用类型的参数)
- ServletModelAttributeMethodProcessor:对父类添加了Servlet特性，使用ServletRequestDataBinder代替父类的WebDataBinder进行参数的绑定
- ModelMethodProcessor:解析Model类型参数，直接返回container中的Model
- PathVariableMapMethodArgumentResolver:解析注解了@PathVariable的map类型参数
- RedirectAttributesMethodArgumentResolver:解析RedirectAttributes类型的参数，新建RedirectAttributesModelMap类型的RedirectAttributes并设置到container中，然后返回给我们的参数
- RequestHeaderMapMethodArgumentResolver:解析注解了@RequestHeader的map类型的参数
- RequestParamMapMethodArgumentResolver:解析注解了@RequestParam的map类型的参数
- ServletResponseMethodArgumentResolver:解析WebRequest.ServletRequest.MultipartRequest.HttpSession.Principal.Locale.TimeZone.InputStream.Reader.HttpMethod类型和"java.time.ZoneId"类型的参数，他们都是使用request获取的
- SessionStatusMethodArgumentResolver:解析SessionStatus类型参数，直接返回container中的SessionStatus
- UriComponentsBuilderMethodArgumentResolver:解析UriComponentsBuilder类型参数

####HandlerMethodReturnValueHandler####
用在ServletInvocableHandlerMethod中，作用是处理处理器执行后的返回值，主要有三个功能：
1. 将相应参数添加到Model
2. 设置view
3. 如果请求已经处理完，则设置ModelAndViewContainer的requestHandled为true

主要是返回参数的解析

返回值处理器：
- AbstractMessageConverterMethodProcessor:处理返回值需要使用HttpMessageConverter写入response的基类，自己并未具体做处理，而是定义了相关工具
- HttpEntityMethodProcessor:处理HttpEntity类型，并且不是RequestEntity类型的返回值
- RequestResponseBodyMethodProcessor:处理当返回值或者处理请求的Handler类注解了@ResponseBody情况下的返回值
- AsyncTaskMethodReturnValueHandler:处理WebAsyncTask类型的返回值，用于异步请求，使用WebAsyncManager完成
- CallableMethodReturnValueHandler:处理Callable类型的返回值，用于异步请求，使用WebAsyncManager完成
- DeferredResultMethodReturnValueHandler:处理DeferredResult类型的返回值，用于异步请求，使用WebAsyncManager完成
- HandlerMethodReturnValueHandlerComposite:用于封装其他处理器，方便调用
- HttpHeadersReturnValueHandler:处理HttpHeaders类型的返回值，将HttpHeaders的返回值添加到response的Headers并设置contaicner的requestHandled为true
- ListenableFutureReturnValueHandler:处理ListenableFutrue类型的返回值，用于异步请求，使用WebAsyncManager完成
- MapMethodProcessor:处理Map类型的返回值，将Map添加到container的Model中
- ModelAndViewMethodReturnValueHandler:处理ModelAndView类型的返回值，将返回值中的view和model设置到container中
- ModelAndViewResolverMethodReturnValueHandler:可以处理所有返回值，一般设置在最后一个，当别的处理器都不能处理时使用他处理
- ModelAttributeMethodProcessor:处理主机@ModelAttribute类型的返回值，如果annotationNotRequired为true,还可以处理没有注解的非通用类型的返回值
- ServletModelAttributeMethodProcessor:对返回值的处理同父类，这里只是修改了参数的解析功能，未对返回值处理功能做修改
- ModelMethodProcessor:处理Model类型的返回值，将Model中的值添加到container的model中
- ViewMethodReturnValueHandler:处理view类型返回值，如果返回值为空，直接返回，否则将返回值设置到container的view中，并判断返回值是不是redirect类型，如果是，则设置container的redirectModelScenario为true
- ViewNameMethodReturnValueHandler:处理void和String类型返回值，如果返回值为空，则直接返回，否则将返回通过container的setViewName方法设置到其view中，并判断返回值是不是redirect类型，如果是,则设置container的redirectModelScenario为true

###VieResolver###
主要作用是根据视图名和Locale解析出视图。

整体可以分为四大类
- BeanNameViewResolver:使用beanName从spring MVC容器中查找
- ViewResolerComposite:封装着多个VeiwResolver的容器。
- ContentNegotiatingViewResolver:在别的解析器解析的结果上增加了对MediaType和后缀的支持。对视图的解析不是自己完成的，而是使用封装的VieweResolver进行的
- AbstractCachingViewResolver:提供统一的缓存功能，当视图解析过一次就被缓存起来，知道缓存被删除前，视图的解析都会自动从缓存中获取。有三个继承类
	- ResourceBundleViewResolver：通过使用properties属性配置文件解析视图
	- XmlViewResolver：和上面类似，使用xml配置文件
	- UrlBasedViewResolver：所有直接将逻辑视图座位url查找模板文件的ViewResovler的基类。其子类主要做了三件事：
		- 通过重写requiredViewClass方法修改了必须符合的视图类型的值
		- 使用setViewClass方法设置了所用的视图类型
		- 给创建出来的视图设置一些属性。

###RequestToViewNameTranslator###
可以在处理器返回的view为空时，使用他根据request获取viewName

###HandlerExceptionResolver###
用于解析请求处理过程中产生的异常

- AbstractHandlerMethodExceptionResolver：和其子类ExceptionHandlerResolver一起完成@ExceptionHandler注解的方法进行异常解析的功能
- DefaultHandlerExceptionResolver:按不同类型分别对异常进行解析
- ResposeStatusExceptionResolver:解析有@ResponseStatus注解类型的异常
- SimpleMappingExceptionResolver:通过配置的异常类和view的对应关系来解析异常。需要提前配置异常类和view的对应关系后才能使用

异常解析过程主要包含两个部分：给ModelAndView设置相应内容，设置response的相关属性。还有一一些辅助功能，如记录日志等。

AbstractHandlerMethodExceptionResolver的作用其实相当于一个适配器

###MultipartResolver###
作用是将上传请求包装成可以直接获取File的Request，方便操作。重点是从Requst中解析出上传的文件，并设置到相应上传类型的Request中。
用于处理上传请求，有两个实现类：
StandardServletMultipartResolver：使用了Servlet3.0标准的上传方式
CommonsMultipartResolver：使用Apache的commons-fileupload

####StandardServletMultipartResolver####
在Servlet3.0中，只需要调用request的getParts方法就可以获取所有上传的文件。如果想获取单独获取某个文件，可以使用request.getPart(fileName)。
获取到part后直接调用它到write(saveFileName)方法就可以将文件保存为saveFileName为文件名的文件，也可以调用getInputStream获取InputStream.
如果想使用这个方式还需要在配置上传文件的Servlet时添加multipart-config属性

multipart-config4个子属性可以配置：
location:设置上传文件存放的根目录
max-file-size:设置单个上传文件的最大值，默认为-1，表示无限制
max-request-size:设置不写入硬盘的最大数据量，默认为0，表示所有上传的文件都会作为一个临时文件写入硬盘

###LocaleResolver###
作用是使用request解析出Locale

类作用：
- AcceptHeaderLocaleResoler：直接使用了Header里的acceptlanguage值，不可以在程序中修改
- FixedLocaleResolver:用于解析出固定的Locale，也就是说在创建时就设置好确定的Locale，之后无法修改
- SessionLocaleResolver：用于将Locale保存到Session中，可以修改返回的ResponseBody的类。
- CookieLocaleResolver：用于将Locale保存到Cookie中，可以修改

###ThemeResolver###

类作用：
FixedThemeResolver:用于解析固定的主题名，主题名在创建时设置，不能修改。
SessionThemeResolver:将主题保存到Session中，可以修改
CookieThemeResolver:将主题保存到Cookie中。

###FlashMapManager###
用来管理FlashMap,FlashMap用于在redirect时传递参数。实现时采用模板模式定义了整体流程，具体实现类SessionFlashMapMananger通过模板方法提供了具体操作FlashMap的功能

**说明**
1. 实际在session中保存的FlashMap是List<FlashMap>类型，即一个session可以保存多个FlashMap。一个FlashMap保存着一套Redirect转发所传递的参数
2. FlashMap继承自HashMap，除了HashMap的功能和设置有效期，还可以保存Redirect后的目标路径和通过url传递的参数，这两项内容主要用来从session保存的多个FlashMap中查找当前请求的FlashMap

##异步请求##
serlvet的异步请求的支持其实采用的是长连接的方式。在原始请求的请求返回时并没有关闭连接，关闭的只是处理请求的那个线程，只有在异步请求全部完成之后才会关闭连接。
使用方式只需要在请求处理过程中调用request的startAsync方法即可，其返回值是AsyncContext类型

AsyncContext可以成为异步请求上下文，也可以称为异步请求容器。他的作用是保存与异步请求相关的所有信息，类似于servlet中的ServletContext。
异步请求主要使用AsyncContext进行操作。他是在请求处理的过程中调用Request的startAsync方法返回的。

````
package javax.servlet;
public interface AsyncContext {
    static final String ASYNC_REQUEST_URI = "javax.servlet.async.request_uri";
    static final String ASYNC_CONTEXT_PATH = "javax.servlet.async.context_path";
    static final String ASYNC_PATH_INFO = "javax.servlet.async.path_info";
    static final String ASYNC_SERVLET_PATH = "javax.servlet.async.servlet_path";
    static final String ASYNC_QUERY_STRING = "javax.servlet.async.query_string";
    public ServletRequest getRequest();
	/** 获取response，就可以对response进行操作 */
    public ServletResponse getResponse();
    public boolean hasOriginalRequestAndResponse();
    /** 将请求发送到一个新地址，发送到request原来的地址(如果有forward则使用forward后的最后一个地址) */
    public void dispatch();
    /** 将请求发送到一个新地址，直接将path作为地址 */
    public void dispatch(String path);
    /** 将请求发送到一个新地址，发送给别的应用指定的地址 */
    public void dispatch(ServletContext context, String path);
    /** 通知容器已经处理完 */
    public void complete();
    /** 启动实际处理线程，也可以自己创建线程在其中使用AsyncContext保存的信息(如response)进行处理 */
    public void start(Runnable run);
    /** 用于添加监听器 */
    public void addListener(AsyncListener listener);
    /** 用于添加监听器 */
    public void addListener(AsyncListener listener, ServletRequest servletRequest, ServletResponse servletResponse);
    public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException; 
    /** 用于修改超时时间 */
    public void setTimeout(long timeout);
    public long getTimeout();
}
````

###一个实例###
![](/picture/spring-async-example.jpeg)

###spring mvc的异步请求###
spring MVC提供了AsyncWebRequest类型的request，处理异步请求的管理器WebAsyncManager和工具WebAsyncUtils

spring MVC将异步请求细分成Callable、WebAsyncTask、DeferredResult和ListenableFuture四种
前两种的核心是Callable
ListenableFuture是在java的基础上增加了设置回调方法的功能，主要用于需要在处理器中调用别的资源(如别的url)的情况。
Spring MVC专门提供了AsyncRestTemplate方法调用别的资源，并返回ListenableFuture类型

````
package org.springframework.web.context.request.async;

public interface AsyncWebRequest extends NativeWebRequest {
	/** 添加请求超时 */
	void setTimeout(@Nullable Long timeout);
	/** 添加请求完成的处理器 */
	void addTimeoutHandler(Runnable runnable);
	/** */
	void addErrorHandler(Consumer<Throwable> exceptionHandler);
	/** */
	void addCompletionHandler(Runnable runnable);
	/** */
	void startAsync();
	/** 判断是否启动了异步处理 */
	boolean isAsyncStarted();
	/** 将请求发送到一个新地址，发送到request原来的地址(如果有forward则使用forward后的最后一个地址) */
	void dispatch();
	/** 判断异步处理是否已经处理完 */
	boolean isAsyncComplete();
}
````

####WebAsyncManager####
WebAsyncManager是spring mvc处理异步请求过程中最核心的类，他管理着整个异步处理的过程。
重要属性:
- timeoutCallableInterceptor:CallableProcessingInterceptor类型，专门用于Callable和WebAsyncTask类型超时的拦截器
- timeoutDeferredResultInterceptor：DeferredResultProcessingInterceptor类型，专门用于DeferredResult和ListenableFuture类型超时的拦截器
- callableInterceptors:map类型，用于所有Callable和WebAsyncTask类型的拦截器
- deferredResultInterceptors：map类型，用于所有DeferredResult和ListenableFuture类型的拦截器
- asyncWebResult:为了支持异步处理而封装的request
- taskExcutor:用于执行Callable和WebAsyncTask类型处理，如果WebAsyncTask中没有定义exuctor,则使用WebAsyncManager中的taskExecutor

##定义解释##
BeanFactory和FacotryBean的解释：
BeanFactory它的职责包括：实例化、定位、配置应用程序中的对象及建立这些对象间的依赖。
BeanFactory是访问Spring beans的一个容器。所有的Spring beans的定义都会在这里被统一的处理。换句话说，BeanFactory interface是一个应用组件（Spring bean）的集中注册器和配置器。从一般意义上来讲，BeanFactory是用来加载和管理Spring bean definition的。
从BeanFactory的定义可以看出，BeanFactory除了获取bean的功能外，还有bean的Type，bean的是否singleton的等特性，此外，前面分析已可以知道，getBean（）可以返回singleton或prototype类型的实例。正是为了统筹管理这些bean创建相关的各种特性，才诞生了FactoryBean类。FactoryBean类主要是bean创建方面的一个统筹的管理。这是BeanFactory和FactoryBean的关系。
FactoryBean(通常情况下，bean无须自己实现工厂模式，Spring容器担任工厂角色；但少数情况下，容器中的bean本身就是工厂，其作用是产生其它bean实例),作用是产生其他bean实例。通常情况下，这种bean没有什么特别的要求，仅需要提供一个工厂方法，该方法用来返回其他bean实例。由工厂bean产生的其他bean实例，不再由Spring容器产生，因此与普通 bean的配置不同，不再需要提供class元素。

##Spring MVC 总结##
spring mvc本质是一个servlet，servlet的运行需要一个servlet容器，比如tomcat。servlet容器帮我们统一做了像底层socket链接那种的工作。是需要按照servlet的接口去做就可以了。
而spring MVC在此基础上提供了一套通用的解决方方案，只需要专注于业务逻辑。

spring mvc提供了三个层次的servlet:HttpServletBean.FrameworkServlet和DispatcherServlet,他们互相继承。HttpServletBean直接继承自java的HttpServlet.
HttpServletBean用于将servlet中配置的参数设置到相应的属性中。
FrameworkServlet初始化了spring mvc中所使用的WebApplicationContext，具体处理请求的9大组件是在DispatcherServlet中初始化的。
继承结构图：
![](/picture/spring-mvc-structure.png)

spring MVC中请求处理过程主要在DispatcherServlet中，不过他上一层的FrameworkServlet也做了一些工作，
首先他讲所有类型的请求都转发到processRequest方法，然后在processRequestfang方法中做了三件事
1. 调用doService模板方法具体处理请求，doService方法在DispatcherServlet中实现
2. 将当前请求的LocaleContext和ServletRequestAttributes在处理请求钱设置到了LocaleContextHolder，并在请求处理完成后恢复
3. 请求处理完后发布一个ServletRequestHandledEnvent类型的消息

DispatcherServlet在doService方法中将webApplicationContext、localeResoler、themeResolver、themeSource、flashMap和flashMapManager设置到request的属性中方便使用，然后将请求交给doDispatch方法进行具体处理

DispatcherServlet的doDispatch方法执行过程大致可以分成4步：
1. 根据request找到handler,
2. 根据handler找到HandlerAdapter
3. 用handlerAdapter调用handler处理请求
4. 用processDispatchResult方法处理handler处理之后的结果(主要处理异常和找到view并渲染输出给用户)

handler是MVC中的C层，在V层干活的工具是view，chazhoaview使用的是ViewResolver和RequestToViewNameTranslator。在M层也就是Model层，这层的工作比较多
注释了@ModelAttribute的方法.SessionAttribute.FlashMap.Model以及需要执行的方法的参数和返回值等都属于这一层。HandlerMethodArgumentResolver和HandlerMethodReturnValueHandler、ModelFactory和FlashMapManager是这一层中使用的人
HandlerMethodArgumentResolver和HandlerMethodReturnValueHandler同时还担任着"查找工具"的角色

一个请求流程：
![](/picture/a-request-1.jpeg)
![](/picture/a-request-2.jpeg)
![](/picture/a-request-3.jpeg)
![](/picture/a-request-4.jpeg)
![](/picture/a-request-5.jpeg)
![](/picture/a-request-6.jpeg)

##tomcat##
tomcat分为两部分，连接器和容器
连接器专门用于处理网络链接相关的事情，如socket连接、request封装、连接线程池维护等工作
容器用来存放我们编写的网站程序，如tomcat中一共有四层容器：Engine.Host.Context.wrapper。一个wrapper对应一个servlet，一个context对应一个应用，一个host对应一个站点，engine是引擎，一个容器只有一个。

context和host的区别是host代表站点，如不同的域名，而context表示一个应用。
默认情况下，webapps/ROOT中存放的为主应用，对应一个站点的根路径,如www.root.com.
webapps下别的目录则存放别的子应用，对应站点的子路径，如webapps/test目录存放www.test.com/test应用
他们每个都有一个对应的容器context，如果想添加一个新的站点，如blog.test.com,则需要是用host，一套容器和多个连接器组成一个server，一个tomcat中可以有多个server

java提供了两个servlet实现类：
- GenericServlet.
	1. 实现了ServletConfig接口，可以直接调用ServletConfig中的方法
	2. 提供了无参的init方法，
	3. 提供了log方法。
- HttpServlet
	1. 将ServletRequest和ServletResponse转换为了HttpServletRequest和HttpServletResponse
	2. 根据http请求类型(如get、post等)将请求路由到了7个不同的处理方法，这样在编写代码时只需要将不同类型的处理代码编写到不同的方法中就可以了，如常见的doGet.doPost、doPut、doDelete、doOptions、doTrace方法（除了doHead的所有处理请求的方法）。






















