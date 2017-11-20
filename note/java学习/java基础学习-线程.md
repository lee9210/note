### 线程的基本概念 ###
线程：在一个程序中，主方法执行叫做进程
- 线程是一个程序内部的顺序控制流
- 线程和进程的区别
	- 每个进程都有独立的代码和数据空间（进程上下文），进程间的切换会有较大的开销
	- 线程可以看成是轻量级的进程，同一类线程共享代码和数据空间，每个线程有独立的运行栈和程序计数器（PC），线程切换的开销小
	- 多进程：在操作系统中能同时运行多个任务（程序）
	- 多线程：在同一应用程序中有多个顺序流同时执行
- java的线程是通过java.lang.Thread类来实现的
- vm启动时会有一个由主方法所定义的线程
- 可以通过创建Thread的实例来创建新的线程
- 每个线程都是通过某个特定Thread对象所对应的方法run()来完成其操作的，方法run()称为线程体
- 通过调用Thread类的start()方法来启动一个线程
### 线程的创建和启动 ###
可以有两种方式创建新的线程
- 第一种
	- 定义线程类实现Runnable接口
	- Thread myThread = new Thread(target)//target为Runnable接口类型
	- Runnable中只有一个方法
		- public void run;//用以定义线程运行体
	- 使用Runnable接口可以为多个线程提供共享的数据
	- 实现Runnable接口的类的run方法中定义中可以使用Thread的静态方法：
		- public static Thread currentThread()//获取当前线程的引用
- 第二种
	- 可以定义一个Thread的子类，并重写其run方法如：
		- class MyThread extends Thread{public void run(){......}}
	- 然后生成该类的对象
		- MyThread myThread = new MyThread(...)

````
public class ThreadTest {
	public static void main(String[] args){
		runnableTest();
	}
	public static void runnableTest(){
		Runner1 r = new Runner1();
		//r.run();
		Thread t = new Thread(r);
		t.start();
		for(int i = 0;i<10;i++){
			System.out.println("main thread : ----"+i);
		}
	}
}

class Runner1 implements Runnable{
	@Override
	public void run() {
		for(int i=0;i<10;i++){
			System.out.println("Runnable:"+i);
		}
	}
}
````
运行结果
````
main thread : ----0
Runnable:0
main thread : ----1
Runnable:1
main thread : ----2
Runnable:2
main thread : ----3
Runnable:3
main thread : ----4
Runnable:4
Runnable:5
Runnable:6
Runnable:7
Runnable:8
Runnable:9
main thread : ----5
main thread : ----6
main thread : ----7
main thread : ----8
main thread : ----9
````
第二种方法
````
public class ThreadTest {
	public static void main(String[] args){
		runnableTest();
	}
	public static void runnableTest(){
		Runner1 r = new Runner1();
		r.start();
		for(int i = 0;i<10;i++){
			System.out.println("main thread : ----"+i);
		}
	}
}

class Runner1 extends Thread{
	@Override
	public void run() {
		for(int i=0;i<10;i++){
			System.out.println("Runnable:"+i);
		}
	}
}
````
运行结果
````
main thread : ----0
Runnable:0
main thread : ----1
Runnable:1
main thread : ----2
Runnable:2
main thread : ----3
Runnable:3
main thread : ----4
Runnable:4
Runnable:5
Runnable:6
Runnable:7
Runnable:8
Runnable:9
main thread : ----5
main thread : ----6
main thread : ----7
main thread : ----8
main thread : ----9	
````
### 线程的调度和优先级 ###
线程的优先级
- java提供一个线程调度器来监控程序中启动后进入就绪状态的所有线程。线程调度器按照线程的优先级决定应调度哪个线程来执行。
- 线程的优先级用数字表示，范围从1到10，一个线程的缺省优先级是5
	- Thread.MIN_PRIORITY = 1
	- Thread.MAX_PRIORITY = 10
	- Thread.NORM_PRIORITY = 5
- 使用下述线方法获得或设置线程对象的优先级
	- int getPriority();
	- void setPriority(int newPriority);

````
public class ThreadTest {
	public static void main(String[] args){
		Thread t1 = new Thread(new T1());
		Thread t2 = new Thread(new T2());
		t1.setPriority(Thread.NORM_PRIORITY+3);
		t1.start();t2.start();
	}
}
class T1 implements Runnable{
	public void run(){
		for(int i=0;i<10;i++){
			System.out.println("T1 : "+i);
		}
	}
}
class T2 implements Runnable{
	public void run(){
		for(int i=0;i<10;i++){
			System.out.println("T2 : "+i);
		}
	}
}
````
运行结果：
````
T1 : 0
T2 : 0
T1 : 1
T2 : 1
T1 : 2
T2 : 2
T1 : 3
T2 : 3
T1 : 4
T2 : 4
T1 : 5
T2 : 5
T1 : 6
T2 : 6
T1 : 7
T2 : 7
T1 : 8
T2 : 8
T1 : 9
T2 : 9
````
### 线程的状态控制 ###
线程状态控制基本方法

|方法|功能|
|-|-|
|isAlive()|判断线程是否还“或者”，即线程是否还未终止|
|getPriority()|获得线程的优先级数值|
|setPriority()|设置线程的优先级数值|
|Thread.sleep()|将当前线程睡眠指定毫秒数|
|join()|调用某线程的该方法，将当前线程与该线程“合并”，即等待该线程结束，再恢复当前线程的运行|
|yield()|让出cpu，当前线程进入就绪队列等待调度。|
|wait()|当前线程进入对象的wait pool|
|notify()  notifyAll()|唤醒对象的wait pool中的一个/所有等待线程|

sleep/join/yield方法
sleep方法
- 可以调用Thread的静态方法：public static void sleep(long millis) throws InterruptedException.使得当前线程休眠（暂停执行millis毫秒）
- 由于是静态方法，sleep可以由类名直接调用：Thread.sleep(..);

````
	public static void main(String[] args){
		System.out.println("start time :"+new Date());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("end time :"+new Date());
	}
````
运行结果
````
start time :Wed Nov 01 22:23:25 CST 2017
end time :Wed Nov 01 22:23:26 CST 2017
````
join方法
- 合并某个线程

````
public class ThreadTest {
	public static void main(String[] args){
		MyThread2 t1 = new MyThread2("t1");
		t1.start();
		try {
			t1.join();
		} catch (InterruptedException e) {}
		for(int i=0;i<10;i++){
			System.out.println("i am main thread");
		}
	}
}

class MyThread2 extends Thread{
	MyThread2(String s){
		super(s);
	}
	public void run(){
		for(int i=0;i<10;i++){
			System.out.println("i am "+getName());
			try {
				sleep(1000);
			} catch (InterruptedException e) {return;}
		}
	}
}
````
输出结果:
````
i am t1
i am t1
i am t1
i am t1
i am t1
i am t1
i am t1
i am t1
i am t1
i am t1
i am main thread
i am main thread
i am main thread
i am main thread
i am main thread
i am main thread
i am main thread
i am main thread
i am main thread
i am main thread

````
若无t1.join方法，则for循环中的输出和线程同时执行

yield方法
- 让出cpu，给其他线程执行的机会。

````
public class ThreadTest {
	public static void main(String[] args){
		MyThread3 t1 = new MyThread3("t1");
		MyThread3 t2 = new MyThread3("t2");
		t1.start();t2.start();
	}
}

class MyThread3 extends Thread{
	MyThread3(String s){
		super(s);
	}
	public void run(){
		for(int i=0;i<10;i++){
			System.out.println(getName()+": "+i);
			if(i%2==0){
				yield();
			}
		}
	}
}
````

结果：

````
t1: 0
t1: 1
t1: 2
t2: 0
t2: 1
t2: 2
t2: 3
t2: 4
t1: 3
t1: 4
t2: 5
t1: 5
t2: 6
t2: 7
t2: 8
t2: 9
t1: 6
t1: 7
t1: 8
t1: 9
````
#### 例子 ####
同一个程序起两个线程
````
public class TestThread2 {
	public static void main(String args[]) {
		Runner2 r = new Runner2();
		Thread t1 = new Thread(r);
		Thread t2 = new Thread(r);
		t1.start();
		t2.start();
	}
}

class Runner2 implements Runnable {
	public void run() {
		for(int i=0; i<30; i++) {	
			System.out.println("No. " + i);
		}
	}
}
````
线程接口中使用sleep方法
````
public class TestThread3{
	public static void main(String args[]) {
		Runner3 r = new Runner3();
		Thread t = new Thread(r);
		t.start();
	}
}

class Runner3 implements Runnable {
	public void run() {
		for(int i=0; i<30; i++) {	
			if(i%10==0 && i!=0) {
				try{
					Thread.sleep(2000); 
				}catch(InterruptedException e){}
			}
			System.out.println("No. " + i);
		}
	}
}
````
如何让一个正常执行的线程停止
不是调用stop
````
public class TestThread4 {	
	public static void main(String args[]){
		Runner4 r = new Runner4();
       	Thread t = new Thread(r);
        t.start();
        for(int i=0;i<100000;i++){
        	if(i%10000==0 & i>0)
        		System.out.println("in thread main i=" + i);
        }
        System.out.println("Thread main is over");
        r.shutDown();
        //t.stop();
    }
}

class Runner4 implements Runnable {
  private boolean flag=true;
  
	public void run() {
		int i = 0;
		while (flag==true) {
			System.out.print(" " + i++);	
		}
	}
	
  public void shutDown() {
		flag = false;
  }
}
````
isAlive()方法和currentThread()方法
````
public class TestThread6 {	
	public static void main(String args[]){
		Thread t = new Runner6();
   	t.start();
		
		for(int i=0; i<50; i++) {
			System.out.println("MainThread: " + i);
		}
  }
}

class Runner6 extends Thread {
	public void run() {
		System.out.println(Thread.currentThread().isAlive());
		for(int i=0;i<50;i++) {
			System.out.println("SubThread: " + i);
		}
	}
}
````
### 线程同步 ###
在java语言中，引入了对象互斥锁的概念，保证共享数据操作的完整性。每个对象都对应于一个可称为"互斥锁"的标记，这个标记保证在任一时刻，只能有一个线程访问该对象。  
关键字synchronized来与对象的互斥锁练习，当某个对象synchronized修饰时，表明该对象在任一时刻只能由一个线程访问。
synchronized的使用方法
````
synchronized(this){
	num++
	try {
		Thread.sleep(1);
	} catch (InterruptedException e) {}
	System.out.println(name+"你是第 "+num+"个使用timer的线程");
}
````
synchronized还可以放在方法中声明，表示整个方法为同步方法
````
synchronized public void add (String name){...}
````
````
public class SyncTest implements Runnable{
	Timer timer = new Timer();
	public static void main(String[] args){
		sycnTest1();
	}
	public static void sycnTest1(){
		SyncTest test = new SyncTest();
		Thread t1 = new Thread(test);
		Thread t2 = new Thread(test);
		t1.setName("t1");
		t2.setName("t2");
		t1.start();
		t2.start();
	}

	@Override
	public void run() {
		timer.add(Thread.currentThread().getName());
	}
}
class Timer{
	private static int num = 0;
	public void add (String name){
		synchronized(this){
			num++;
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {}
			System.out.println(name+"你是第 "+num+"个使用timer的线程");
		}
	}
}
````
结果：
````
t1你是第 1个使用timer的线程
t2你是第 2个使用timer的线程
````
**注意**：方法被synchronized只是表示其他线程不能执行这个方法。不代表不能用里面的变量。如果在执行过程中，这个参数的值改变了，会相应的修改参数的值。
````
public static void main(String[] args){
		SyncTest test = new SyncTest();
		Thread t = new Thread(test);
		t.start();
		test.m2();
		System.out.println("b = "+test.b);
	}

	@Override
	public void run() {
		m1();
	}
	
	int b = 100;
	public synchronized void m1(){
		b = 2000;
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("m1-b = "+b);
	}
	public synchronized void m2(){
		b = 1000;
		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("m2-b = "+b);
	}
````
运行结果
````
m2-b = 1000
b = 1000
m1-b = 2000
````
以上程序表示在运行过程中，因为m2()运行速度比较快，先拿到锁，所以先运行m2(),再运行m1()。

生产组，消费者案例模拟线程同步
````
public class SyncTest{
	public static void main(String[] args){
		Kuang kuang = new Kuang();
		Producer cooker = new Producer(kuang,"cooker");
		Consumer consumer = new Consumer(kuang, "consumer");
		new Thread(cooker).start();
		new Thread(consumer).start();
	}
}

class ManTou{
	String name;
	public ManTou(String name){
		this.name = name;
	}
	public String getName(){
		return name;
	}
}

class Kuang{
	int index = 0;
	ManTou[] attrMt = new ManTou[6]; 
	public synchronized void push(ManTou mt){
		if(index == attrMt.length){
			try {
				this.wait();//Object类中的wait方法。锁定在当前对象的线程停止住
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.notify();
		attrMt[index] = mt;
		System.out.println("push mantou : "+mt.name);
		index++;
	}
	public synchronized ManTou get(){
		if(index == 0){
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.notify();
		index--;
		return attrMt[index];
	}
}

//生产类
class Producer implements Runnable{
	Kuang kuang = null;
	String name = null;
	public Producer(Kuang kuang,String name){
		this.kuang = kuang;
		this.name = name;
	}
	@Override
	public void run() {
		for(int i=0;i<20;i++){
			String mtn = this.name+":"+i;
			ManTou mt = new ManTou(mtn);
			kuang.push(mt);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

//消费类
class Consumer implements Runnable{
	Kuang kuang = null;
	String name = null;
	Consumer(Kuang kuang,String name){
		this.kuang = kuang;
		this.name = name;
	}
	@Override
	public void run() {
		for(int i=0;i<20;i++){
			ManTou mt = kuang.get();
			String mtn = mt.name;
			System.out.println(this.name+ " get "+ mtn);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
````
运行结果：
````
push mantou : cooker:0
consumer get cooker:0
push mantou : cooker:1
consumer get cooker:1
push mantou : cooker:2
consumer get cooker:2
push mantou : cooker:3
consumer get cooker:3
push mantou : cooker:4
consumer get cooker:4
push mantou : cooker:5
consumer get cooker:5
push mantou : cooker:6
consumer get cooker:6
push mantou : cooker:7
consumer get cooker:7
push mantou : cooker:8
consumer get cooker:8
push mantou : cooker:9
consumer get cooker:9
push mantou : cooker:10
consumer get cooker:10
push mantou : cooker:11
consumer get cooker:11
push mantou : cooker:12
consumer get cooker:12
push mantou : cooker:13
consumer get cooker:13
push mantou : cooker:14
consumer get cooker:14
push mantou : cooker:15
consumer get cooker:15
push mantou : cooker:16
consumer get cooker:16
push mantou : cooker:17
consumer get cooker:17
push mantou : cooker:18
consumer get cooker:18
push mantou : cooker:19
consumer get cooker:19
````