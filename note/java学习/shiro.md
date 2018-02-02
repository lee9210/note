java安全框架，提供了认证，授权，加密和会话管理功能。
解决了以下问题
认证 - 用户身份识别，常被称为用户登录；
授权 - 访问控制
密码加密 - 保护或隐藏数据防止被偷窥
会话管理 - 每用户相关的时间敏感状态
辅助特性 - web应用安全、单元测试和多线程
## helloworld ##
pom.xml
````
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.java.shiro</groupId>
  <artifactId>shiro01</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  
  <dependencies>
  	<dependency>
		<groupId>org.apache.shiro</groupId>
		<artifactId>shiro-core</artifactId>
		<version>1.2.4</version>
    </dependency>
    
    <dependency>
		<groupId>org.slf4j</groupId>
		<artifactId>slf4j-log4j12</artifactId>
		<version>1.7.12</version>
	</dependency>
  </dependencies>
</project>
````
shiro.ini配置文件，包含的是用户名密码等信息
````
[users]
java=123456
jack=123
````
log4j配置文件。log4j.properties
````
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
log4j.rootLogger=INFO, stdout

log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%d %p [%c] - %m %n

# General Apache libraries
log4j.logger.org.apache=WARN

# Spring
log4j.logger.org.springframework=WARN

# Default Shiro logging
log4j.logger.org.apache.shiro=TRACE

# Disable verbose logging
log4j.logger.org.apache.shiro.util.ThreadContext=WARN
log4j.logger.org.apache.shiro.cache.ehcache.EhCache=WARN

````
HelloWorld.java
````
package com.java.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;


public class HelloWorld {

	public static void main(String[] args){
		//读取配置文件，初始化SecurityManager工厂
		Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
		//获取SecurityManager实例
		SecurityManager securityManager = factory.getInstance();
		//把SecurityManager实例绑定到SecurityUtils
		SecurityUtils.setSecurityManager(securityManager);
		//得到当前执行的用户
		Subject currentUser = SecurityUtils.getSubject();
		//创建token令牌，用户名/密码形式
		UsernamePasswordToken token = new UsernamePasswordToken("java","123456");
		try {
			//身份认证、登陆
			currentUser.login(token);
			System.out.println("身份认证成功");
		}catch (Exception e){
			e.printStackTrace();
			System.out.println("身份认证失败");
		}
		currentUser.logout();
	}
}

````
运行结果：
````
D:\Java\jdk1.8.0_144\bin\java "-javaagent:D:\Java\intellij idea\lib\idea_rt.jar=51846:D:\Java\intellij idea\bin" -Dfile.encoding=UTF-8 -classpath D:\Java\jdk1.8.0_144\jre\lib\charsets.jar;D:\Java\jdk1.8.0_144\jre\lib\deploy.jar;D:\Java\jdk1.8.0_144\jre\lib\ext\access-bridge-64.jar;D:\Java\jdk1.8.0_144\jre\lib\ext\cldrdata.jar;D:\Java\jdk1.8.0_144\jre\lib\ext\dnsns.jar;D:\Java\jdk1.8.0_144\jre\lib\ext\jaccess.jar;D:\Java\jdk1.8.0_144\jre\lib\ext\jfxrt.jar;D:\Java\jdk1.8.0_144\jre\lib\ext\localedata.jar;D:\Java\jdk1.8.0_144\jre\lib\ext\nashorn.jar;D:\Java\jdk1.8.0_144\jre\lib\ext\sunec.jar;D:\Java\jdk1.8.0_144\jre\lib\ext\sunjce_provider.jar;D:\Java\jdk1.8.0_144\jre\lib\ext\sunmscapi.jar;D:\Java\jdk1.8.0_144\jre\lib\ext\sunpkcs11.jar;D:\Java\jdk1.8.0_144\jre\lib\ext\zipfs.jar;D:\Java\jdk1.8.0_144\jre\lib\javaws.jar;D:\Java\jdk1.8.0_144\jre\lib\jce.jar;D:\Java\jdk1.8.0_144\jre\lib\jfr.jar;D:\Java\jdk1.8.0_144\jre\lib\jfxswt.jar;D:\Java\jdk1.8.0_144\jre\lib\jsse.jar;D:\Java\jdk1.8.0_144\jre\lib\management-agent.jar;D:\Java\jdk1.8.0_144\jre\lib\plugin.jar;D:\Java\jdk1.8.0_144\jre\lib\resources.jar;D:\Java\jdk1.8.0_144\jre\lib\rt.jar;E:\workspace\shiro01\target\classes;C:\Users\Administrator\.m2\repository\org\apache\shiro\shiro-core\1.2.4\shiro-core-1.2.4.jar;C:\Users\Administrator\.m2\repository\org\slf4j\slf4j-api\1.6.4\slf4j-api-1.6.4.jar;C:\Users\Administrator\.m2\repository\commons-beanutils\commons-beanutils\1.8.3\commons-beanutils-1.8.3.jar;C:\Users\Administrator\.m2\repository\org\slf4j\slf4j-log4j12\1.7.12\slf4j-log4j12-1.7.12.jar;C:\Users\Administrator\.m2\repository\log4j\log4j\1.2.17\log4j-1.2.17.jar com.java.shiro.HelloWorld
2018-01-23 22:35:30,469 DEBUG [org.apache.shiro.io.ResourceUtils] - Opening resource from class path [shiro.ini] 
2018-01-23 22:35:30,492 DEBUG [org.apache.shiro.config.Ini] - Parsing [users] 
2018-01-23 22:35:30,495 TRACE [org.apache.shiro.config.Ini] - Discovered key/value pair: java=123456 
2018-01-23 22:35:30,495 TRACE [org.apache.shiro.config.Ini] - Discovered key/value pair: jack=123 
2018-01-23 22:35:30,497 DEBUG [org.apache.shiro.config.IniFactorySupport] - Creating instance from Ini [sections=users] 
2018-01-23 22:35:30,497 TRACE [org.apache.shiro.config.Ini] - Specified name was null or empty.  Defaulting to the default section (name = "") 
2018-01-23 22:35:30,595 DEBUG [org.apache.shiro.realm.text.IniRealm] - Discovered the [users] section.  Processing... 
2018-01-23 22:35:30,619 TRACE [org.apache.shiro.mgt.DefaultSecurityManager] - Context already contains a SecurityManager instance.  Returning. 
2018-01-23 22:35:30,619 TRACE [org.apache.shiro.mgt.DefaultSecurityManager] - No identity (PrincipalCollection) found in the context.  Looking for a remembered identity. 
2018-01-23 22:35:30,619 TRACE [org.apache.shiro.mgt.DefaultSecurityManager] - No remembered identity found.  Returning original context. 
2018-01-23 22:35:30,623 TRACE [org.apache.shiro.subject.support.DelegatingSubject] - attempting to get session; create = false; session is null = true; session has id = false 
2018-01-23 22:35:30,624 TRACE [org.apache.shiro.subject.support.DelegatingSubject] - attempting to get session; create = false; session is null = true; session has id = false 
2018-01-23 22:35:30,624 TRACE [org.apache.shiro.subject.support.DelegatingSubject] - attempting to get session; create = false; session is null = true; session has id = false 
2018-01-23 22:35:30,624 TRACE [org.apache.shiro.subject.support.DelegatingSubject] - attempting to get session; create = false; session is null = true; session has id = false 
2018-01-23 22:35:30,624 TRACE [org.apache.shiro.subject.support.DelegatingSubject] - attempting to get session; create = false; session is null = true; session has id = false 
2018-01-23 22:35:30,624 TRACE [org.apache.shiro.subject.support.DelegatingSubject] - attempting to get session; create = false; session is null = true; session has id = false 
2018-01-23 22:35:30,624 TRACE [org.apache.shiro.authc.AbstractAuthenticator] - Authentication attempt received for token [org.apache.shiro.authc.UsernamePasswordToken - java, rememberMe=false] 
2018-01-23 22:35:30,625 DEBUG [org.apache.shiro.realm.AuthenticatingRealm] - Looked up AuthenticationInfo [java] from doGetAuthenticationInfo 
2018-01-23 22:35:30,625 DEBUG [org.apache.shiro.realm.AuthenticatingRealm] - AuthenticationInfo caching is disabled for info [java].  Submitted token: [org.apache.shiro.authc.UsernamePasswordToken - java, rememberMe=false]. 
2018-01-23 22:35:30,625 DEBUG [org.apache.shiro.authc.credential.SimpleCredentialsMatcher] - Performing credentials equality check for tokenCredentials of type [[C and accountCredentials of type [java.lang.String] 
2018-01-23 22:35:30,625 DEBUG [org.apache.shiro.authc.credential.SimpleCredentialsMatcher] - Both credentials arguments can be easily converted to byte arrays.  Performing array equals comparison 
2018-01-23 22:35:30,626 DEBUG [org.apache.shiro.authc.AbstractAuthenticator] - Authentication successful for token [org.apache.shiro.authc.UsernamePasswordToken - java, rememberMe=false].  Returned account [java] 
2018-01-23 22:35:30,626 DEBUG [org.apache.shiro.subject.support.DefaultSubjectContext] - No SecurityManager available in subject context map.  Falling back to SecurityUtils.getSecurityManager() lookup. 
2018-01-23 22:35:30,626 TRACE [org.apache.shiro.mgt.DefaultSecurityManager] - Context already contains a SecurityManager instance.  Returning. 
2018-01-23 22:35:30,626 TRACE [org.apache.shiro.subject.support.DelegatingSubject] - attempting to get session; create = false; session is null = true; session has id = false 
2018-01-23 22:35:30,626 DEBUG [org.apache.shiro.subject.support.DefaultSubjectContext] - No SecurityManager available in subject context map.  Falling back to SecurityUtils.getSecurityManager() lookup. 
2018-01-23 22:35:30,626 TRACE [org.apache.shiro.subject.support.DelegatingSubject] - attempting to get session; create = false; session is null = true; session has id = false 
2018-01-23 22:35:30,626 TRACE [org.apache.shiro.subject.support.DelegatingSubject] - attempting to get session; create = false; session is null = true; session has id = false 
2018-01-23 22:35:30,626 TRACE [org.apache.shiro.subject.support.DelegatingSubject] - attempting to get session; create = false; session is null = true; session has id = false 
2018-01-23 22:35:30,626 TRACE [org.apache.shiro.subject.support.DelegatingSubject] - attempting to get session; create = false; session is null = true; session has id = false 
2018-01-23 22:35:30,626 TRACE [org.apache.shiro.subject.support.DelegatingSubject] - attempting to get session; create = false; session is null = true; session has id = false 
2018-01-23 22:35:30,626 TRACE [org.apache.shiro.subject.support.DelegatingSubject] - attempting to get session; create = false; session is null = true; session has id = false 
2018-01-23 22:35:30,627 TRACE [org.apache.shiro.subject.support.DelegatingSubject] - attempting to get session; create = true; session is null = true; session has id = false 
2018-01-23 22:35:30,630 TRACE [org.apache.shiro.subject.support.DelegatingSubject] - Starting session for host null 
2018-01-23 22:35:30,631 DEBUG [org.apache.shiro.session.mgt.AbstractValidatingSessionManager] - No sessionValidationScheduler set.  Attempting to create default instance. 
2018-01-23 22:35:30,632 TRACE [org.apache.shiro.session.mgt.AbstractValidatingSessionManager] - Created default SessionValidationScheduler instance of type [org.apache.shiro.session.mgt.ExecutorServiceSessionValidationScheduler]. 
2018-01-23 22:35:30,632 INFO [org.apache.shiro.session.mgt.AbstractValidatingSessionManager] - Enabling session validation scheduler... 
2018-01-23 22:35:30,641 TRACE [org.apache.shiro.session.mgt.DefaultSessionManager] - Creating session for host null 
2018-01-23 22:35:30,641 DEBUG [org.apache.shiro.session.mgt.DefaultSessionManager] - Creating new EIS record for new session instance [org.apache.shiro.session.mgt.SimpleSession,id=null] 
2018-01-23 22:35:31,008 TRACE [org.apache.shiro.session.mgt.AbstractValidatingSessionManager] - Attempting to retrieve session with key org.apache.shiro.session.mgt.DefaultSessionKey@4ac68d3e 
2018-01-23 22:35:31,008 TRACE [org.apache.shiro.subject.support.DelegatingSubject] - attempting to get session; create = false; session is null = false; session has id = true 
2018-01-23 22:35:31,008 TRACE [org.apache.shiro.session.mgt.AbstractValidatingSessionManager] - Attempting to retrieve session with key org.apache.shiro.session.mgt.DefaultSessionKey@4ac68d3e 
2018-01-23 22:35:31,008 TRACE [org.apache.shiro.session.mgt.AbstractValidatingSessionManager] - Attempting to retrieve session with key org.apache.shiro.session.mgt.DefaultSessionKey@4ac68d3e 
2018-01-23 22:35:31,009 TRACE [org.apache.shiro.mgt.DefaultSecurityManager] - This org.apache.shiro.mgt.DefaultSecurityManager instance does not have a [org.apache.shiro.mgt.RememberMeManager] instance configured.  RememberMe services will not be performed for account [java]. 
2018-01-23 22:35:31,009 TRACE [org.apache.shiro.subject.support.DelegatingSubject] - attempting to get session; create = false; session is null = false; session has id = true 
身份认证成功
2018-01-23 22:35:31,009 TRACE [org.apache.shiro.subject.support.DelegatingSubject] - attempting to get session; create = false; session is null = false; session has id = true 
2018-01-23 22:35:31,009 TRACE [org.apache.shiro.session.mgt.AbstractValidatingSessionManager] - Attempting to retrieve session with key org.apache.shiro.session.mgt.DefaultSessionKey@4ac68d3e 
2018-01-23 22:35:31,009 TRACE [org.apache.shiro.subject.support.DelegatingSubject] - attempting to get session; create = false; session is null = false; session has id = true 
2018-01-23 22:35:31,009 TRACE [org.apache.shiro.session.mgt.AbstractValidatingSessionManager] - Attempting to retrieve session with key org.apache.shiro.session.mgt.DefaultSessionKey@4ac68d3e 
2018-01-23 22:35:31,009 DEBUG [org.apache.shiro.mgt.DefaultSecurityManager] - Logging out subject with primary principal java 
2018-01-23 22:35:31,010 TRACE [org.apache.shiro.realm.CachingRealm] - Cleared cache entries for account with principals [java] 
2018-01-23 22:35:31,010 TRACE [org.apache.shiro.subject.support.DelegatingSubject] - attempting to get session; create = false; session is null = false; session has id = true 
2018-01-23 22:35:31,010 TRACE [org.apache.shiro.session.mgt.AbstractValidatingSessionManager] - Attempting to retrieve session with key org.apache.shiro.session.mgt.DefaultSessionKey@4ac68d3e 
2018-01-23 22:35:31,010 TRACE [org.apache.shiro.session.mgt.AbstractValidatingSessionManager] - Attempting to retrieve session with key org.apache.shiro.session.mgt.DefaultSessionKey@4ac68d3e 
2018-01-23 22:35:31,010 TRACE [org.apache.shiro.subject.support.DelegatingSubject] - attempting to get session; create = false; session is null = false; session has id = true 
2018-01-23 22:35:31,010 TRACE [org.apache.shiro.session.mgt.AbstractValidatingSessionManager] - Attempting to retrieve session with key org.apache.shiro.session.mgt.DefaultSessionKey@4ac68d3e 
2018-01-23 22:35:31,010 DEBUG [org.apache.shiro.session.mgt.AbstractSessionManager] - Stopping session with id [6ecfbf2b-255d-451c-94ac-cb29a2c06a57] 

Process finished with exit code 0

````

#### subject认证主体 ####
subject认证主体包括两个信息：
principals:身份，可以是用户名，邮件，手机号等等，用来标识一个登陆主体身份
credentials:凭证，常见有密码，数字证书等等

#### realm和jdbcrealm ####

2.10


