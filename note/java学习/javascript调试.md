#### debugger ####
相当于在程序中中加了断点，打开控制台的时候，当程序自动运行到这一步的时候自动停止，按F8或F10继续
例如：
````
<script>
	function test(){
		var i = 0;
		debugger;
		alert(i);
	}
</script>
````
#### console.log() ####
在控制台打印内容。console对象的上面5种方法，都可以使用printf风格的占位符。不过，占位符的种类比较少，只支持字符（%s）、整数（%d或%i）、浮点数（%f）和对象（%o）四种。
例如：
````
<script>
	function test(){
		var i = 0;
		console.info("i value is :"+i);
		console.log("i value is :"+i);
		console.warn("i value is :"+i);
		console.error("i value is :"+i);
		console.debug("i value is :"+i);
		var name = "my name is shuke";
		var json = {"key":"value"};
		console.log("%s year",2016);
		console.log("%d year %d month",2016,6);	
		console.log("%f",3.1415);
		console.log("%o",json);


	}
</script>
````
#### xhr断点/ajax断点 ####
XHR 断点
右侧调试区有一个 XHR Breakpoints，点击+ 并输入 URL 包含的字符串即可监听该 URL 的 Ajax 请求，输入内容就相当于 URL 的过滤器。如果什么都不填，那么就监听所有 XHR 请求。一旦 XHR 调用触发时就会在 request.send() 的地方中断。

#### console.trace ####
使用console.trace (仅仅只是在控制台中跟踪) 可以方便地调试JavaScript.
````
	var car;
	var func1 = function() {
	  func2();
	}

	var func2 = function() {
	  func4();
	}
	var func3 = function() {
	}

	var func4 = function() {
	  car = new Car();
	  car.funcX();
	}
	var Car = function() {
	  this.brand = 'volvo';
	  this.color = 'red';
	  this.funcX = function() {
		this.funcY();
	  }

	  this.funcY = function() {
		this.funcZ();
	  }

	  this.funcZ = function() {
		console.trace('trace car')
	  }
	}
	func1();
````
控制台输出：
````
trace car
Car.funcZ @ jstest.html:53
Car.funcY @ jstest.html:49
Car.funcX @ jstest.html:45
func4 @ jstest.html:39
func2 @ jstest.html:32
func1 @ jstest.html:28
(anonymous) @ jstest.html:56
````
#### 控制台打断点 ####
在控制台中使用debug(funcName)，当到达传入的函数时，代码将停止。

#### blackbox script ####
当你使用 “Event listener breakpoint :: Mouse :: Click” ，可能在一些第三方的库里（例如jquery）就先结束了， 你需要在 debugger 里走几次才能到达“真正的” event handler。“blackbox” 第三方脚本是避免这个问题一个很棒的方法。在 blackboxed 脚本中 debugger 不会中断, 他会继续执行直到遇到一行不在blackboxed 文件中的代码。 在 callstack 你可以选择第三方的库右击选择 “Blackbox Script” 将其放入 blackbox。

