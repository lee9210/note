#### 简述 ####
在传统java中，程序中通过直接new一个对象的过程被称为正转。在spring中，通过容器的方法，来逆转这个过程，被称为控制反转。通过容器来控制对象的生成注入，对象只是被动的接收依赖对象。这个就是ioc。
#### ioc容器的概念 ####
ioc容器就是依赖注入的功能的容器，ioc容器负责实例化，定位，配置应用程序中的对象，以及建立这些对象之间的依赖

测试：
````
package com.shuke.chapter.impl;

import com.shuke.chapter.HelloApi;

public class HelloImpl implements HelloApi{
    @Override
    public void sayHello() {
        System.out.println("hello world");
    }
}
````
````
package com.shuke.chapter;

public interface HelloApi {
    void sayHello();
}
````
xml文件配置
````
<?xml version="1.0" encoding="UTF-8"?>
<beans
        xmlns="http://www.springframework.org/schema/beans"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:context="http://www.springframework.org/schema/context"
        xsi:schemaLocation="
http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd">
    <!-- id 表示你这个组件的名字，class表示组件类 -->
    <bean id="hello" class="com.shuke.chapter.impl.HelloImpl"></bean>
</beans>
````
单元测试：
````
package test.shuke.chapter;

import com.shuke.chapter.HelloApi;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class HelloTest {
    //@Test
    public void test(){
        System.out.println("this is a test file");
    }
    @Test
    public void testApi(){
        //1、读取配置文件实例化一个IoC容器
        ApplicationContext context = new ClassPathXmlApplicationContext("helloworld.xml");
        //2、从容器中获取Bean，注意此处完全“面向接口编程，而不是面向实现”
        HelloApi helloApi = context.getBean("hello", HelloApi.class);
        //3、执行业务逻辑
        helloApi.sayHello();
    }
}
````



Spring IoC容器的依赖有两层含义：**Bean依赖容器**和**容器注入Bean的依赖资源**：
**Bean依赖容器**：也就是说Bean要依赖于容器，这里的依赖是指容器负责创建Bean并管理Bean的生命周期，正是由于由容器来控制创建Bean并注入依赖，也就是控制权被反转了，这也正是IoC名字的由来，此处的有依赖是指Bean和容器之间的依赖关系。

**容器注入Bean的依赖资源**：容器负责注入Bean的依赖资源，依赖资源可以是Bean、外部文件、常量数据等，在Java中都反映为对象，并且由容器负责组装Bean之间的依赖关系，此处的依赖是指Bean之间的依赖关系，可以认为是传统类与类之间的“关联”、“聚合”、“组合”关系。

#### 构造器注入 ####
test类：
````
    @Test
    public void testConstructorDependencyInjectTest() {
        BeanFactory beanFactory =  new ClassPathXmlApplicationContext("constructorDependencyInject.xml");
        //获取根据参数索引依赖注入的Bean
        HelloApi byIndex = beanFactory.getBean("byIndex", HelloApi.class);
        byIndex.sayHello();

        //获取根据参数类型依赖注入的Bean
        HelloApi byType = beanFactory.getBean("byType", HelloApi.class);
        byType.sayHello();

        //获取根据参数名字依赖注入的Bean
        HelloApi byName = beanFactory.getBean("byName", HelloApi.class);
        byName.sayHello();
    }
````

xml配置
````
<!-- 通过构造器参数索引方式依赖注入 -->
<bean id="byIndex" class="com.shuke.chapter.impl.HelloImpl3">
    <constructor-arg index="0" value="Hello World!"/>
    <constructor-arg index="1" value="1"/>
</bean>
        <!-- 通过构造器参数类型方式依赖注入 -->
<bean id="byType" class="com.shuke.chapter.impl.HelloImpl3">
<constructor-arg type="java.lang.String" value="Hello World!"/>
<constructor-arg type="int" value="2"/>
</bean>
        <!-- 通过构造器参数名称方式依赖注入 -->
<bean id="byName" class="com.shuke.chapter.impl.HelloImpl3">
<constructor-arg name="message" value="Hello World!"/>
<constructor-arg name="index" value="3"/>
</bean>
````
bean实体类
````
public class HelloImpl3 implements HelloApi{
    private String message;
    private int index;
    @java.beans.ConstructorProperties({"message", "index"})
    public HelloImpl3(String message, int index) {
        this.message = message;
        this.index = index;
    }
    @Override
    public void sayHello() {
        System.out.println(index + ":" + message);
    }
}
````

#### 静态工厂方法注入和实例工厂注入 ####

test类
````
    @Test
    public void testConstructorDependencyInjectTest() {
        BeanFactory beanFactory =  new ClassPathXmlApplicationContext("chapter03/constructorDependencyInject.xml");
        //获取根据参数索引依赖注入的Bean
        HelloApi byIndex = beanFactory.getBean("byIndex", HelloApi.class);
        byIndex.sayHello();

        //获取根据参数类型依赖注入的Bean
        HelloApi byType = beanFactory.getBean("byType", HelloApi.class);
        byType.sayHello();

        //获取根据参数名字依赖注入的Bean
        HelloApi byName = beanFactory.getBean("byName", HelloApi.class);
        byName.sayHello();
    }
````
实例工厂xml配置
````
<?xml version="1.0" encoding="UTF-8"?>
<beans
        xmlns="http://www.springframework.org/schema/beans"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:context="http://www.springframework.org/schema/context"
        xsi:schemaLocation="
http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd">
    <!-- id 表示你这个组件的名字，class表示组件类 -->
<!-- 通过构造器参数索引方式依赖注入 -->
<bean id="byIndex" class="com.shuke.chapter.impl.HelloImpl3">
    <constructor-arg index="0" value="Hello World!"/>
    <constructor-arg index="1" value="1"/>
</bean>
        <!-- 通过构造器参数类型方式依赖注入 -->
<bean id="byType" class="com.shuke.chapter.impl.HelloImpl3">
<constructor-arg type="java.lang.String" value="Hello World!"/>
<constructor-arg type="int" value="2"/>
</bean>
        <!-- 通过构造器参数名称方式依赖注入 -->
<bean id="byName" class="com.shuke.chapter.impl.HelloImpl3">
<constructor-arg name="message" value="Hello World!"/>
<constructor-arg name="index" value="3"/>
</bean>
</beans>
````
静态工厂xml配置
````
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
    <bean id="byIndex"
          class="com.shuke.chaper03.DependencyInjectByStaticFactory" factory-method="newInstance">
        <constructor-arg index="0" value="Hello World!"/>
        <constructor-arg index="1" value="1"/>
    </bean>
    <bean id="byType"
          class="com.shuke.chaper03.DependencyInjectByStaticFactory" factory-method="newInstance">
        <constructor-arg type="java.lang.String" value="Hello World!"/>
        <constructor-arg type="int" value="2"/>
    </bean>
    <bean id="byName"
          class="com.shuke.chaper03.DependencyInjectByStaticFactory" factory-method="newInstance">
        <constructor-arg name="message" value="Hello World!"/>
        <constructor-arg name="index" value="3"/>
    </bean>
</beans>
````

#### setter方法注入 ####
test类
````
    @Test
    public void testConstructorDependencyInjectTest() {
        BeanFactory beanFactory =  new ClassPathXmlApplicationContext("chapter03/setterDependencyInject.xml");
        //获取根据参数索引依赖注入的Bean
        HelloApi byIndex = beanFactory.getBean("bean", HelloApi.class);
        byIndex.sayHello();
    }
````
setter xml配置
````
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
    <!-- 通过setter方式进行依赖注入 -->
    <bean id="bean" class="com.shuke.chapter.impl.HelloImpl4">
        <property name="message" value="Hello World!"/>
        <property name="index">
            <value>1</value>
        </property>
    </bean>
</beans>
````
bean类
````
package com.shuke.chapter.impl;

import com.shuke.chapter.HelloApi;

public class HelloImpl4 implements HelloApi{
    private String message;
    private int index;

    //setter方法
    public void setMessage(String message) {
        this.message = message;
    }
    public void setIndex(int index) {
        this.index = index;
    }
    @Override
    public void sayHello() {
        System.out.println(index + ":" + message);
    }
}

````
JavaBean getter/setter 方法命名约定：

- 该类必须要有公共的无参构造器，如public HelloImpl4() {}；
- 属性为private访问级别，不建议public，如private String message;
- 属性必要时通过一组setter（修改器）和getter（访问器）方法来访问；
- setter方法，以“set” 开头，后跟首字母大写的属性名，如“setMesssage”,简单属性一般只有一个方法参数，方法返回值通常为“void”;
- getter方法，一般属性以“get”开头，对于boolean类型一般以“is”开头，后跟首字母大写的属性名，如“getMesssage”，“isOk”；
- 还有一些其他特殊情况，比如属性有连续两个大写字母开头，如“URL”,则setter/getter方法为：“setURL”和“getURL”，其他一些特殊情况请参看“Java Bean”命名规范。
