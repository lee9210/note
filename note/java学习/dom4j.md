#### 获取的方式 ####
xml文件：
````
<?xml version="1.0" encoding="UTF-8"?>
<habernate-mapping>
	<class name="java.com.Dom4jTest" table="t_user">
        <property name="userName"></property>
        <property name="password"></property>
	</class>
</habernate-mapping>
````
java代码：
````
public static void SAXTest() throws Exception{
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(new File("xml/user.xml"));
        Element rootElement = document.getRootElement();//获取根节点
        System.out.println(rootElement.getName());
        //通过遍历的方法取得子节点
        for(Iterator i = rootElement.elementIterator();i.hasNext();){
            Element element = (Element) i.next();
            System.out.println(element.getName());
            Iterator j = element.attributeIterator();
            while(j.hasNext()){
            	Attribute attribute = (Attribute) j.next();
            	System.out.println(attribute.getValue());
            }
        }
        //xpath寻找对应节点
        List<Node> list = document.selectNodes("//habernate-mapping/class/property");
        for(Node n:list){
        	System.out.println(n.getName());
        	System.out.println("xpath test:"+n.valueOf("@name"));
        	
        }
        //xpath 获取单个节点(若有多个，获取单个)
        Node node = document.selectSingleNode("//habernate-mapping/class/property");
        System.out.println("sigle test:"+node.valueOf("@name"));
	}
````
运行结果：
````
habernate-mapping
class
java.com.Dom4jTest
t_user
property
xpath test:userName
property
xpath test:password
sigle testuserName
````

