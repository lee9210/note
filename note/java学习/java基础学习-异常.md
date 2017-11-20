### java异常概念 ###
- java异常是java提供的用于处理程序中错误的一种机制
- 所谓错误是指在程序运行的过程中发生的一些异常事件（如：除0溢出，数组下标越界，所要读取的文件不存在）
- 设计良好的程序应该在异常发生时提供处理这些错误的方法，使得程序不会因为异常的发生而阻断或产生不可预见的结果。
- java程序的执行过程如出现异常时间，可以生成一个异常类对象，该异常对象封装了异常事件的信息，并将被提交给java运行时系统，这个过程被称为抛出异常（throw）
- 当java运行时系统接收到异常对象时，会寻找能处理这一异常的代码，并把当前异常对象交给其处理，这一过程称为捕获异常（catch）


### java异常的分类 ###
- Error:称为错误，由java虚拟机生成并抛出，包括动态链接失败，虚拟机错误等，程序不对其做处理。
- Exception：所有异常类的父类，其子类对应了各种各样可能出现的异常时间，一般需要用户显示的声明或捕获。
- RuntimeException：一类特殊的异常，如被0除，数组下标超出范围，其产生比较频繁，处理麻烦，如果显示的声明或捕获，会对程序可读性和运行效率影响很大。因此，由系统自动检测并将他们交给缺省的异常处理程序（用户可不必对其处理）

### 异常的捕获和处理 ###
#### try语句 ####
- try{...}语句指定一段代码，该段代码就是一次捕获并处理异常的范围。
- 在执行过程中，该段代码可能会产生并抛出一种或几种类型的遗产刚兑现个，它后面的catch语句要分别对这些异常做相应的处理
- 如果没有异常产生，所有的catch代码段都被忽略不执行

#### catch语句 ####
- 在catch语句块中是都会异常进行处理的代码，每个try语句块可以伴随一个或多个catch语句，用于处理可能产生的不同类型的遗产搞对象。
- 在catch中声明的异常对象（catch（SomeException e））封装了异常时间发生的信息，在catch语句块中可以使用这个对象的一些方法获取这些信息
- 例如
	- getMessage()方法,用来得到有关异常时间的信息。
	- printStackTrace()方法，用来跟踪异常事件发生时执行堆栈的内容

#### finally语句 ####
- finally语句为异常处理提供一个统一的出口，使得在控制流程转到程序的其他部分以前，鞥能够对程序的状态统一的管理。
- 无论try所指定的程序块中是否抛出异常，finally所指定的代码都要被执行。
- 通常finally语句中可以进行资源的清除工作，如：
	- 关闭打开的文件
	- 删除临时文件

````
try{

}catch(Exception1 e){

}catch(Exception2 e){

}catch(Exception3 e){

}finally{

}
````
### 使用自定义的异常 ###
使用自定义异常一般有如下步骤
1. 通过继承java.lang.Exception类声明自己的异常类。
2. 在方法适当的位置生成自定义遗产规定实例，并用throw语句抛出
3. 在方法的声明部分用throws语句声明该方法可能抛出的异常。

````
public class RegexTest {
	public static void  main(String[] args) throws MyException{
		try{
			throwsException();
		}catch(MyException e){
			System.out.println(e.getId());
			System.out.println(e.getMessage());
		}
	}
	public static void throwsException() throws MyException{
		throw new MyException("not found",2);
	}	
}
class MyException extends Exception{
	private int id;
	
	public MyException(String message,int id){
		super(message);
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
}
````
注意：重写方法需要抛出与原方法所抛出异常类型一致异常或不抛出异常


