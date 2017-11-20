### java反射机制 ###
在java运行时环境中，对于任意一个类，知道这个类有哪些属性和方法。对于任意一个对象，调用它的任意一个方法。这种动态获取类的信息以及动态调用对象的方法的功能来自于java语言的反射。
java反射机制主要提供一下功能：
- 在运行时判断任意一个对象所属的类。
- 在运行时构造任意一个类的对象。
- 在运行时判断任意一个类所具有的成员变量和方法。
- 在运行时调用任意一个对象的方法。

reflection是java被视为动态语言的一个关键性质。这个机制允许程序在运行时透过reflection APIs取得热呢一个已知名称的class的内部信息，包括其modifiers(诸如public，static等等)、superclass(例如Object)、实现interfaces(例如Serializable),也包括fields和methods的所有信息，并可于运行时改变fields内容或调用methods

在jdk中，主要由以下类来实现java反射机制，这些类都位于java.lang.reflect包中
- Class类：代表一个类
- Field类：代表类的成员变量（成员变量也称为类的属性）
- Method类：代表类的方法
- Constructor类：代表类的构造方法
- Array类：提供了动态创建数组，以及访问数组的元素的静态方法

通过Class类获取对象
````
public static void getMethod(){
		try {
			Class<?> classMethod = Class.forName("java.lang.String");
			Method[] methods = classMethod.getDeclaredMethods();
			for(Method method:methods){
				System.out.println(method);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
````
运行结果：
````
public boolean java.lang.String.equals(java.lang.Object)
public java.lang.String java.lang.String.toString()
public int java.lang.String.hashCode()
public int java.lang.String.compareTo(java.lang.String)
public int java.lang.String.compareTo(java.lang.Object)
public int java.lang.String.indexOf(java.lang.String,int)
public int java.lang.String.indexOf(java.lang.String)
public int java.lang.String.indexOf(int,int)
public int java.lang.String.indexOf(int)
static int java.lang.String.indexOf(char[],int,int,char[],int,int,int)
static int java.lang.String.indexOf(char[],int,int,java.lang.String,int)
public static java.lang.String java.lang.String.valueOf(int)
public static java.lang.String java.lang.String.valueOf(long)
public static java.lang.String java.lang.String.valueOf(float)
public static java.lang.String java.lang.String.valueOf(boolean)
public static java.lang.String java.lang.String.valueOf(char[])
public static java.lang.String java.lang.String.valueOf(char[],int,int)
public static java.lang.String java.lang.String.valueOf(java.lang.Object)
public static java.lang.String java.lang.String.valueOf(char)
public static java.lang.String java.lang.String.valueOf(double)
public char java.lang.String.charAt(int)
private static void java.lang.String.checkBounds(byte[],int,int)
public int java.lang.String.codePointAt(int)
public int java.lang.String.codePointBefore(int)
public int java.lang.String.codePointCount(int,int)
public int java.lang.String.compareToIgnoreCase(java.lang.String)
public java.lang.String java.lang.String.concat(java.lang.String)
public boolean java.lang.String.contains(java.lang.CharSequence)
public boolean java.lang.String.contentEquals(java.lang.CharSequence)
public boolean java.lang.String.contentEquals(java.lang.StringBuffer)
public static java.lang.String java.lang.String.copyValueOf(char[])
public static java.lang.String java.lang.String.copyValueOf(char[],int,int)
public boolean java.lang.String.endsWith(java.lang.String)
public boolean java.lang.String.equalsIgnoreCase(java.lang.String)
public static java.lang.String java.lang.String.format(java.util.Locale,java.lang.String,java.lang.Object[])
public static java.lang.String java.lang.String.format(java.lang.String,java.lang.Object[])
public void java.lang.String.getBytes(int,int,byte[],int)
public byte[] java.lang.String.getBytes(java.nio.charset.Charset)
public byte[] java.lang.String.getBytes(java.lang.String) throws java.io.UnsupportedEncodingException
public byte[] java.lang.String.getBytes()
public void java.lang.String.getChars(int,int,char[],int)
void java.lang.String.getChars(char[],int)
private int java.lang.String.indexOfSupplementary(int,int)
public native java.lang.String java.lang.String.intern()
public boolean java.lang.String.isEmpty()
public static java.lang.String java.lang.String.join(java.lang.CharSequence,java.lang.CharSequence[])
public static java.lang.String java.lang.String.join(java.lang.CharSequence,java.lang.Iterable)
public int java.lang.String.lastIndexOf(int)
public int java.lang.String.lastIndexOf(java.lang.String)
static int java.lang.String.lastIndexOf(char[],int,int,java.lang.String,int)
public int java.lang.String.lastIndexOf(java.lang.String,int)
public int java.lang.String.lastIndexOf(int,int)
static int java.lang.String.lastIndexOf(char[],int,int,char[],int,int,int)
private int java.lang.String.lastIndexOfSupplementary(int,int)
public int java.lang.String.length()
public boolean java.lang.String.matches(java.lang.String)
private boolean java.lang.String.nonSyncContentEquals(java.lang.AbstractStringBuilder)
public int java.lang.String.offsetByCodePoints(int,int)
public boolean java.lang.String.regionMatches(int,java.lang.String,int,int)
public boolean java.lang.String.regionMatches(boolean,int,java.lang.String,int,int)
public java.lang.String java.lang.String.replace(char,char)
public java.lang.String java.lang.String.replace(java.lang.CharSequence,java.lang.CharSequence)
public java.lang.String java.lang.String.replaceAll(java.lang.String,java.lang.String)
public java.lang.String java.lang.String.replaceFirst(java.lang.String,java.lang.String)
public java.lang.String[] java.lang.String.split(java.lang.String)
public java.lang.String[] java.lang.String.split(java.lang.String,int)
public boolean java.lang.String.startsWith(java.lang.String,int)
public boolean java.lang.String.startsWith(java.lang.String)
public java.lang.CharSequence java.lang.String.subSequence(int,int)
public java.lang.String java.lang.String.substring(int)
public java.lang.String java.lang.String.substring(int,int)
public char[] java.lang.String.toCharArray()
public java.lang.String java.lang.String.toLowerCase(java.util.Locale)
public java.lang.String java.lang.String.toLowerCase()
public java.lang.String java.lang.String.toUpperCase()
public java.lang.String java.lang.String.toUpperCase(java.util.Locale)
public java.lang.String java.lang.String.trim()

````

通过.class获取对象
````
package com.lee.test;

import java.lang.reflect.Method;

public class InvokeTest {
	public static void main(String[] args){
		getMehtodsInvoke();
	}
	public static void getMehtodsInvoke(){
		Class<?> classType = InvokeTest.class;//获得InvokeTest类所对应的Class对象
		Method[] methods = classType.getDeclaredMethods();
		for(Method method:methods){
			System.out.println(method);
		}
	}
	public static void getMethodsClass(){
		try {
			Class<?> classType = Class.forName("com.lee.test.InvokeTest");
			Method[] methods = classType.getDeclaredMethods();
			for(Method method:methods){
				System.out.println(method);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	public int add(int item1,int item2){
		return item1 + item2;
	}
	public int cheng(int item1,int item2){
		return item1 * item2;
	}
}

````
运行结果：
````
public static void com.lee.test.InvokeTest.main(java.lang.String[])
public int com.lee.test.InvokeTest.add(int,int)
public static void com.lee.test.InvokeTest.getMehtodsInvoke()
public static void com.lee.test.InvokeTest.getMethodsClass()
public int com.lee.test.InvokeTest.cheng(int,int)

````

调用通过.class获取对象的方法
````
public class InvokeTest {
	public static void main(String[] args) throws Exception{
		getMethodsInvoke();
	}
	public static void getMethodsInvoke() throws Exception{
		Class<?> classType = Class.forName("com.lee.test.InvokeTest");
		Object invokeTest = classType.newInstance();//以上两行代码等价于 InvokeTest invokeTest = new InvokeTest();构造一个不带参数的构造方法
		Method addMethod = classType.getMethod("add", new Class[]{int.class,int.class});//获取add方法,addMethod对象，就是add方法所对应的Method对象
		Object result = addMethod.invoke(invokeTest, new Object[]{100,200});//invode里面表示要调用那个对象的方法，object表示传入的参数
		//以上代码等价于 invokeTest.addd(100,200);
		
		Method echoMethod = classType.getMethod("echoMethod", new Class[]{int.class,String.class});
		Object resultString = echoMethod.invoke(invokeTest, new Object[]{100,"30"});
		//以上代码等价于 invokeTest.addd(100,"30");
		System.out.println((Integer)result);
		System.out.println((String)resultString);
	} 
	public int add(int item1,int item2){
		return item1 + item2;
	}
	public String echoMethod(int item1,String item2){
		return item1+item2;
	}
}

````
运行结果：
````
300
10030
````

通过反射获取类的对象的步骤
1. 通过Class获取Class对象；
2. 想获得方法，则获得Method对象；想获得属性，则获得field对象；
3. Method可以通过invoke()去操纵方法
4. Field操作属性


````
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectTest {

	public static void main(String[] args) throws Exception {
		Customer customer = new Customer();
		customer.setAge(12);;
		customer.setId(new Long(31));
		customer.setName("tom");
		Customer customerCopy = (Customer) new ReflectTest().copy(customer);
		System.out.println(customerCopy.getAge()+" "+customerCopy.getName()+" "+customerCopy.getId());
	}
	public Object copy(Object object) throws Exception{
		Class<?> classType = object.getClass();
		System.out.println(classType.getName());
		Object objectCopy = classType.getConstructor(new Class[]{}).newInstance(new Object[]{});
		//等价于 Object objectCopy = classType.newInstance();//通过构造方法，获得一个实例
		//getConstructor(new Class[]{}):无参数的构造方法
		//newInstance(new Object[]{}):获得实例
		
		//获得对象的所有属性
		Field[] fields = classType.getDeclaredFields();
		for(Field field:fields){
			String fieldName = field.getName();
			//获得第一个字母，并且转换成大写
			String firstLetter = fieldName.substring(0,1).toUpperCase();
			//获得和属性对应的get、set方法的名字
			String getMethodName = "get" + firstLetter + fieldName.substring(1);
			String setMethodName = "set" + firstLetter + fieldName.substring(1);
			//获取set、get方法
			Method getMethod = classType.getMethod(getMethodName, new Class[]{});
			Method setMethod = classType.getMethod(setMethodName, new Class[]{field.getType()});
			//获得传进来对象的值
			Object value = getMethod.invoke(object, new Class[]{});
			System.out.println(fieldName + ":" + value);
			//把传进来对象的值通过set方法设置到copy对象的
			setMethod.invoke(objectCopy, new Object[]{value});
		}
		return objectCopy;
	}
}

class Customer{
	private String name;
	private int age;
	private Long id;
	public Customer(){}
	public String getName() {return name;}
	public void setName(String name) {this.name = name;}
	public int getAge() {return age;}
	public void setAge(int age) {this.age = age;}
	public Long getId() {return id;}
	public void setId(Long id) {this.id = id;}
}
````
运行结果：
````
com.lee.test.Customer
name:tom
age:12
id:31
12 tom 31
````

通过反射的方式操纵数组
java.lang.Array类提供了动态创建和访问数组元素的各种静态方法。
方法1
````
public class ArrayTest1 {
    public static void main(String[] args) throws Exception{
        arrayTest();
    }
    public static void arrayTest() throws Exception{
        Class<?> stringType = Class.forName("java.lang.String");//获取String的class
        //通过Array的静态方法newInstance，创建一个stringType类型的长度为10的数组
        Object array = Array.newInstance(stringType, 10);
        Array.set(array,5,"helloworld");//对array数组的第5个元素进行设值
        String str = (String)Array.get(array,5);//取出array数组中的第5个元素，并且转换为String类型
        System.out.println(str);
    }
}
````
运行结果：
````
helloworld
````
使用Array类里面的静态方法创建一维以及多维数组
````
public class ArrayTest2 {
    public static void main(String[] args) throws Exception{
        arrayTest2();
    }
    public static void arrayTest2() throws Exception {
        int[] dims = new int[]{5,10,15};//创建一个{5,10,15}的数组
        Object array = Array.newInstance(Integer.TYPE,dims);//创建一个5*10*15的三维数组
        Object arrayObject = Array.get(array,3);//获取array的第3个元素，是一个二维数组
//        Class<?> cls = arrayObject.getClass().getComponentType();//获得arrayObject的组件类型
        Object arrayObject2 = Array.get(arrayObject,5);//获取arrayObject的第5个元素，是一个一维数组
        Array.set(arrayObject2,10,37);
        //普通取出值的方式
        int[][][] arrayCast = (int[][][])array;
        System.out.println(arrayCast[3][5][10]);
    }
}

````
运行结果：
````
37
````

- Class class十分特殊。他和一般classes一样继承自Object，其实体用以表达java程序运行时的classes和interface，也用来表达enum、array、primitive java types
- （boolean,byte,char,short,int,long,float,double）以及关键词void。当一个class被加载，或当加载器（class loader）的defineClass（）被jvm调用，jvm便自动产生一个class object，不能借由"修改java标准库源码"来观察class objcet的实际生成时机（例如在class的constructor内添加一个println()）,因为class并没有public constructor.
- class是reflection起源。针对任何想探勘的class，唯有先为他产生一class object，接下来才能经由后者唤起为数十多个的reflection apis

#### class object的取得途径 ####
- java允许从多种途径为一个class生成对应的class object

|class object诞生管道|示例|
|-|-|
|运用getClass()  注：每个class都有此函数|String str = "abc";  Class c1 = str.getClass();|
|运用 Class.getSuperclass()//获得父类所对应的对象|Button b = new Button();  Class c1 = b.getClass();  Class c2 = c1.getSuperclass();|
|运用static method Class.forName|Class c1 =  Class.forName("java.lang.String");   Class c2 = Class.forName("java.awt.Button");   Class c3 = Class.forName("java.util.LinkedList$Entry");  Class c4 = Class.forName("I");  Class c5 = Class.forName("[I");|
|运用.class语法|Class c1 = String.class;  Class c2 = java.awt.Button.class;  Class c3 = Main.InnerClass.class;  Class c4 = int.class;  Class c5 = int[].class;|
|运用primitive wrapper classes 的TYPE语法(原生对象)|Class c1 = Boolean.TYPE;  Class c2 = Byte.TYPE;  Class c3 = Character.TYPE;  Class c4 = Short.TYPE;  Class c5 = Integer.TYPE;  Class c6 = Long.TYPE;  Class c7 = Float.TYPE; Class c8 = Double.TYPE;  Class c9 = Void.TYPE;

 
#### 运行时生成instances ####
- 欲生成对象实体，在recflection动态机制中有两种做法，一个针对“无自变量ctor”，一个针对“带参数ctor”。如果欲调用的是“带参数ctor”就比较麻烦，不再调用Class的newInstance().而是调用Constructor的newInstance().

例如：ctor:构造方法
1. 准备一个Class[] 作为ctor的参数类型
2. 然后以此为自变量调用getConstructor(),获得一个专属ctor。
3. 再准备一个Object[]作为ctor实参值,调用上述专属ctor的newInstance()。

不带参数的
````
public class Test {
    public static void main(String[] args)throws Exception {
        forName();
    }
    public static void forName() throws Exception {
        Class<?> c = Class.forName("com.lee.DynTest");
        Object object = null;
        object = c.newInstance();//不带自变量
        System.out.println(object);
    }
}
class DynTest{}
````
运行结果：
````
com.lee.DynTest@4554617c
````

带参数的构造方法：
````
public class Test {
    public static void main(String[] args)throws Exception {
        forName();
    }
    public static void forName() throws Exception {
        Class c = Class.forName("com.lee.DynTest");
        Class[] pTypes = new Class[]{double.class,int.class};
        Constructor ctor = c.getConstructor(pTypes);//指定parameter list，便可以获得特定构造方法
        Object object = null;
        Object[] args = new Object[]{3.1415926,125};
        object = ctor.newInstance(args);
        // 把两步合1：Constractor con = c.getConstructor(new Class[]{double.class,int.class});
        // con.newInstance(new Object[]{3.1415926,125});
        DynTest dynTest = (DynTest)object;
        System.out.println(dynTest.getI());
    }
}
class DynTest{
    private double d;
    private int i;
    public DynTest(double d,int i){
        this.d = d;
        this.i = i;
    }
    public int getI(){
        return i;
    }object
}

````
运行结果：
````
125
````

#### 运行时调用method ####

这个动作和上述调用带参数ctor类似
1. 准备一个Class[]作为参数类型
2. 然后以此为自变量调用getMethod(),获得特定的Method Object
3. 接下来准备一个Object[]放置自变量，然后调用上述所得特定Method objec的invoke();

````
public class Test {
    public static void main(String[] args)throws Exception {
        forName();
    }
    public static void forName() throws Exception {
        Class c = Class.forName("com.lee.DynTest");
        Class[] pTypes = new Class[]{double.class,int.class};
        Constructor ctor = c.getConstructor(pTypes);//指定parameter list，便可以获得特定构造方法
        Object object = null;
        Object[] args = new Object[]{3.1415926,125};
        object = ctor.newInstance(args);
        // 把两步合1：Constractor con = c.getConstructor(new Class[]{double.class,int.class});
        // con.newInstance(new Object[]{3.1415926,125});
        Method getI = c.getMethod("getI");//获得getI()方法
        Object o = getI.invoke(object);//操作方法invoke()，获得对应的o
        System.out.println(o);
    }
}
class DynTest{
    private double d;
    private int i;
    public DynTest(double d,int i){
        this.d = d;
        this.i = i;
    }
    public int getI(){
        return i;
    }
}

````
运行结果：
````
125
````

#### 运行时修改field内容 ####
1. 调用Class的getField(),并指定field名称
2. 获得特定的Field object之后便可直接调用Field的get(),和set方法

````
import java.lang.reflect.Field;

public class Test {
    public static void main(String[] args)throws Exception {
        forName();
    }
    public static void forName() throws Exception {
        Class c = Class.forName("com.lee.DynTest");
        DynTest dynTest = new DynTest();
        Field f = c.getField("d");
        f.set(dynTest,125.4);
        System.out.println(f.get(dynTest));
    }
}
class DynTest{
    public double d = 3.25;
    public int i = 10;
    public int getI(){
        return i;
    }
}
````
运行结果：
````
125.4
````

#### 修改private修饰的field ####
````
import java.lang.reflect.Field;

public class ReflectionTest {
    public static void main(String[] args) throws Exception{
        test();
    }
    public static void test() throws Exception {
        PrivateTest privateTest = new PrivateTest();
        Class<?> classType = PrivateTest.class;
        Field nameType = classType.getDeclaredField("name");
        nameType.setAccessible(true);//压制java的访问的控制检查，不再检查是否private
        System.out.println(nameType.get(privateTest));
        nameType.set(privateTest,"world");
        System.out.println(nameType.get(privateTest));
    }
}

````
````
public class PrivateTest {
    private String name = "hello";
    public String getName(){
        return name;
    }
}

````
运行结果：
````
hello
world
````




