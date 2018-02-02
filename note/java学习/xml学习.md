### xml概念 ###
xml语言--描述事物本身
xsl语言--展现事物表现形式（把xml转换成其他形式，例如具体实例）
dtd（schema）--定义xml语言的语法（规定xml描述的时候必须按照什么格式，什么能加什么不能加）
### xml语法 ###
验证一个xml文档是否正确
1. 格式正确
	1. 编程正确
	2. xml parser
2. 与dtd文档相符合
	1. 编程
	2. xml parser


xml解析器一般会把空格去除
例如：
````
<name>张三</name>
<name> 张三 </name>
````
会把第二个张三前后的空格去掉

PI--process instruction
语法为:
````
<?...?>
<?xml-stysheet href="xxx.xsl" type="text/xsl"?>
<?xml-stysheet href="xxx.css" type="text/css"?>
````
namespace--命名空间
````
<person xmlns="http://www.w3c.org/xxx.dtd"//命名空间来源于xxx.dtd文件
	xmlns:X="http://www.w3c.org/xxxxxx.dtd">//以X开头的命名空间来源于xxxxxx.dtd文件
<name>张三</name>
<X:person>
	<X:name>李四</X:name>
</X:person>

或者
<person xmlns="http://www.w3c.org/xxx.dtd">//person标签里面的所有命名空间都是来源于xxx.dtd文件
	<name>张三</name>
	<persion1 xmlns="http://www.w3c.org/xxxxxx.dtd">//person1标签里面的所有命名空间都来源于xxxxxx.dtd文件
		<name>李四</name>
	</persion1>
</person>
````
#### xml文档结构 ####
1. xml声明
2. xml元素组织数据
3. 可以引入cdata区数据块
4. 注释
5. 处理指令
### xsl基础 ###
xml文件
````
<?xml version="1.0" encoding="gb2312"?>
<?xml-stylesheet style="text/xsl" href="practise.xsl"?>
<icecream_shop>
	<name>冷饮专卖</name>
	<icecream>
		<货号>0001111</货号>
		<品名>吃了吐</品名>
		<价格>56</价格>
		<描述页 网址="www.baidu.com">详细请点这里</描述页>
	</icecream>
	<icecream>
		<货号>0002222</货号>
		<品名>吐了吃</品名>
		<价格>34</价格>
		<描述页 网址="www.163.com">详细请点这里</描述页>
	</icecream>
</icecream_shop>
````
xsl文件
````
<?xml version="1.0" encoding="gb2312"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/TR/WD-xsl">
	<xsl:teplate match="/">
		<html>
			<head><title>结果</title></head>
			<body>
				<div align="center"><p>冰激凌</p></div>
				<div align="center"><p>
					<xsl:value-of select="*/name" />
				</p></div>
			<xsl:apply-templates select="icecream_shop" /><!-- 应用另外一个模板 -->
			</body>
		</html>
	</xsl:template>
	<xsl:template match="icecream_shop">
		<p align="center">
			<table border="1">
				<tr>
					<td>货号</td>
					<td>品名</td>
					<td>价格</td>
					<td>描述页</td>
				</tr>
				<xsl:for-each select="icecream"><!-- for循环，遇到icecream标签就输出下面的内容 -->
					<tr>
						<td><xsl:value-of select="货号" /></td>
						<td><xsl:value-of select="品名" /></td>
						<td><xsl:value-of select="价格" /></td>
						<td>
							<a>
								<xsl:attribute name="href"><!-- 输出一个网址 -->
									<xsl:value-of select="描述页/@网址">
								</xsl:attribute>
								<xsl:value-of select="描述页">
							</a>
					</tr>
				<xsl:for-each>
			</table>
		</p>
	</xsl:template>
</xsl:stylesheet>
````
#### xpath简介 ####
### DTD&Schema ###


````
<?xml version="1.0" encoding="gb2312"?>
<!ELEMENT 丛书 (书*)><!-- 根节点必须为"丛书" 丛书里面是"书"节点，可以出现0次或多次 -->
	<!ELEMENT 书 (名,人+,价*)><!-- 书节点下面有三个子节点，名，人，价。名必须出现1次，人一次或多次，价是0次或多次。如果用逗号隔开，就严格按照名，人价的顺序。如果用空格隔开，则没有顺序关系 -->
		<!ELEMENT 名 (#PCDATA)><!-- 纯文本数据 -->
			<!ATTLIST 人 gender CDATA "male">
		<!ELEMENT 价 (#PCDATA)>
			<!ATTLIST 价 unit (rmb|美元|日元) "rmb"><!-- 在价格这个标签里面，可以有unit属性，但是属性值只能取rmb|美元|日元三者之一 -->

````


#### 语法 ####
ANY


````
<!ELEMENT 元素名 ANY>
````


- 表示该元素下可以有纯文本，不需要在dtd中重新定义
- 该元素下也可以有子元素，但子元素必须在dtd中已经有定义才可以
- ANY一般用在根元素上面，但是尽量不要用ANY

OR	
````
<!ELEMENT 联系人
(姓名,(电话|Email))>
````
- 联系人标签里面有两个子标签，姓名或者电话和Email中的一个
PCDATA 
纯文本数据，只能包含纯文本
例如：

````
<!DOCTYP 联系人列表[
	<!ELEMENT 联系人列表 ANY>
	<!ELEMENT 联系人(姓名)>
	<!ELEMENT 姓名(#PCDATA)>
]>
````

上面中，姓名标签只能包含纯文本数据

空元素

````
<!ELEMENT hr EMPTY>
--hr 即为空元素
<hr/>
````
#### 属性取值方式 ####
- <!ATTLIST 书
	- 名 #REQUIRED :必须要有
	- 价 #IMPLIED :可有可无
	- 大小 #FIXED "20*20" :固定属性，提供属性者
	- 如果以上都不用，必须提供缺省值
- <!ATTLIST 作者
	- 姓名 CDATA #IMPLIED--姓名是一个文本，可有可无
	- 年龄 CDATA #IMPLIED--年龄是一个文本，可有可无
	- 联系方式 CDATA #REQUIRED--联系方式是一个纯文本，必须有的
	- 职务 CDATA #FIXED "程序员"--职务固定为程序员
	- 个人爱好 CDATA 上网 --个人爱好默认值为上网

#### 引入dtd的方式 ####
直接在xml文件中写入（内部dtd）
把dtd文件的定义写到xml文件中
````
<?xml version="1.0" standalone="yes"?>
<!DOCTYPE 根元素名[元素描述]>
ENTITY定义
文件体
````
分别在不同的文件中（外部dtd）
````
<?mxl version="1.0" standalone="no"?>
<!DOCTYPE 根元素名 SYSTEM "DTD文件名">//如果dtd文件在同一个目录文件下用此方法
<!DOCTYPE 根元素名 PUBLIC "DTD标识名"
	http:www.w3.org/...DTD>
网上公开的权威机构指定的，大家都可以引用的
dtd标识名：ISO的以ISO开头，被改进的非ISO标准以“+”开头，未被改进的非ISO标准的以“-”开头
例如："-//DTD所有者名称//所描述的文件类型//语言的种类"
````
schema实例
````
<xs:schema xmlns="http://www.w3.ort/2001/XMLSchema">
	<xs:element name="quantity" type="nonNegativeInteger">
	</xs:element>
</xs:schema>

<quantity>5</quantity>
<quantity>-13</quantity>
````
````
<?xml version="1.0" encoding="gb2312"?>
<xs:schema xmlns:xs="http:www.w3.org/2001/XMLSchema">
	<xs:element name="丛书">
		<xs:complexType>//丛书下面是一个复杂的类型
			<xs:sequence>//必须按照这个顺序
				<xs:element name="书">
					<xs:element name="名" minoccurs="1"></xs:element>//minoccurs：至少出现一次
					<xs:element name="人"></xs:element>
					<xs:element name="价">
						<xs:attribute name="unit">
							<xs:enumeration value="rmb"/>//enumeration:枚举
							<xs:enumeration value="美元"/>
							<xs:enumeration value="日元"/>
						</xs:attribute>
					</xs:element>
				</xs:element>
			</xs:sequence>
		</xs:complexType>
	</xs:element>
</xs:schema>
````
#### 程序分析模型 ####
dom：所有数据位于内存
sax：流程性分析，不必把所有数据Load到内存中，可分析大型的xml文件，长用于server-side的xml-xhtml转换

### xml语法 ###



5.6:50