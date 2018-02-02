# spring bean的测试 #
引入spring-core和spring-bean两个包，加入junit测试工具
xml文件
````
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xsi:schemaLocation="http://www.springframework.org/schema/beans 
        http://www.springframework.org/schema/beans/spring-beans-3.0.xsd ">
        <bean id="myTestBean" class="com.spring.bean.Bean01" />
</beans>
````
bean类
````
package com.spring.bean;

public class Bean01 {

	private String str = "this is a test bean";

	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}
	
}
````
测试类
````
package test.spring.bean;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import com.spring.bean.Bean01;

@SuppressWarnings("deprecation")
public class BeanTest01 {
	
	@Test
	public void testBean(){
		BeanFactory bf = new XmlBeanFactory(new ClassPathResource("beanFactoryTest.xml"));
		Bean01 bean = (Bean01) bf.getBean("myTestBean");
		System.out.println(bean.getStr());
	}
}
````
输出结果为：
````
this is a test bean
````
## 功能分析 ##
1. 读取配置文件：beanFactoryTest.xml
2. 根据beanFactoryTest.xml中的配置找到对应的类的配置，并实例化
3. 调用实例化后的实例

## xml解析过程 ##
1. 封装资源文件
2. 获取输入流
3. 构造inputSource实例和Resource实例
4. 获取xml文件的验证模式
5. 加载xml文件，并得到相应document
6. 返回document注册的bean信息

## 默认标签的解析 ##
### bean标签 ###
1. 首先使用BeanDefinitionDelegate类的parseBeanDefinitionElement方法进行元素解析，并返回BeanDefinitionDelegate的实例bdHolder。此实例含有配置文件中的属性，例如：class,name,id,alias之类的属性
2. 当返回的bdHolder不为空的情况下，若存在默认标签的子节点下再有自定义属性，还需要对自定义标签进行解析
3. 解析完成后，对解析后的bdHolder进行注册，注册操作委托给了BeanDefinitionReaderUtils的registerBeanDefinition方法
4. 最后发出相应事件，通知想关的监听器，这个bean已经加载完成


#### 解析BeanDefinition ####
1. 提取元素中的id和name属性
2. 进一步解析其他所有属性并同意封装至GenericBeanDefinition类型的实例中
3. 如果检测到bean没有指定beanName，那么使用默认规则为此bean生成beanName，
4. 将获取到的信息封装到BeanDefinitionHolder的实例中

#### 复杂标签的解析 ####
##### 创建用于属性承载的BeanDefinition #####
##### 解析各种属性 #####
（scope.singleton.abstract.lazy-init.autowire.dependecy-check.depends-on.autowire-candidate.primary.init-method.destroy-method.factory-method.factory-bean）
##### 解析子元素meta #####
##### 解析子元素lookup-method（lookup-method子元素是动态的将bean作为返回值） #####
父类
````
package com.spring.bean;

public class User {
	public void showMe(){
		System.out.println("i am user");
	}
}
````
子类
````
package com.spring.bean;

public class Teacher extends User{
	public void showMe(){
		System.out.println("i am teacher");
	}
}
````
调用方法
````
package test.spring.bean;

import com.spring.bean.User;

public abstract class GetBeanTest {
	public void showMe(){
		this.getBean().showMe();
	}
	public abstract User getBean();
}
````
测试方法
````
	@Test
	public void testLookUp(){
		ApplicationContext context = new ClassPathXmlApplicationContext("lookupTest.xml");
		GetBeanTest test = (GetBeanTest) context.getBean("getBeanTest");
		test.showMe();
	}
````
xml文件
````
    <bean id="getBeanTest" class="test.spring.bean.GetBeanTest">
    	<lookup-method name="getBean" bean="teacher"></lookup-method>
    </bean>
    <bean id="teacher" class="com.spring.bean.Teacher" />
````
运行结果
````
i am teacher
````
当修改lookup-method之后
重新创建的类
````
package com.spring.bean;

public class Student extends User{
	public void showMe(){
		System.out.println("i am student");
	}
}
````
修改xml
````
    <bean id="getBeanTest" class="test.spring.bean.GetBeanTest">
    	<lookup-method name="getBean" bean="student"></lookup-method>
    </bean>
    <bean id="teacher" class="com.spring.bean.Teacher" />
    <bean id="student" class="com.spring.bean.Student" />
````
运行结果：
````
i am student
````
##### constructor-arg #####
constructor-arg：通过构造函数注入。 

#### 注册解析的BeanDefinition ####
##### 通过beanName注册beanDefinition #####
将beadDefinition直接放入map中，使用beanName做为key
在bean的注册处理方式上，主要进行了几个步骤：
1. 对AbstractBeanDefinition的校验，此时的校验是针对于AbstractBeanDdfinition的methodOverrides属性。
2. 对beanName已经注册的情况的处理。如果设置了不允许bean的覆盖，则需要抛出异常，否则直接覆盖。
3. 加入map缓存
4. 清除解析之前留下的对应beanName的缓存

##### 通过别名注册BeanDefinition #####
alias的步骤如下
1. alias与beanName相同情况处理。若alias与beanName并名称相同则不需要处理并删除掉原有alias。
2. alias覆盖处理。若aliasName已经使用并已经指向另一beanName则需要用户的设置处理
3. alias循环检查。当A->B存在时，若再次出现A->C->B时候则会抛出异常。
4. 注册alias

### import标签 ###
分模块的方法，构造spring配置文件
applicationContext.xml文件
````
<beans>
	<import resource='customerContext.xml'>
	<import resource='systemContext.xml'>
</beans>
````

### alias标签 ###
有时，当我们期望能在当前位置为那些在别处定义的bean引入别名。在xml配置文件中，可用单独的<alias/>元素来完成bean别名的定义。如配置文件中定义了一个javabean
````
<bean id="testBean" calss="com.test">
````
要给这个javabean增加别名，以便不同的对象来调用，我们可以直接使用bean标签中的name属性
````
<bean id="testBean" name="testBean,testBean2" calss="com.test">
````
同时，spring还有另外一种声明方式
````
<bean id="testBean" class="com.test">
<alias name="testBean" alias="testBean,testBean2"/>
````
### beans标签 ###


## 自定义标签的解析 ##
1. 创建一个需要扩展的组件
2. 定义一个xsd文件描述组件内容
3. 创建一个文件，实现beanDefinitionParser接口，用来解析xsd文件中的定义和组件定义
4. 创建一个handler文件，扩展自NamespaceHandlerSupport，目的是将组件注册到spring容器
5. 编写springHandler和spring.schemas文件

# bean的加载 #
bean加载步骤
1. 转换对应beanName
2. 尝试从缓存中加载单列
3. bean的实例化
4. 原型模式的依赖检查
5. 检测parentBeanFactory
6. 将存储xml配置文件的gernericBeanDefinition转换为rootBeanDefinition
7. 寻找依赖
8. 针对不同scope进行bean的创建
9. 类型转换

解释：
1. beanName有可能是别名，也有可能是factoryBean，所以要进行一系列的解析，包括：
	- 去除factoryBean的修饰符
	- 取指定alias所表示的最终beanName
2. 尝试从缓冲中加载，如果加载不成功，则再次尝试从singletonFactories中加载，
3. 缓存中记录的是最原始的bean状态，
4. 只有在单例情况下才会尝试解决循环依赖。
5. 如果缓存没有数据的话，直接转到父类工厂上去加载。然后再去递归调用getbean方法
6. 从xml配置文件中读取到的bean信息是存储在gernericBeanDefinition中的，但是所有的bean后续处理都是针对于rootBeanDefinition，所以这里要进行一个转换，转换的同时，如果父类bean不为空的话，则会一并合并父类的属性
7. 因为bean的初始化过程可能会用到某些属性，而某些属性可能是动态配置的，并且依赖于其他的bean，这时候，就需要先加载依赖的bean，所以在spring的加载顺序中，在初始化某一个bean的时候，首先会初始化这个bean所对应的依赖。
8. spring中存在不同的scope，在此步骤中，spring会根据不同的配置进行不同的初始化策略

### factoryBean的使用 ###
一般情况下，spring通过反射机制利用bean的class属性指定实现类来实例化bean。在某些情况下，实例化bean的过程比较复杂，如果按照传统的方式，则需要在<bean>中提供大量的配置信息，，配置方式的灵活性是受限的，这时采用编码的方式可能会得到一个简单的方案。spring为此提供了一个org.Springframework.bean.factory.FactoryBean的工厂类接口，用户可以通过实现该接口定制实例化bean的逻辑。
````
public interface FactoryBean<T>{
	T getObject() throws Exception;
	Class<?> getObjectType();
	boolean isSingleton();
}
````
T getObject():返回由FactoryBean创建的实例，如果isSingleton()返回true，则该实例会放到spring容器中单实例池。
boolean isSingleton()：返回由FactoryBean创建的bean实例的作用域是singleton还是property。
Class<?> getObjectType()：返回FactoryBean创建的bean类型。
当配置文件中<bean>的class属性配置的实现类是FactoryBean时，通过getBean()方法返回的不是FactoryBean本身，而是FactoryBean#getObject方法所返回的对象

### 缓存中获取单例bean ###
这个方法首先尝试从singletonObjects里面获取实例，如果获取不到，再从earlySingletonObject里面获取，如果还获取不到，再尝试从singletonFactoryies里面获取beanName，并放到earlySingletonObject里面去，并且从singletonFactories里面remove掉这个ObjectFactory，而对于后续的所有内存操作都只为了循环依赖检测的时候使用，也就是在allowEarlyReference为true的情况下才会使用

###### map解释 ######
- singletonObjects:用于保存BeanName和创建bean实例之间的关系，bean name->bean instance
- singletonFactories:用于保存BeanName和创建bean的工厂之间的关系，bean name->ObjectFactories
- earlySingletonObjects:也是保存BeanName和创建bean实例之间的关系，与singletonObjects的不同之处在于，当一个单例bean被放到这里面后，那么当bean还在创建过程中，就可以通过getBean方法获取到了，其目的是用来检测循环引用。
- registeredSingletons：用来保存当前所有已注册的bean

### 从bean的实例中获取对象 ###
getObjectForInstance方法的工作
1. 对FactoryBean正确性的验证。
2. 对非FactoryBean不做任何处理。
3. 对bean进行转换
4. 将从Factory中解析bean的工作委托给getObjectFromFactoryBean

### 获取单例 ###
如果缓存中不存在已经加载的单例bean，就要从头开始bean的加载，spring中使用getSingleton的重载方法实现bean的加载过程
getSingleton函数可以在单例创建的前后做一些准备及操作，而真正获取单例bean的方法其实并不是在此方法中实现的，其实现逻辑实在ObjectFactory类型的实例singletonFactory中实现的。其准备及操作包括如下内容：
1. 检查缓存是否已经加载过
2. 若没有加载，则记录beanName的正在加载状态
3. 加载单例前加载状态
4. 通过调用参数传入的ObjectFactory的个体Object方法实例化bean
5. 加载单例后的处理方法调用
6. 将结果记录到缓存并删除加载bean过程中记录的各种辅助状态
7. 返回处理结果

ObjectFactory的核心部分其实只是调用了createBean的方法

### 准备创建bean ###
createBean函数完成具体步骤和功能
1. 根据设置的class属性或者根据className来解析Class
2. 对override属性进行标记及验证
3. 应用初始化前的后处理器，解析指定bean是否存在初始化前的短路操作
4. 创建bean

#### 处理override属性 ####
本过程处理的是lookup-method和replace-method两个配置的功能

#### 实例化的前置处理 ####
在真正调用doCreate方法创建bean的实例钱，使用了这样一个方法，resolveBeforeInstantiation(beanName,mbd)对BeanDefinigiton中的属性做些前置处理。AOP功能就是在这里判断的
两个方法：applyBeanPostProcessorsBeforeInstantiation以及applyBeanPostProcessorsAfterInitialization。两个方法的实现非常简单，是对后处理器中的所有InstantiationAwareBeanPostProcessor类型的后处理器进行postProcessBeforeInstantiation方法和BeanPostProcessor的postProcessAfterInitialization方法的调用

##### 实例化前的后处理器应用 #####
bean的实例化前调用，也就是讲AbstractBeanDefinition转换为BeanWrapper前的处理，给子类一个修BEANDefinition的机会

##### 实例化后的后处理器应用 #####
spring中的规则是在bean的初始化后尽可能保证将注册的后处理器的postProcessAfterInitialization方法应用到该bean中，因为如果返回的bean不为空，那么便不会再次经历bean的创建过程，所以只能在这里应用后处理器的postProcessAfterInitialization方法

#### 循环依赖 ####
##### spring解决循环依赖 #####
1.构造器循环依赖
通过构造器注入构成的循环依赖，此依赖是无法解决的，只能抛出BeanCurrentlyInCreationException异常表示循环依赖。

2.setter依赖
对于setter注入造成的依赖是通过spring容器提前暴露刚完成构造器注入但未完成其他步骤（如setter注入）的bean来完成的，而且只能解决单例作用域的bean循环依赖。通过提前暴露一个单例工厂方法，从而使其他bean能引用到该bean，
具体步骤：
1. spring容器创建单例“testA”bean，首先根据无参数构造器创建bean，并暴露一个“ObjectFactory”用于返回一个提前暴露一个创建中的bean，并将“testA”标识符放到“当前创建bean池”中，然后进行setter注入“testB”
2. spring容器创建单例“testB”bean，首先根据无参数构造器创建bean，并暴露一个“ObjectFactory”用于返回一个提前暴露一个创建中的bean，并将“testB”标识符放到“当前创建bean池”中，然后进行setter注入“circle”
3. spring容器创建单例“testC”bean，首先根据无参数构造器创建bean，并暴露一个“ObjectFactory”用于返回一个提前暴露一个创建中的bean，并将“testB”标识符放到“当前创建bean池”中，然后进行setter注入“testA”。进行注入“testA”时由于提前暴露了“ObjectFactory”工厂，从而使用它返回提前暴露一个创建中的bean。
4. 最后在依赖注入“testB”和“testA”，完成setter注入。

3.prototype范围的依赖处理
对于“prototype”作用域bean，spring容器无法完成依赖注入，因为spring容器不进行缓存prototype作用域的bean，因此无法提前暴露一个创建中的bean

#### 创建bean ####
````
````
````
````
````
````
````
````
````
````
````
````
````
````
````
````
````
````````
````
````
````
````
````
````
````
````
````
````
````
````
````
````
````
````
````
````
````
````
````
````
````````
````
````
````
````
````
````
````
````
````
````
````
````
````
````
````
````
````
````
````
````
````
````
````