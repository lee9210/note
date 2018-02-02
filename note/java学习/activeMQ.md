### 安装方式 ###
#### win ####
1. 配置java环境
2. 启动bin文件中的activemq.bat文件

#### 控制台 ####
http://127.0.0.1:8161
admin/admin

### 发送消息的方式 ###
#### 直接recieve的方式 ####
Session.AUTO_ACKNOWLEDGE。当客户成功的从recieve方法返回的时候，或者从MessageListener.onMessage方法成功返回的时候，会话自动确认客户收到的消息。
Session.CLIENT_ACKNOWLEDGE。客户通过消息的acknowledge方法确认消息。需要注意的是，在这种模式中，确认是在会话层上进行；确认一个被消费的消息将自动确认所有已被会话消费的消息。例如一个消息消费者消费了10个消息，然后确认第5个消息，那么所有10个消息都被确认。

Session.DUPS_OK_ACKNOWLEDGE。该选择只是会话迟钝第确认消息的提交。如果JMS provider失败，那么可能会导致一些重复的消息。如果是重复的消息，那么JMS provider必须把消息头的JMSRedelivered字段设置为true
消息生产者
````
package com.jms.produce;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class JMSProduce {
	private static final String USER_NAME = ActiveMQConnection.DEFAULT_USER;//默认的链接用户名
	private static final String PASS_WORD = ActiveMQConnection.DEFAULT_PASSWORD;
//	private static final String BORKER_URL = ActiveMQConnection.DEFAULT_BROKER_URL;
	private static final String BORKER_URL = "failover://tcp://192.168.137.101:61616";
//	failover://tcp://localhost:61616
	private static final int SEND_NUM = 10;
	

	public static void main(String[] args) throws JMSException{
		ConnectionFactory factory;//连接工厂
		Connection connection = null;//连接
		Session session;//会话 接收或发送消息的线程（可以有事务）
		Destination destination;//消息的目的地
		MessageProducer messageProducer;//消息发送/生产者
		//实例化链接工厂
		factory = new ActiveMQConnectionFactory(JMSProduce.USER_NAME,JMSProduce.PASS_WORD,JMSProduce.BORKER_URL);
		
		try {
			connection = factory.createConnection();//通过链接工厂获取链接
			connection.start();//启动链接
			session  = connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);//创建session
			destination = session.createQueue("first queue --1");//创建消息队列
			messageProducer = session.createProducer(destination);//创建消息生产者
			message(session,messageProducer);//发送消息
			session.commit();
		} catch (JMSException e) {
			e.printStackTrace();
		} finally{
			if(connection!=null){
				connection.close();
			}
		}
	}
	
	public static void message(Session session,MessageProducer messageProducer) throws JMSException{
		for(int index = 0;index<JMSProduce.SEND_NUM;index++){
			TextMessage message = session.createTextMessage("send for mq :"+index);
			System.out.println("has send message : send for mq :" +index);
			messageProducer.send(message);
		}
	}
}

````
生产者运用结果
````
has send message : send for mq :0
has send message : send for mq :1
has send message : send for mq :2
has send message : send for mq :3
has send message : send for mq :4
has send message : send for mq :5
has send message : send for mq :6
has send message : send for mq :7
has send message : send for mq :8
has send message : send for mq :9
````

消息消费者
````
package com.jms.comsumer;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class JMSComsumer {
	
	private static final String USER_NAME = ActiveMQConnection.DEFAULT_USER;//默认的链接用户名
	private static final String PASS_WORD = ActiveMQConnection.DEFAULT_PASSWORD;
	private static final String BORKER_URL = "failover://tcp://192.168.137.101:61616";
	
	public static void main(String[] args){
		ConnectionFactory factory;//连接工厂
		Connection connection = null;//连接
		Session session;//会话 接收或发送消息的线程（可以有事务）
		Destination destination;//消息的目的地
		MessageConsumer messageComsumer;//消息消费者
		//实例化链接工厂
		factory = new ActiveMQConnectionFactory(JMSComsumer.USER_NAME,JMSComsumer.PASS_WORD,JMSComsumer.BORKER_URL);
		try {
			connection = factory.createConnection();//通过链接工厂获取链接
			connection.start();//启动链接
			session  = connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);//创建session
			destination = session.createQueue("first queue --1");//创建消息队列
			messageComsumer = session.createConsumer(destination);//创建消息消费者
			while(true){
				TextMessage textMessage = (TextMessage) messageComsumer.receive(1000);
				if(textMessage != null){
					System.out.println("had received message : " + textMessage.getText());
				}else{
					break;
				}
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
````
消费者运行结果
````
had received message : send for mq :0
had received message : send for mq :1
had received message : send for mq :2
had received message : send for mq :3
had received message : send for mq :4
had received message : send for mq :5
had received message : send for mq :6
had received message : send for mq :7
had received message : send for mq :8
had received message : send for mq :9
````
#### 使用Listener监听方式 ####
 
监听器：
````
package com.jms.comsumer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * 消息监听
 */
public class JMSListener implements MessageListener{

	@Override
	public void onMessage(Message message) {
		try {
			System.out.println("had received message : " + ((TextMessage)message).getText());
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}	
}
````
消费者
````
package com.jms.comsumer;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * 消息消费者
 */
public class JMSConsumer2 {

	private static final String USERNAME=ActiveMQConnection.DEFAULT_USER; // 默认的连接用户名
	private static final String PASSWORD=ActiveMQConnection.DEFAULT_PASSWORD; // 默认的连接密码
	private static final String BORKER_URL = "failover://tcp://192.168.137.101:61616";
	
	public static void main(String[] args) {
		ConnectionFactory connectionFactory; // 连接工厂
		Connection connection = null; // 连接
		Session session; // 会话 接受或者发送消息的线程
		Destination destination; // 消息的目的地
		MessageConsumer messageConsumer; // 消息的消费者
		
		// 实例化连接工厂
		connectionFactory=new ActiveMQConnectionFactory(JMSConsumer2.USERNAME, JMSConsumer2.PASSWORD, JMSConsumer2.BORKER_URL);
				
		try {
			connection=connectionFactory.createConnection();  // 通过连接工厂获取连接
			connection.start(); // 启动连接
			session=connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE); // 创建Session
			destination=session.createQueue("FirstQueue1");  // 创建连接的消息队列
			messageConsumer=session.createConsumer(destination); // 创建消息消费者
			messageConsumer.setMessageListener(new JMSListener()); // 注册消息监听
		} catch (JMSException e) {
			e.printStackTrace();
		} 
	}
}
````
### 发布/订阅 ###
消息订阅者1
````
package com.jms2.producer;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * 消息订阅者
 * 消息订阅者--1
 */
public class Consumer1 {

	private static final String USERNAME=ActiveMQConnection.DEFAULT_USER; // 默认的连接用户名
	private static final String PASSWORD=ActiveMQConnection.DEFAULT_PASSWORD; // 默认的连接密码
	private static final String BORKER_URL = "failover://tcp://192.168.137.101:61616";
	
	public static void main(String[] args) {
		ConnectionFactory connectionFactory; // 连接工厂
		Connection connection = null; // 连接
		Session session; // 会话 接受或者发送消息的线程
		Destination destination; // 消息的目的地
		MessageConsumer messageConsumer; // 消息的消费者
		
		// 实例化连接工厂
		connectionFactory=new ActiveMQConnectionFactory(Consumer1.USERNAME, Consumer1.PASSWORD, Consumer1.BORKER_URL);
				
		try {
			connection=connectionFactory.createConnection();  // 通过连接工厂获取连接
			connection.start(); // 启动连接
			session=connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE); // 创建Session
			destination=session.createTopic("topic--1");  // 创建连接的消息队列
			messageConsumer=session.createConsumer(destination); // 创建消息消费者
			messageConsumer.setMessageListener(new JMSListener()); // 注册消息监听
		} catch (JMSException e) {
			e.printStackTrace();
		} 
	}
}
````
消息订阅者2
````
package com.jms2.producer;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * 消息订阅者
 * 消息订阅者--2
 */
public class Consumer2 {

	private static final String USERNAME=ActiveMQConnection.DEFAULT_USER; // 默认的连接用户名
	private static final String PASSWORD=ActiveMQConnection.DEFAULT_PASSWORD; // 默认的连接密码
	private static final String BORKER_URL = "failover://tcp://192.168.137.101:61616";
	
	public static void main(String[] args) {
		ConnectionFactory connectionFactory; // 连接工厂
		Connection connection = null; // 连接
		Session session; // 会话 接受或者发送消息的线程
		Destination destination; // 消息的目的地
		MessageConsumer messageConsumer; // 消息的消费者
		
		// 实例化连接工厂
		connectionFactory=new ActiveMQConnectionFactory(Consumer2.USERNAME, Consumer2.PASSWORD, Consumer2.BORKER_URL);
				
		try {
			connection=connectionFactory.createConnection();  // 通过连接工厂获取连接
			connection.start(); // 启动连接
			session=connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE); // 创建Session
			destination=session.createTopic("topic--1");  // 创建连接的消息队列
			messageConsumer=session.createConsumer(destination); // 创建消息消费者
			messageConsumer.setMessageListener(new JMSListener()); // 注册消息监听
		} catch (JMSException e) {
			e.printStackTrace();
		} 
	}
}
````
监听者1
````
package com.jms2.producer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * 消息监听-1
 */
public class JMSListener implements MessageListener{

	@Override
	public void onMessage(Message message) {
		try {
			System.out.println("topic1 had received message : " + ((TextMessage)message).getText());
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}	
}

````
监听者2
````
package com.jms2.producer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * 消息监听-2
 */
public class JMSListener2 implements MessageListener{

	@Override
	public void onMessage(Message message) {
		try {
			System.out.println("topic2 had received message : " + ((TextMessage)message).getText());
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}	
}
````
生产者
````
package com.jms2.producer;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class Producer {
	private static final String USER_NAME = ActiveMQConnection.DEFAULT_USER;//默认的链接用户名
	private static final String PASS_WORD = ActiveMQConnection.DEFAULT_PASSWORD;
//	private static final String BORKER_URL = ActiveMQConnection.DEFAULT_BROKER_URL;
	private static final String BORKER_URL = "failover://tcp://192.168.137.101:61616";
//	failover://tcp://localhost:61616
	private static final int SEND_NUM = 10;
	

	public static void main(String[] args) throws JMSException{
		ConnectionFactory factory;//连接工厂
		Connection connection = null;//连接
		Session session;//会话 接收或发送消息的线程（可以有事务）
		Destination destination;//消息的目的地
		MessageProducer messageProducer;//消息发送/生产者
		//实例化链接工厂
		factory = new ActiveMQConnectionFactory(Producer.USER_NAME,Producer.PASS_WORD,Producer.BORKER_URL);
		
		try {
			connection = factory.createConnection();//通过链接工厂获取链接
			connection.start();//启动链接
			session  = connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);//创建session
			destination = session.createTopic("topic--1");
			messageProducer = session.createProducer(destination);//创建消息生产者
			message(session,messageProducer);//发送消息
			session.commit();
		} catch (JMSException e) {
			e.printStackTrace();
		} finally{
			if(connection!=null){
				connection.close();
			}
		}
	}
	
	public static void message(Session session,MessageProducer messageProducer) throws JMSException{
		for(int index = 0;index<Producer.SEND_NUM;index++){
			TextMessage message = session.createTextMessage("send for mq topic :"+index);
			System.out.println("has send message topic : send for mq :" +index);
			messageProducer.send(message);
		}
	}
}
````
运行结果

生产者：
````
has send message topic : send for mq :0
has send message topic : send for mq :1
has send message topic : send for mq :2
has send message topic : send for mq :3
has send message topic : send for mq :4
has send message topic : send for mq :5
has send message topic : send for mq :6
has send message topic : send for mq :7
has send message topic : send for mq :8
has send message topic : send for mq :9
````
消费者1
````
topic1 had received message : send for mq topic :0
topic1 had received message : send for mq topic :1
topic1 had received message : send for mq topic :2
topic1 had received message : send for mq topic :3
topic1 had received message : send for mq topic :4
topic1 had received message : send for mq topic :5
topic1 had received message : send for mq topic :6
topic1 had received message : send for mq topic :7
topic1 had received message : send for mq topic :8
topic1 had received message : send for mq topic :9
````
消费者2
````
topic1 had received message : send for mq topic :0
topic1 had received message : send for mq topic :1
topic1 had received message : send for mq topic :2
topic1 had received message : send for mq topic :3
topic1 had received message : send for mq topic :4
topic1 had received message : send for mq topic :5
topic1 had received message : send for mq topic :6
topic1 had received message : send for mq topic :7
topic1 had received message : send for mq topic :8
topic1 had received message : send for mq topic :9
````




