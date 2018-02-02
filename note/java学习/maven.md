管理项目的一个工具
### pom标签解释 ###
modelVersion：pom模型版本，4.0.0固定
groupId：一般指某个公司或某个组织的某个项目，比如：org.springframework
artifactId:一般指某个项目的某个具体模块，比如：spring-context
version：项目的版本SNAPSHOT（快照版本，不稳定）
dependencies：依赖

maven远程仓库：http://mvnrepository.com/
### maven命令 ###
mvn compile 编译
mvn clean：清空
mvn test：测试
mvn package：打包
mvn install：把项目安装到本地仓库

### 设置maven地址 ###
安装目录下的conf\settings.xml
<localRepository>D:\maven\repository</localRepository>

### 在eclipse中使用maven创建工程 ###
#### 一般方法 ####
1. new-> maven project -> next -> quickstart(开发web的时候用web)
2. groupId：com.java1234.helloWorld
3. artifectid：HelloWorld（对应项目的名字）

打包的方法：
maven->build->goals填：package
### maven仓库概念 ###
maven远程仓库配置文件
$m2_HOME/lib/maven-model-builder-3.3.3.jar
文件：org\apache\maven\model\pom-4.0.0.xml 
````
  <repositories>
    <repository>
      <id>central</id>
      <name>Central Repository</name>
      <url>https://repo.maven.apache.org/maven2</url><!-- 远程仓库地址 -->
      <layout>default</layout>
      <snapshots>
        <enabled>false</enabled>
      </snapshots>
    </repository>
  </repositories>
````
### maven聚合特性 ###
选择用父类管理用服务的类，要选择pom
然后在子类中添加：
````
	<parent>
	  <groupId>com.java1234.user</groupId>
	  <artifactId>user-parent</artifactId>
	  <version>0.0.1-SNAPSHOT</version>
	  <relativePath>../user-parent/pom.xml</relativePath><!-- 寻找pom的相对路径 -->
	</parent>
 
````
子项目中的两个结点
````
<groupId>com.java1234.user</groupId>
  <version>0.0.1-SNAPSHOT</version>
````
就可以去掉，由父类统一管理

jar依赖在父类pom中添加
````
<dependencyManagement>
  	<dependencies>
  		<dependency>
		<groupId>org.mybatis</groupId>
		<artifactId>mybatis</artifactId>
		<version>3.3.0</version>
		</dependency>
		
		<dependency>
			<groupId>mysql</groupId>
			<artifactId>mysql-connector-java</artifactId>
			<version>5.1.36</version>
		</dependency>
		
		<dependency>
			<groupId>log4j</groupId>
			<artifactId>log4j</artifactId>
			<version>${log4j.version}</version>
		</dependency>
	  
	    <dependency>
	      <groupId>junit</groupId>
	      <artifactId>junit</artifactId>
	      <version>${junit.version}</version>
	      <scope>test</scope>
	    </dependency>
	    
	    <dependency>
  		<groupId>org.springframework</groupId>
  		<artifactId>spring-core</artifactId>
  		<version>${spring.version}</version>
  	</dependency>
  	<dependency>
  		<groupId>org.springframework</groupId>
  		<artifactId>spring-beans</artifactId>
  		<version>${spring.version}</version>
  	</dependency>
  	<dependency>
         <groupId>org.springframework</groupId>
         <artifactId>spring-tx</artifactId>
         <version>${spring.version}</version>
        </dependency>
  	<dependency>
  		<groupId>org.springframework</groupId>
  		<artifactId>spring-context</artifactId>
  		<version>${spring.version}</version>
  	</dependency>
  	<dependency>
  		<groupId>org.springframework</groupId>
  		<artifactId>spring-context-support</artifactId>
  		<version>${spring.version}</version>
  	</dependency>
  	<dependency>
		<groupId>org.springframework</groupId>
		<artifactId>spring-web</artifactId>
		<version>${spring.version}</version>
	</dependency>
  	
  	<dependency>
		<groupId>org.springframework</groupId>
		<artifactId>spring-webmvc</artifactId>
		<version>${spring.version}</version>
	</dependency>
	
	<dependency>
		<groupId>org.springframework</groupId>
		<artifactId>spring-aop</artifactId>
		<version>${spring.version}</version>
	</dependency>
	
	
	<dependency>
		<groupId>org.springframework</groupId>
		<artifactId>spring-aspects</artifactId>
		<version>${spring.version}</version>
	</dependency>

	
	<dependency>
		<groupId>org.springframework</groupId>
		<artifactId>spring-jdbc</artifactId>
		<version>4.1.7.RELEASE</version>
	</dependency>
  
	  <dependency>
		<groupId>org.mybatis</groupId>
		<artifactId>mybatis-spring</artifactId>
		<version>1.2.3</version>
	</dependency>
	
	<dependency>
		<groupId>javax.servlet</groupId>
		<artifactId>javax.servlet-api</artifactId>
		<version>3.1.0</version>
	</dependency>
	
	<dependency>
		<groupId>javax.servlet.jsp</groupId>
		<artifactId>javax.servlet.jsp-api</artifactId>
		<version>2.3.1</version>
	</dependency>
	
	<!-- 添加jtl支持 -->
	<dependency>
		<groupId>javax.servlet</groupId>
		<artifactId>jstl</artifactId>
		<version>1.2</version>
	</dependency>
  	</dependencies>
  </dependencyManagement>
````

版本信息也可以在父类pom中添加
````
  <properties>
  	<spring.version>4.1.7.RELEASE</spring.version>
  	<junit.version>4.12</junit.version>
  	<log4j.version>1.2.17</log4j.version>
  </properties>
````
然后在下方用${xxx}引用
````
		<dependency>
			<groupId>log4j</groupId>
			<artifactId>log4j</artifactId>
			<version>${log4j.version}</version>
		</dependency>
	  
	    <dependency>
	      <groupId>junit</groupId>
	      <artifactId>junit</artifactId>
	      <version>${junit.version}</version>
	      <scope>test</scope>
	    </dependency>
	    
	    <dependency>
  		<groupId>org.springframework</groupId>
  		<artifactId>spring-core</artifactId>
  		<version>${spring.version}</version>
  	</dependency>
````
### maven依赖范围 ###
classpath分为三种：编译classpath，测试classpath，运行classpath

scope选项如下：
compile：编译依赖范围。默认是compile。在编译、测试、运行都有效；
test：测试依赖范围。仅测试有效；如junit
provided：已提供依赖范围。编译，测试有效，运行时无效。如servlet-api（因为在tomcat中包含这些包，在发布的时候会产生冲突）
system：系统依赖范围。使用system范围的依赖必须通过systempath指定依赖文件的路径
import：导入依赖范围。使用dependencyManagement时候，可以导入依赖配置。
例如junit的socpe为
````
	    <dependency>
	      <groupId>junit</groupId>
	      <artifactId>junit</artifactId>
	      <version>${junit.version}</version>
	      <scope>test</scope>
	    </dependency>
```

