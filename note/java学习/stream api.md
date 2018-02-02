stream api

### 过滤 ###
#### distinct ####
去重
````
public static void main(String[] args){
        List<Dog> dogList = getDog();
        System.out.println("before distinct number is :"+dogList.size());
        List<Dog> dogList1 = dogList.stream().distinct().collect(Collectors.toList());
        System.out.println("after distinct number is :"+dogList1.size());
    }

public static List<Dog> getDog(){
    Dog dog1 = new Dog("tom1",1,"yellow");
    Dog dog2 = new Dog("tom2",2,"blue");
    Dog dog3 = new Dog("tom3",3,"black");
    Dog dog4 = new Dog("tom4",4,"red");
    Dog dog5 = new Dog("tom5",5,"yellow");
    Dog dog6 = new Dog("tom1",6,"yellow");
    Dog dog7 = new Dog("tom2",2,"blue");
    List<Dog> dogList = new ArrayList<>();
    dogList.add(dog1);
    dogList.add(dog2);
    dogList.add(dog6);
    dogList.add(dog4);
    dogList.add(dog3);
    dogList.add(dog5);
    dogList.add(dog7);
    return dogList;
}
````
运行结果：
````
before distinct number is :7
after distinct number is :6
````
#### fiter ####
过滤
````
public static void main(String[] args){
    List<Dog> dogList = getDog();
    List<Dog> dogListDistinct = dogList.stream().distinct().collect(Collectors.toList());
    List<Dog> dogListFilter = dogListDistinct.stream().filter(o -> o.getAge() >2).collect(Collectors.toList());
    System.out.print("dogs age are : {");
    for(Dog dog:dogListFilter){
        System.out.print(dog.getAge() + ";");
    }
    System.out.println("}");
}
````
运行结果：
````
dogs age are : {6;4;3;5;}
````
根据key过滤：
````
public static void main(String[] args){
    List<Dog> dogList = getDog();
    System.out.print("dogs age are : {");
    for(Dog dog:dogList){
        System.out.print(dog.getFear() + ";");
    }
    System.out.println("}");
    List<Dog> dogListSkip = dogList.stream()
            .filter(distinctByKey(o1 -> o1.getFear()))
            .collect(Collectors.toList());
    System.out.print("dogs age are : {");
    for(Dog dog:dogListSkip){
        System.out.print(dog.getFear() + ";");
    }
    System.out.println("}");
}
public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
    Map<Object, Boolean> seen = new ConcurrentHashMap<>();
    return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
}
````
运行结果：
````
dogs age are : {yellow;blue;yellow;red;black;yellow;blue;}
dogs age are : {yellow;blue;red;black;}
````

### 变换 ###
#### flatMap ####
#### map ####
只提取一个元素
````
public static void main(String[] args){
    List<Dog> dogList = getDog();
    System.out.print("dogs age are : {");
    for(Dog dog:dogList){
        System.out.print(dog.getFear() + ";");
    }
    System.out.println("}");
    List<String> dogListSkip = dogList.stream()
            .map(o ->o.getFear().toUpperCase())
            .collect(Collectors.toList());
    System.out.print("dogs age are : {");
    for(String dog:dogListSkip){
        System.out.print(dog + ";");
    }
    System.out.println("}");
}
````
运行结果：
````
dogs age are : {yellow;blue;yellow;red;black;yellow;blue;}
dogs age are : {YELLOW;BLUE;YELLOW;RED;BLACK;YELLOW;BLUE;}
````
操作全部元素
````
public static void main(String[] args){
    List<Dog> dogList = getDog();
    System.out.print("dogs age are : {");
    for(Dog dog:dogList){
        System.out.print(dog.getFear() + ";");
    }
    System.out.println("}");
    System.out.print("dogs age are : {");
    List<String> dogStringList = dogList.stream().map(dog -> dog.toString()).collect(Collectors.toList());
    for(String dog:dogStringList){
        System.out.print(dog + ";");
    }
    System.out.println("}");
    List<String> dogListSkip = dogStringList.stream()
            .map(String::toUpperCase)
            .collect(Collectors.toList());
    System.out.print("dogs age are : {");
    for(String dog:dogListSkip){
        System.out.print(dog + ";");
    }
    System.out.println("}");
}
````
运行结果：
````
dogs age are : {yellow;blue;yellow;red;black;yellow;blue;}
dogs age are : {name:tom1age:1fear:yellow;name:tom2age:2fear:blue;name:tom1age:6fear:yellow;name:tom4age:4fear:red;name:tom3age:3fear:black;name:tom5age:5fear:yellow;name:tom2age:2fear:blue;}
dogs age are : {NAME:TOM1AGE:1FEAR:YELLOW;NAME:TOM2AGE:2FEAR:BLUE;NAME:TOM1AGE:6FEAR:YELLOW;NAME:TOM4AGE:4FEAR:RED;NAME:TOM3AGE:3FEAR:BLACK;NAME:TOM5AGE:5FEAR:YELLOW;NAME:TOM2AGE:2FEAR:BLUE;}

````
### 拆分合并流 ###
#### peek ####

#### concat ####
````
public static void main(String[] args){
    List<Dog> dogList1 = getDog();
    List<Dog> dogList2 = getDog();
    Stream<Dog> stream1 = dogList1.stream();
    Stream<Dog> stream2 = dogList2.stream();
    List<Dog> dogs = Stream.concat(stream1,stream2).collect(Collectors.toList());
    System.out.println(dogs.size());
}
````
运行结果：
````
14
````
注：只能合并两个Stream流
#### limit ####
限制，返回 Stream 的前面 n 个元素
````
public static void main(String[] args){
    List<Dog> dogList = getDog();
    List<Dog> dogListLimit = dogList.stream().limit(4).collect(Collectors.toList());
    System.out.print("dogs age are : {");
    for(Dog dog:dogListLimit){
        System.out.print(dog.getAge() + ";");
    }
    System.out.println("}");
}
````
运行结果:
````
dogs age are : {1;2;6;4;}
````
#### skip ####
跳过Stream前的n个元素
````
public static void main(String[] args){
    List<Dog> dogList = getDog();
    List<Dog> dogListSkip = dogList.stream().skip(4).collect(Collectors.toList());
    System.out.print("dogs age are : {");
    for(Dog dog:dogListSkip){
        System.out.print(dog.getAge() + ";");
    }
    System.out.println("}");
}
````
运行结果：
````
dogs age are : {3;5;2;}
````
### 排序 ###
#### sorted ####
排序
````
public static void main(String[] args){
    List<Dog> dogList = getDog();
    List<Dog> dogListSort = dogList.stream()
            .sorted((o1, o2) -> Integer.compare(o1.getAge(),o2.getAge()))
            .collect(Collectors.toList());
    System.out.print("dogs age are : {");
    for(Dog dog:dogListSort){
        System.out.print(dog.getAge() + ";");
    }
    System.out.println("}");
}
````
运行结果：
````
dogs age are : {1;2;2;3;4;5;6;}
````
### Stream结果处理 ###

#### 遍历forEach ####


#### 聚合reduce ####
reduce()可以实现从一组数据中生成一个数据,这个方法有三种形式：
````
Optional<T> reduce(BinaryOperator<T> accumulator)
T reduce(T identity, BinaryOperator<T> accumulator)
<U> U reduce(U identity,BiFunction<U,? super T,U> accumulator,BinaryOperator<U> combiner)
````
````
public static void main(String[] args){
    List<Dog> dogList = getDog();
    List<Integer> dogAge = dogList.stream()
            .map(o->Integer.valueOf(o.getAge()))
            .collect(Collectors.toList());
    Optional<Integer> optional = dogAge.stream().reduce((sum, i)-> sum = sum+i);
    int result = optional.get();
    System.out.println(result);
}
````
运行结果：
````
23
````

#### Optional类型 ####
Optional是一种容器，可以存储一些值和null。利用这个类，可以进行null的判断，能够有效的避免NullPointerException
1. get(),可以拿到Optional中的值，没有值则抛出空指针异常
2. isPresent(),有非空的值，返回true，否则返回false
3. ifPresent(),public void ifPresent(Consumer<? super T> consumer)这个方法中需要一个Consumer接口。如果有非空的值，就执行指定的Consumer的方法来处理这个非空的值；如果为空，则啥都不做


#### 收集collect ####
collect()可以将Stream流转变成集合



代码来源：https://www.cnblogs.com/qdwyg2013/p/5631057.html
````
package com.mavsplus.java8.turtorial.streams;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * java.util.Stream使用例子
 * 
 * <pre>
 * java.util.Stream表示了某一种元素的序列，在这些元素上可以进行各种操作。Stream操作可以是中间操作，也可以是完结操作。
 * 完结操作会返回一个某种类型的值，而中间操作会返回流对象本身，并且你可以通过多次调用同一个流操作方法来将操作结果串起来。
 * Stream是在一个源的基础上创建出来的，例如java.util.Collection中的list或者set（map不能作为Stream的源）。
 * Stream操作往往可以通过顺序或者并行两种方式来执行。
 * </pre>
 * 
 * public interface Stream<T> extends BaseStream<T, Stream<T>> {
 * <p>
 * 可以看到Stream是一个接口,其是1.8引入
 * 
 * <p>
 * Java 8中的Collections类的功能已经有所增强，你可以之直接通过调用Collections.stream()或者Collection.
 * parallelStream()方法来创建一个流对象
 * 
 * @author landon
 * @since 1.8.0_25
 */
public class StreamUtilExample {

    private List<String> stringList = new ArrayList<>();

    public StreamUtilExample() {
        init();
    }

    private void init() {
        initStringList();
    }

    /**
     * 初始化字符串列表
     */
    private void initStringList() {
        stringList.add("zzz1");
        stringList.add("aaa2");
        stringList.add("bbb2");
        stringList.add("fff1");
        stringList.add("fff2");
        stringList.add("aaa1");
        stringList.add("bbb1");
        stringList.add("zzz2");
    }

    /**
     * Filter接受一个predicate接口类型的变量，并将所有流对象中的元素进行过滤。该操作是一个中间操作，
     * 因此它允许我们在返回结果的基础上再进行其他的流操作
     * （forEach）。ForEach接受一个function接口类型的变量，用来执行对每一个元素的操作
     * 。ForEach是一个中止操作。它不返回流，所以我们不能再调用其他的流操作
     */
    public void useStreamFilter() {
        // stream()方法是Collection接口的一个默认方法
        // Stream<T> filter(Predicate<? super T>
        // predicate);filter方法参数是一个Predicate函数式接口并继续返回Stream接口
        // void forEach(Consumer<? super T> action);foreach方法参数是一个Consumer函数式接口

        // 解释:从字符串序列中过滤出以字符a开头的字符串并迭代打印输出
        stringList.stream().filter((s) -> s.startsWith("a")).forEach(System.out::println);
    }

    /**
     * Sorted是一个中间操作，能够返回一个排过序的流对象的视图。流对象中的元素会默认按照自然顺序进行排序，
     * 除非你自己指定一个Comparator接口来改变排序规则.
     * 
     * <p>
     * 一定要记住，sorted只是创建一个流对象排序的视图，而不会改变原来集合中元素的顺序。原来string集合中的元素顺序是没有改变的
     */
    public void useStreamSort() {
        // Stream<T> sorted();返回Stream接口
        // 另外还有一个 Stream<T> sorted(Comparator<? super T>
        // comparator);带Comparator接口的参数
        stringList.stream().sorted().filter((s) -> s.startsWith("a")).forEach(System.out::println);

        // 输出原始集合元素，sorted只是创建排序视图，不影响原来集合顺序
        stringList.stream().forEach(System.out::println);
    }

    /**
     * map是一个对于流对象的中间操作，通过给定的方法，它能够把流对象中的每一个元素对应到另外一个对象上。
     * 下面的例子就演示了如何把每个string都转换成大写的string.
     * 不但如此，你还可以把每一种对象映射成为其他类型。对于带泛型结果的流对象，具体的类型还要由传递给map的泛型方法来决定。
     */
    public void useStreamMap() {
        // <R> Stream<R> map(Function<? super T, ? extends R> mapper);
        // map方法参数为Function函数式接口(R_String,T_String).

        // 解释:将集合元素转为大写(每个元素映射到大写)->降序排序->迭代输出
        // 不影响原来集合
        stringList.stream().map(String::toUpperCase).sorted((a, b) -> b.compareTo(a)).forEach(System.out::println);
    }

    /**
     * 匹配操作有多种不同的类型，都是用来判断某一种规则是否与流对象相互吻合的。所有的匹配操作都是终结操作，只返回一个boolean类型的结果
     */
    public void useStreamMatch() {
        // boolean anyMatch(Predicate<? super T> predicate);参数为Predicate函数式接口
        // 解释:集合中是否有任一元素匹配以'a'开头
        boolean anyStartsWithA = stringList.stream().anyMatch((s) -> s.startsWith("a"));
        System.out.println(anyStartsWithA);

        // boolean allMatch(Predicate<? super T> predicate);
        // 解释:集合中是否所有元素匹配以'a'开头
        boolean allStartsWithA = stringList.stream().allMatch((s) -> s.startsWith("a"));
        System.out.println(allStartsWithA);

        // boolean noneMatch(Predicate<? super T> predicate);
        // 解释:集合中是否没有元素匹配以'd'开头
        boolean nonStartsWithD = stringList.stream().noneMatch((s) -> s.startsWith("d"));
        System.out.println(nonStartsWithD);
    }

    /**
     * Count是一个终结操作，它的作用是返回一个数值，用来标识当前流对象中包含的元素数量
     */
    public void useStreamCount() {
        // long count();
        // 解释:返回集合中以'a'开头元素的数目
        long startsWithACount = stringList.stream().filter((s) -> s.startsWith("a")).count();
        System.out.println(startsWithACount);

        System.out.println(stringList.stream().count());
    }

    /**
     * 该操作是一个终结操作，它能够通过某一个方法，对元素进行削减操作。该操作的结果会放在一个Optional变量里返回。
     */
    public void useStreamReduce() {
        // Optional<T> reduce(BinaryOperator<T> accumulator);
        // @FunctionalInterface public interface BinaryOperator<T> extends
        // BiFunction<T,T,T> {

        // @FunctionalInterface public interface BiFunction<T, U, R> { R apply(T
        // t, U u);
        Optional<String> reduced = stringList.stream().sorted().reduce((s1, s2) -> s1 + "#" + s2);

        // 解释:集合元素排序后->reduce(削减 )->将元素以#连接->生成Optional对象(其get方法返回#拼接后的值)
        reduced.ifPresent(System.out::println);
        System.out.println(reduced.get());
    }

    /**
     * 使用并行流
     * <p>
     * 流操作可以是顺序的，也可以是并行的。顺序操作通过单线程执行，而并行操作则通过多线程执行. 可使用并行流进行操作来提高运行效率
     */
    public void useParallelStreams() {
        // 初始化一个字符串集合
        int max = 1000000;
        List<String> values = new ArrayList<>();

        for (int i = 0; i < max; i++) {
            UUID uuid = UUID.randomUUID();
            values.add(uuid.toString());
        }

        // 使用顺序流排序

        long sequenceT0 = System.nanoTime();
        values.stream().sorted();
        long sequenceT1 = System.nanoTime();

        // 输出:sequential sort took: 51921 ms.
        System.out.format("sequential sort took: %d ms.", sequenceT1 - sequenceT0).println();

        // 使用并行流排序
        long parallelT0 = System.nanoTime();
        // default Stream<E> parallelStream() {
        // parallelStream为Collection接口的一个默认方法
        values.parallelStream().sorted();
        long parallelT1 = System.nanoTime();

        // 输出:parallel sort took: 21432 ms.
        System.out.format("parallel sort took: %d ms.", parallelT1 - parallelT0).println();

        // 从输出可以看出：并行排序快了一倍多
    }

    public static void main(String[] args) {
        StreamUtilExample example = new StreamUtilExample();

        example.useStreamFilter();
        example.useStreamMap();
        example.useStreamMatch();
        example.useStreamCount();
        example.useStreamReduce();
        example.useParallelStreams();
    }
}
````






http://www.jianshu.com/p/cbd5713a8f26