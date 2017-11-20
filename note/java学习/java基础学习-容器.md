### 容器的概念 ###
java api所提供的一系列类的实例，用于在程序中存放对象

### 容器api ###
- Collection接口：定义了存取一组对象的方法，其子接口Set和List分别定义了存储方式
	- Set中的数据对象没有顺序，且不可以重复。
	- List中的数据对象有顺序且可以重复
- Map接口定义了存储"键(key)-值(value)映射对"的方法
### Collection接口 ###
Collection接口中所定义的方法
1. int size();
2. boolean isEmpty();
3. boolean contains(object element);//是否包含element
4. boolean add(object element);// 
5. Iterator iterator();
6. boolean containAll(Collection c);//是否包含另一个集合中所有元素
7. boolean addAll(Collection c);
8. boolean removeAll(Collection c);//去除
9. boolean retainAll(Collection c);//求交集
10. Object[] toArray();//转换成对象数组 

容器类对象在调用remove、contains等方法时需要比较对象是否相等，这会涉及到对象类型的equals方法和hashCode方法；对已自定义的类型，需要重写equals和hashCode方法以实现自定义的对象相等规则。
- 注意：相等的对象应该具有相等的hash codes。

增加name类的equals和hashCode方法如下：
````
public boolean equals(Object obj){
	if(obj instanceof Name){
		Name name = (Name) obj;
		return (firstName.equals(name.firstName)&&(lastName.equals(name.lastName)));
	}
}
public int hashCode(){
	return firstName.hashCode();
}
````
### Interator接口 ###
- 所有实现了Collection接口的容器类都有一个iterartor方法用以返回一个实现了Iterator接口的对象。
- Iterator对象称作迭代器，用以方便的实现对容器内元素的遍历操作。
- Iterator接口定义了如下方法：
	- boolean hasNext();//判断游标右边是否有元素
	- Object next();//返回游标右边的元素并将游标移动到下一个位置
	- void remove();//删除游标左边的元素，在执行完next之后，该操作只能执行一次

例子1:
````
public static void main(String[] args) {
	Collection<Name> c = new HashSet<Name>();
	c.add(new Name("f1","l1"));
	c.add(new Name("f2","l2"));
	c.add(new Name("f3","l3"));
	c.add(new Name("f4","l4"));
	Iterator<Name> i = c.iterator();
	while(i.hasNext()){
		Name n = i.next();
		System.out.print(n.getFirstName()+" ");
	}
}
````
输出结果为：
````
f1 f2 f3 f4 
````
例子2：
````
public static void main(String[] args) {
	Collection<Name> c = new HashSet<Name>();
	c.add(new Name("f111","l1"));
	c.add(new Name("f222","l2"));
	c.add(new Name("f3","l3"));
	c.add(new Name("f444","l4"));
	for(Iterator<Name> i = c.iterator();i.hasNext();){
		Name name = i.next();
		if(name.getFirstName().length()>3){
			i.remove();
		}
	}
	System.out.println(c.size());
}
````
输出结果为：
````
1
````
### 增强for循环 ###
除了简单遍历并独处其中的内容外，不建议使用增强for循环

例子：
````
int[] array = {1,2,3,4,5};
for(int item:array){
	System.out.print(item+" ");
}
````
输出结果：
````
1,2,3,4,5,
````
### Set接口 ###
- Set接口是Collection的子接口，Set接口没有提供额外的方法，但实现Set接口的容器类中的与阿奴是没有顺序的，而且不可以重复。
- Set容器可以与数学中"集合"的概念相对应。
- Set容器类有HashSet,TreeSet等
### List接口和Comparable接口 ###
- List接口是Collection的子接口，实现List接口的容器类中的元素是有顺序的，而且可以重复。
- List容器中的元素都对应一个整数型的序号记载其在容器中的位置，可以根据序号存取容器中的元素。
- List容器类有ArrayList，LinkedList等。

提供的方法有
- Object get(int index);
- Object set(int index, Object element);
- void add(int index,Object element);
- Object remove(int index);
- int indexOf(Object o);//元素o在容器中出现的第一个位置
- int lastIndexOf(Object o);//元素o在容器中出现的最后一个位置

#### List常用算法 ####
void sort(List)//对List容器内的元素排序
void shuffle(List)//对List容器内的对象进行随机排列
void reverse(List)//对List容器内的对象进行逆序排列
void fill(List,Object)//用一个特定的对象重写整个List容器
void copy(List dest,List src)//将src List容器内容拷贝到dest List容器
int binarySearch(List,Object)//对于顺序的List容器，采用折半查找的方法查找特定对象

#### Comparable接口 ####
以上算法实现了java.lang.Comparable接口，Comparable接口中只有一个方法：  
public int compareTo(Object obj);
- 返回0表示：this == obj;
- 返回正数表示：this > obj;
- 返回负数表示：this < obj;

实现了Comparable接口的类通过实现bomparaTo方法从而确定该类对象的排序方式。

重写Name类,具有比较大小的方法
````
class Name implements Comparable{
	private String firstName;
	private String lastName;
	public Name(String firstName,String lastName){
		this.firstName = firstName;
		this.lastName = lastName;
	}
	public String getFirstName(){
		return firstName;
	}
	public String getLastName(){
		return lastName;
	}
	public boolean equals(Object obj){
		if(obj instanceof Name){
			Name name = (Name)obj;
			return (firstName.equals(name.firstName)&&lastName.equals(name.lastName));
		}
		return super.equals(obj);
	}
	public int hashCode(){
		return firstName.hashCode();
	}
	@Override
	public int compareTo(Object o) {
		Name name = (Name) o;
		int lastCmp = lastName.compareTo(name.lastName);
		return lastCmp != 0 ? lastCmp :firstName.compareTo(name.firstName);//当lastCmp等于0，返回0；当lastCmp不等于0，返回firstName的比较值
	}
}
````
### Collections类 ###

### Map接口 ###
实现Map接口的类用来存储键-值对。
Map接口的实现类有HashMap和TreeMap等。
Map类中存储的键-值通过键来标识，所以键值不能重复。
提供的方法：
- Object put(Object key,Object value);
- Object get(Object key);
- Object remove(Object key);
- boolean containsKey(Object key);//是不是包含这个key
- boolean containsValue(Object value);//是不是含有这个value
- int size();
- boolean isEmpty();
- void putAll(Map t);//把另外一个map中的所有内容都加进来
- void clear();
