### 传统定时器 ###
````
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TraditionalTimerTest {
    public static void main(String[] args){
        timerTest();
    }
    public static void timerTest(){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("run time");
            }
        },1500,1000);//1000毫秒的周期
        int run = 0;
        while (run<5){
            run ++;
            System.out.println(new Date().getTime());
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
1511357236668
1511357237669
run time
1511357238669
run time
1511357239669
run time
1511357240669
run time
run time
....
````
### 线程池 ###
固定线程池：
````
package com.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolTest {
    public static void main(String[] args){
        poolTest();
    }
    public static void poolTest(){
        ExecutorService threadpool = Executors.newFixedThreadPool(3);//创建一个线程池，固定链接数为3个
//        ExecutorService threadpool = Executors.newCachedThreadPool();//创建动态线程池，自动扩充
//        ExecutorService threadpool = Executors.newSingleThreadExecutor();//只有一个线程，但是能保证，一定有一个线程
        for(int i=0;i<10;i++){
            final int taskId = i;
            threadpool.execute(new Runnable() {
                @Override
                public void run() {
                    for (int j=0;j<5;j++){
                        System.out.println(Thread.currentThread().getName()+
                                " is looping off " + j + " for task of " + taskId);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        System.out.println("all of task have committed");
        threadpool.shutdown();
    }
}

````
运行结果：
````
pool-1-thread-1 is looping off 0 for task of 0
pool-1-thread-2 is looping off 0 for task of 1
pool-1-thread-3 is looping off 0 for task of 2
pool-1-thread-1 is looping off 1 for task of 0
pool-1-thread-2 is looping off 1 for task of 1
pool-1-thread-3 is looping off 1 for task of 2
pool-1-thread-1 is looping off 2 for task of 0
pool-1-thread-2 is looping off 2 for task of 1
pool-1-thread-3 is looping off 2 for task of 2
pool-1-thread-2 is looping off 3 for task of 1
pool-1-thread-1 is looping off 3 for task of 0
pool-1-thread-3 is looping off 3 for task of 2
pool-1-thread-1 is looping off 4 for task of 0
pool-1-thread-2 is looping off 4 for task of 1
pool-1-thread-3 is looping off 4 for task of 2
pool-1-thread-1 is looping off 0 for task of 3
pool-1-thread-2 is looping off 0 for task of 4
pool-1-thread-3 is looping off 0 for task of 5
pool-1-thread-1 is looping off 1 for task of 3
pool-1-thread-2 is looping off 1 for task of 4
pool-1-thread-3 is looping off 1 for task of 5
pool-1-thread-2 is looping off 2 for task of 4
pool-1-thread-1 is looping off 2 for task of 3
pool-1-thread-3 is looping off 2 for task of 5
pool-1-thread-2 is looping off 3 for task of 4
pool-1-thread-1 is looping off 3 for task of 3
pool-1-thread-3 is looping off 3 for task of 5
pool-1-thread-2 is looping off 4 for task of 4
pool-1-thread-1 is looping off 4 for task of 3
pool-1-thread-3 is looping off 4 for task of 5
pool-1-thread-1 is looping off 0 for task of 6
pool-1-thread-2 is looping off 0 for task of 7
pool-1-thread-3 is looping off 0 for task of 8
pool-1-thread-1 is looping off 1 for task of 6
pool-1-thread-2 is looping off 1 for task of 7
pool-1-thread-3 is looping off 1 for task of 8
pool-1-thread-1 is looping off 2 for task of 6
pool-1-thread-2 is looping off 2 for task of 7
pool-1-thread-3 is looping off 2 for task of 8
pool-1-thread-1 is looping off 3 for task of 6
pool-1-thread-2 is looping off 3 for task of 7
pool-1-thread-3 is looping off 3 for task of 8
pool-1-thread-2 is looping off 4 for task of 7
pool-1-thread-1 is looping off 4 for task of 6
pool-1-thread-3 is looping off 4 for task of 8
pool-1-thread-2 is looping off 0 for task of 9
pool-1-thread-2 is looping off 1 for task of 9
pool-1-thread-2 is looping off 2 for task of 9
pool-1-thread-2 is looping off 3 for task of 9
pool-1-thread-2 is looping off 4 for task of 9
````
以上结果可以看出，每次只运行了三个任务，并且此线程池没有关闭
如果想要关闭线程池，则使用shutdown()方法或者立即关闭用shutdownNow();

### Callable&Future ###
- Future取得的结果类型和Callable返回的结果类型必须是一致的，这是通过泛型来实现的
- Callable要采用ExecuorService的submit方法提交，返回的future对象可取消任务
- CompletionService用于提交一组Callable任务，其task方法返回已完成一个Callable任务对应的Future对象。

````
package com.thread;

import java.util.concurrent.*;

public class CallableAndFutureTest {
    public static void main(String[] args) throws Exception {
        test();
    }
    public static void test() throws Exception{
        ExecutorService threadPool = Executors.newSingleThreadExecutor();
        Future<String> future =
        threadPool.submit(
                new Callable<String>() {
                    public String call() throws Exception{
                        Thread.sleep(1000);
                        return "hello";
                    };
                }
        );
        System.out.println("waiting for result");
        System.out.println("get result " + future.get());
    }
}
````
运行结果：
````
waiting for result
get result hello
````
批量返回：
````
package com.thread;

import java.util.Random;
import java.util.concurrent.*;

public class CallableAndFutureTest {
    public static void main(String[] args) throws Exception {
        test();
    }
    public static void test() throws Exception{
        ExecutorService threadPool =  Executors.newFixedThreadPool(10);//创建一个线程池，固定链接数为10个
        CompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(threadPool);
        for(int i=0;i<10;i++){
            final int seq = i;
            completionService.submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    Thread.sleep(new Random().nextInt(5000));//不超过5秒
                    return seq;
                }
            });
        }
        for(int i=0;i<10;i++){
            System.out.println(completionService.take().get());
        }
    }
}
````
运行结果：
````
0
3
9
8
2
6
4
7
5
1
````
### lock与Condition实现线程同步通信 ###
- Lock比传统线程模型中的synchronized方式更加面向对象，与生活中的锁类似，锁本身也应该是一个对象。两个线程执行的代码片段要实现同步互斥的效果，他们必须用同一个Lock对象。锁是上在代表要操作的资源的类的内部方法中，而不是线程代码中
- 读写锁：分为读锁和写锁，多个读锁不互斥，读锁与写锁互斥，写锁与写锁互斥，这是由jvm自己控制的，只需要上号相应的锁即可。如果代码只读数据，可以很多人同时读，但是不能同事些，那就上读锁；如果代码修改数据，只能偶一个人在写，且不能同时读取，就上写锁。
- Condition的功能类似在传统线程记住中的object.wait和object.notify的功能。在等待condition时，允许发生虚假唤醒，这通常作为对基础平台语义的让步。对于大多数应用程序，这带来的实际影响很小，因为condition应该总是在一个循环中被等待，并测试整被等待的状态声明。某个实现可以随意移出可能的虚假唤醒，但建议应用程序员总是嘉定这些虚假唤醒可能发生，因此总是在一个循环中等待。
- 一个锁内部可以有多个condition，即有多路等待和通知。在传统的线程机制中一个监视器对象只能有一路等待和通知，要想实现多路等待和通知，必须嵌套多个同步监视器对象。（如果只有一个condition，两个放的都在等，一旦一个的放进去了，那么它通知可能会导致另一个放接着往下走）

使用lock
````
package com.thread;

import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockTest implements Runnable{
    public static void main(String[] args){
        test();
    }

    public static void test(){
        LockTest lockTest1 = new LockTest();
        LockTest lockTest2 = new LockTest();
        lockTest1.run();
        lockTest2.run();
    }
    public static void output(){
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            System.out.println((new Date()).getTime()+" outputer");
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void run() {
        output();
    }
}
````
运行结果：
````
1511787139989 outputer
1511787141990 outputer
````

````
package com.thread;

import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockTest {
    public static void main(String[] args) {
        final Queue3 q3 = new Queue3();
        for(int i=0;i<3;i++){
            new Thread(){
                public void run(){
                    int index = 0;
                    while(index<5){
                        q3.put(new Random().nextInt(10000));
                        index ++;
                    }
                }

            }.start();
            new Thread(){
                public void run(){
                    int index = 0;
                    while(index<5){
                        q3.get();
                        index ++;
                    }
                }
            }.start();
        }
    }
}

class Queue3{
    private Object data = null;//共享数据，只能有一个线程能写该数据，但可以有多个线程同时读该数据。
    ReadWriteLock rwl = new ReentrantReadWriteLock();
    public void get(){
        rwl.readLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + " be ready to read data!");
            Thread.sleep((long)(Math.random()*1000));
            System.out.println(Thread.currentThread().getName() + "have read data :" + data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally{
            rwl.readLock().unlock();
        }
    }

    public void put(Object data){
        rwl.writeLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + " be ready to write data!");
            Thread.sleep((long)(Math.random()*1000));
            this.data = data;
            System.out.println(Thread.currentThread().getName() + " have write data: " + data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally{
            rwl.writeLock().unlock();
        }
    }
}

````

运行结果：
````
Thread-0 be ready to write data!
Thread-0 have write data: 8061
Thread-0 be ready to write data!
Thread-0 have write data: 9550
Thread-0 be ready to write data!
Thread-0 have write data: 9099
Thread-0 be ready to write data!
Thread-0 have write data: 6205
Thread-2 be ready to write data!
Thread-2 have write data: 8033
Thread-2 be ready to write data!
Thread-2 have write data: 1260
Thread-2 be ready to write data!
Thread-2 have write data: 1366
Thread-2 be ready to write data!
Thread-2 have write data: 8968
Thread-2 be ready to write data!
Thread-2 have write data: 1483
Thread-1 be ready to read data!
Thread-3 be ready to read data!
Thread-3have read data :1483
Thread-1have read data :1483
Thread-4 be ready to write data!
Thread-4 have write data: 3519
Thread-4 be ready to write data!
Thread-4 have write data: 7270
Thread-4 be ready to write data!
Thread-4 have write data: 6487
Thread-5 be ready to read data!
Thread-5have read data :6487
Thread-0 be ready to write data!
Thread-0 have write data: 1588
Thread-3 be ready to read data!
Thread-1 be ready to read data!
Thread-1have read data :1588
Thread-3have read data :1588
Thread-4 be ready to write data!
Thread-4 have write data: 6401
Thread-4 be ready to write data!
Thread-4 have write data: 3099
Thread-5 be ready to read data!
Thread-1 be ready to read data!
Thread-3 be ready to read data!
Thread-1have read data :3099
Thread-1 be ready to read data!
Thread-5have read data :3099
Thread-5 be ready to read data!
Thread-1have read data :3099
Thread-1 be ready to read data!
Thread-3have read data :3099
Thread-3 be ready to read data!
Thread-5have read data :3099
Thread-5 be ready to read data!
Thread-3have read data :3099
Thread-3 be ready to read data!
Thread-5have read data :3099
Thread-5 be ready to read data!
Thread-1have read data :3099
Thread-3have read data :3099
Thread-5have read data :3099
````

可以看出：读和写是分离的