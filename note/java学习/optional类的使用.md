````
public static void main(String[] args){
    Dog dog1 = new Dog();
    Dog dog2 = new Dog("tom",3,"blue");
    String test1 = "this is test message";
    String test2 = null;
    String result1 = Optional.ofNullable(test1).orElse("other");
    String result2 = Optional.ofNullable(test2).orElse("other");
    System.out.println(result1);
    System.out.println(result2);
    String name1 = Optional.of(dog1).map(Dog::getName).orElse("no dog");
    String name2 = Optional.of(dog2).map(Dog::getName).orElse("no dog");
    System.out.println(name1);
    System.out.println(name2);
}
````
运行结果：
````
this is test message
other
no dog	
tom
````