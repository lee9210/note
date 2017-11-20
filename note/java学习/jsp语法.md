### jsp传统语法 ###

#### Declaration（声明） ####
- 基本语法
	- <%! %>
	- <%= xxx %>  把xxx内容输出出来
- 说明：在此声明的变量、方法都会被保留成唯一的一份，直到jsp程序停止执行

例如：
````
<%! 
int i;//全局变量，jsp初始化的时候就初始化了，只初始化一次
public void setName(){ ......}
%>
<%
int i;//局部变量，每次访问都会重新声明。
%>
````
#### Scrptlet ####
- 基本语法
	- <%程序代码区%>
- 可以放入任何的java程序代码
例如：
````
<% 
	for(int i = 0;i<10;i++){}
%>
````

#### Ecpression ####
- 基本语法：<%=... ...%>
- =后面必须是字符串变量或者可以被转换成字符串的表达式
- 不需要以；结束
- 只有一行

例如：
````
<%="hello world"%>
<%=i+1%>
<%=request.getParameter("name")%>
````
#### Comment(注释) ####
- 注释格式
	- <%--.. ..--%>
	- <%//... ...%>
	- <%/*.. ..*/%>

#### Directives(编译指令) ####
- 相当于在编译期间的命令
- 格式
	- <%@Drective 属性="属性值"%>
- 常见的Drective：
	- page
	- include
	- taglib	

##### page #####
指明与JSP container的沟通方式
基本格式
````
<%@page language="script language"| --只能写java
				extends="className"| --从谁继承，一般不指定
				import="importList"| --引入进来那些类
				buffer="none|kb size"| --none:不缓冲，默认8kb
				session="true|false"| --是否可以使用session。默认true
				autoFlush="true|false"| --缓冲器是否自动清除。默认true
				isThreadSafe="true|false"|
				info="infoText"| --任何文件。很少用
				errorPage="errorPageUrl"| --出错之后显示的页面
				isErrorPage="true|false"
				contentType="contentTypeInfo"|
%>
````
-isErrorPage
````
<%@ page contentType="text/html;charset=gb2312"%>
<%@ page isErrorPage="true"%>
<html>
<body TEXT="red">
错误信息：<%= exception.getMessage()%>//获取错误信息
</body>
</html>
````
````
<%page errorPage="ErrPage.jsp"%>
<%
	String s = "13ads"
	int i = Integer.parseInt(s);
	out.println("s = " + s + "i = " + i);
%>
````
##### include #####
- 将指定的jsp程序或者html文件包含进来
- 格式：<%include file="fileURL"%>
- jsp engine会在jsp程序的转换时期，先把file属性设定的文件包含进来，然后开始执行转换及编译的工作。
- 限制：不能向fileURL中传参数。比如：abc.jsp?user=fadsf

````
<%@page contentType="text/html;charset=gb2312"%>
<html>
<head>
	<title>testBar.jsp</title>
</head>
<body>
	<table>
		<tr><td><%@ include file="titlebar.jsp"%></td></tr>
		<tr><td><% out.println("<p>这是用户显示区</p>")%></td></tr>
	</table>
</body>
</html>
````
#### Action动作指令 ####
- Action(动作指令)在运行期间的命令
- 常见的有
	- jsp:useBean
		- jsp:setProperty
		- jsp:getProperty
	- jsp:include
	- jsp:forward
		- jsp:param
	- jsp:plugin //往网页中嵌入一段

##### jsp:include/jsp:param #####
- 用于动态包含jsp程序或html文件等
- 除非这个指令会被执行到，否则不会被tomcat等jsp engine编译
- 格式：
	- <jsp:include page="URLSpec" flush="true" />
	- <jsp:include page="URLSpec" flush="true"><jsp:param name="ParamName" value="paramValue" /></jsp:include>
- jsp:param用来设定include文件时的参数和对应的值
- 例如


例1
````
<html>
<head>
	<title>include test</title>
</head>
<body bgcolor="white">
	<font color="red">
		the current date and time are
		<%@ include file="date.jsp"%>
		<jsp:include page="date.jsp" flush="true">
	</font>
</body>
</html>
````
date.jsp
````
<%@ page import="java.util.*" %>
<%@= (new java.util.Date()).toLocaleString() %>//转换成本地字符串
````

例2
compute.html
````
<html>
<head>
	<title>divide</title>
	<meta http=equiv="Content-Type" content="text/html;charset=gb2312">
</head>
<body bgcolor="white">
	<div>
		<form method="post" action="compute.jsp">
			<p>
				选择要做的运算
				<input type="radio" name="compute" value="division" checked>
				除法
				<input type="radio" name="compute" value="multiplication">
				乘法</p>
			<p>
				被除数（被乘数）
				<input type="text" name="value1">
				除数（乘数）
				<input type="text" name="value2">
			</p>
			<p>
				<input type="submit" name="submit" value="计算结果">
			</p> 
		</form>
	</div>
</body>
</html>
````
compute.jsp
````
<%@ page language="java" %>
<%
	String value1 = request.getParameter("value1");
	String value2 = request.getParameter("value2");
%>
<% if (request.getParameter("compute").equals("division")){%>
	<jsp:include page="devide.jsp" flush="true">
		<jsp:param name="v1" value="<%=value1%>" />
		<jsp:param name="v2" value="<%=value2%>">
<% } else {%>
<%@ include file="multiply.jsp" %>
<% } %>

````
devide.jsp
````
<html>
<head>
	<title>divide</title>
	<meta http=equiv="Content-Type" content="text/html;charset=gb2312">
</head>
<body bgcolor="white">
<center>
<h1>
<%
	try{
		float devidend = Float.parseFloat(request.getParameter("v1"));
		float devisor = Float.parseFloat(request.getParameter("v1"));
		double result = devidend/devisor
	
	%>
	<%= result%>
	<%
	//out.println(devidend + "/"+ devisor + "=" + result);
	} catch(Exception e){
		out.println("不合法的被乘数或除数")
	}
%>
</h1>
</center>
</body>
</html>
````
- 和编译指令include的区别
	- include编译指令是在jsp程序的转换时期就将file属性所指定的程序内容嵌入，然后再编译执行；而include指令在转换时期是不会被编译的，只有在客户端请求时期如果被执行到才会被动态的编译载入
	- include不能带参数，而<jsp:include>可以
#### 内置对象 ####
##### out #####
- out内置对象是一个缓冲的输出流，用来给客户端返回信息。它是favax.servlet.jsp.JspWriter的一个实例
- 典型应用：向客户端输出内容
- 常用方法
	- println();//向客户端输出各种类型数据
	- newLine();//输出一个换行符
	- close();//关闭输出流
	- flush();//输出缓冲区里的数据
	- clearBuffer();//清除缓冲区里的数据，同时把数据输出到客户端
	- clear();//清除缓冲区里的数据，不把数据输出到客户端
	- getBufferSize();//返回缓冲区的大小
##### request #####
- request内置对象表示的是调用JSP页面的请求。通常，request对象是javax.servlet.http.HttpServletRequest接口的一个实例
- 典型应用：通过request.getParameter("paramName")可以获得form提交过来的参数值
- 可以用此对象取得请求的header、信息（如浏览器版本，语言和编码等）、请求的方式（get/post）、请求的参数名称、参数值、客户端的主机名称等
- 常见方法
	- getMethod();//返回客户端向服务器端传送数据的方法
	- getParameter(String paramName);//返回客户端向服务端传送的参数值，该参数由paramName指定
	- getParameterValue(String name);//获得指定参数的所有值，由name指定
	- getRequestUrl();//获得发出请求字符串的客户端地址
	- getRemoteAddr();//获取客户端的ip地址
	- getRemoteHost();//获取客户端机器名称
	- getServerNmae();//获取服务器的名字
	- getServletName();//客户端所请求的脚本文件路径
	- getServerPort();//获取服务器端的端口
- 对应类：javax.servlet.http/HttpServletRequest
##### response #####
- 表示的是返回给客户端的相应
- 是javax.servlethttp.HttpServleResponse接口的一个实例
- 经常用于设置HTTP标题，添加cookie、设置响应内容的类型和状态、发送HTTP重定向和编码URL
- 常用方法
	- addCookie(Cookie cookie);//添加一个Cookie对象，用于在客户端保存特定的信息
	- addHeader(String name,String value);//添加HTTP头信息，该Header信息将发送到客户端
	- containsHeader(String name);//判断指定名字的HTTP文件头是否存在
	- sendError(int);//向客户端发送错误的信息
	- sendRedirect(String url);//重定向JSP文件
		- 和<jsp:forward>的区别
			- sendRedirect通过向客户端发起二次申请，不同的request对象
			- jsp:forward是同一个request，在服务器内部转发
	- setContentType(String contentType);//设置MIME类型编码方式

##### Cookie #####
- Http协议的无链接性要求出现一种保存C/S间状态的机制
- Cookie：保存到客户端的一个文本文件，与特定客户相关
- Cookie以"key-value"对的形式保存数据
- 通过getName和getValue的方式得到相应的名字和值

##### session #####
- <%@ page session="true"%>(默认) --表示session功能已经在jsp页面中启动
- session常用方法
	- void setAttribute(String name,Object value);
	- Object getAttribute(String name);
	- boolean isNew();

### servlet和jsp的通信 ###
- 从jsp调用servlet可用<jsp:forward>。请求信息自动传递到servlet或者通过sendRedirect
- 从servlet调用jsp使用
	- RequestDispatcher接口的forward(req,res)方法
	- 请求信息需要显式传递（在req、res参数中）
	- 或者通过sendRedirect





