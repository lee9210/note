### 基本概念 ###
- 构建网页的一种综合使用javascript和xml的技术
- html网页的异步传输技术
	- 在等待网页的传输过程中，用户依然可以和系统进行交互
	- 页面不用刷星就可以更新内容

典型流程
1. 客户端触发异步操作
2. 创建新的XMLHttpRequest对象
3. 与server进行连接
4. 服务器端进行连接处理
5. 返回包含处理结果的xml文档
6. XMLHttpRequest对象接收处理结果并分析
7. 更新页面

### XMLHttpRequest ###
- 重要的JavaScript对象，通过它提起对服务器端的请求
- 可以通过JavaScript提起请求
	- 如果要提起多个请求，需要多个XHR对象
- 请求的结果被预定义好的方法处理

### 重要属性 ###
#### readyState ####
- 0 = UNINITIALIZED:open not yet called
- 1 = LOADING:send for request not yet called
- 2 = LOADED:send called,headers and status are available
- 3 = INTERACTIVE: downloading response,
	- responseText only partially set
- 4 = COMPLETED :finish downloading response

#### responseText ####
response as text;null if error occurs or ready state < 3
#### responseXML ####
response as DOM Document abject;null if error or ready state < 3
#### status ####
integer status code
#### statusText ####
string status

### 相关方法 ###
#### 基本方法（base methods） ####
- open(method,url[,async])-initalizes a new HTTP request 
	- method can be "GET"、"POST"、"PUT"、"DELETE"
	- url must be an HTTP URL(start with "http://")
	- async is a boolean indicating whether request should be sent asynchronous - defaults to true
- send(body) -sends HTTP request (body can be null)
- abort() - called after send() to cancel request

#### 头文件方法（和http协议相关，header method） ####
- void setRequestHeader(name,value)
- String getResponseHeader(name)
- String getAllResponseHeaders()
	- returns a string where "header:value" pairs are delimited by carriage return

例如：
````
Connection:Keep-Alive
Date: Sun,15 May 2016 21:23:12 GMT
Content-Type:text/xml
Server:WEBrick/1.3.1 (Ruby/1.8.3/2004-12-25)
Content-Lenth:1810
````

实例：
````
<script>
	var url = "http://www.baidu.com"
	var req;
	if(window.XMLHttpRequst){
		req = new XMLHttpRequest();
	}else if(window.ActiveXObject){
		req = new ActiveXObject("Microsoft.XMLHTTP");
	}
	req.open("GET",rul,true);
	req.onreadystatechange = callback;//当状态改变的时候，调用callback方法
	req.send(null);
	
	function callback(){
		alert(req.readyState);
	}
</script>

````



	





