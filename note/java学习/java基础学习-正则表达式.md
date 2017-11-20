把所有数字换成横线
````
print("abcd548421545afdf".replaceAll("\\d", "-"));
````
````
Pattern p = Pattern.compile("[a-z](3)");//匹配一个具有三个字符的字符串
````
".","*","+"的含义
.:匹配一个字符
*：0个或多个
+:一个或多个
?:一个或没有	
````
"a".matches(".");//true
"aa".matches("aa");//true
"aaaa".matches("a*");//true
"aaaa".matches("a+");//true
"".matches("a*");//true
"aaaa".matches("a?");//true
"".matches("a?");//true
"a".matches("a?");//true
"1654632566132".matches("\\d(3,100)");//true
"192.256.4.asdf".matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");//false
"1654632566132".matches("\\d{3,100}");//true
"192".matches("[0-2][0-9][0-9]");//true
````
范围
````
print("a".matches("[abc]"));//abc中的一个
print("a".matches("[^abc]"));//除了abc的字符
print("A".matches("[a-zA-Z]"));//小写的a-z或者大写的A-Z
print("A".matches("[a-z]|[A-Z]"));//小写的a-z或者大写的A-Z
print("A".matches("[a-z[A-Z]]"));//小写的a-z或者大写的A-Z
print("R".matches("[A-Z&[RFG]]"));//取交集
````
认识\s \w \d \
\d : 0-9
\D : 0-9取反[^0-9]
\s : 空白字符。包含：[空格]、\n、\t、\x0B、\f、\r	
\S : 不是空白字符。[^\s]
\w : [a-zA-Z_0-9](a-z,A-Z,下横线，0-9)
\W : [^\w]
````
print(" \n\r\t".matches("\\s{4}"));//true,空白字符
print(" ".matches("\\s"));//true，空白字符
print("a_8".matches("\\w{3}"));//true
print("\\".matches("\\\\"));//true
````
边界匹配
````
print("hello sir".matches("^h.*"));//true
print("hello sir".matches(".*ir$"));//true
print("hello sir".matches("^h[a-z]{1,3}o\\b.*"));//true
print("hello sir".matches("^h[a-z]{1,3}o\\b.*"));//true 
````
注：^位于中括号内为取反，中括号外面为开始 

matches find lookingAt
matches:匹配或不匹配
find:找一个字串，并取出，原来的字符串删除

#### 替换 ####
````
String str = "java Java JAVa JaVa IloveJAVA you hateJava awefasdfasdfew";
Pattern p = Pattern.compile("java",Pattern.CASE_INSENSITIVE);//忽略大小写
Matcher m = p.matcher(str);
//print(m.replaceAll("JAVA"));//m.replaceAll("JAVA")返回值是一个字符串
StringBuffer ret = new StringBuffer();
while(m.find()){
	m.appendReplacement(ret, "java");//替换匹配的部分
}
m.appendTail(ret);//添加上后面的
print(ret);
````
#### 分组 ####
````
Pattern p = Pattern.compile("(\\d{3,5})([a-z]{2})");
String s = "123aa-34345bb-234cc-00dd";
Matcher m = p.matcher(s);
while(m.find()){
print(m.group(0));//匹配的字串
print(m.group(1));//匹配的第一组
print(m.group(2));//匹配的第二组
}
````
例子，统计行数
````
static long normalLines = 0;
static long commentLines = 0;
static long whiteLines = 0;

public static void main(String[] args) {
	File f = new File("D:\\java");//读取文件夹
	File[] codeFiles = f.listFiles();//获取文件夹内文件列表
	for(File child : codeFiles){
		if(child.getName().matches(".*\\.java$")) {//若匹配为java文件，则统计
			parse(child);
		}
	}
	
	System.out.println("normalLines:" + normalLines);
	System.out.println("commentLines:" + commentLines);
	System.out.println("whiteLines:" + whiteLines);
	
}

private static void parse(File f) {
	BufferedReader br = null;//创建一个输入
	boolean comment = false;
	try {
		br = new BufferedReader(new FileReader(f));//对输入进行处理
		String line = "";
		while((line = br.readLine()) != null) {//readLine会去掉换行符
			line = line.trim();//去掉行首尾的空格
			if(line.matches("^[\\s&&[^\\n]]*$")) {//匹配空行
				whiteLines ++;
			} else if (line.startsWith("/*") && !line.endsWith("*/")) {//注释行并且注释行开始
				commentLines ++;
				comment = true;	
			} else if (line.startsWith("/*") && line.endsWith("*/")) {//注释行
				commentLines ++;
			} else if (true == comment) {//注释行之间的内容
				commentLines ++;
				if(line.endsWith("*/")) {//注释行结束
					comment = false;
				}
			} else if (line.startsWith("//")) {
				commentLines ++;
			} else {
				normalLines ++;
			}
		}
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} finally {
		if(br != null) {
			try {
				br.close();//关闭输入流
				br = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
````
修饰符"?"、"+"、"不写"
"?"：按照最小量来匹配
````
Pattern p = Pattern.compile(".{3,10}?[0-9]");//最小量匹配
String s = "aaaa5bbbb68";
Matcher m = p.matcher(s);
if(m.find()){
	System.out.println(m.start() + "-" + m.end());
}else {
	System.out.println("not match!");
}
````
"+":最大量匹配，按照最大的数量来匹配
````
Pattern p = Pattern.compile("(.{3,10}+)[0-9]");
String s = "aaaa5bbbb8";
Matcher m = p.matcher(s);
if(m.find()){
	System.out.println(m.start() + "-" + m.end());
}else {
	System.out.println("not match!");
}
````
不写类似于+,按照最大的数值来匹配
