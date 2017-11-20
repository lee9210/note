### java流式输入/输出原理 ###
在java程序中，对于数据的输入/输出操作以"流"（stream）方式进行；J2SDK提供了各种各样的"流"类，用以获取不同种类的数据；程序中通过标准的方法输入或输出数据。
### java流的分类 ###
java.io包中定义了多个流类型（类或抽象类）来实现输入/输出功能；可以从不同的角度对其进行分类：
- 按数据流的方向可以分为输入流和输出流
- 按处理数据单位不同可以分为字节流和字符流
- 按照功能不同可以分为节点流和处理流  

### 输入/输出流类 ###
|-|字节流|字符流|
|-|-|-|
|输入流|InputStream|Reader|
|输出流|OutputStream|Writer|

#### InputStream ####
继承自InputStream的流都是用于向程序中输入数据，且数据的单位为字节（8bit）；
InputStream的实现方法有：
- FileInputStream(节点流)  
- PipeInputStream(节点流)   
- ByteArrayInputStream(节点流)   
- StringBufferInputStream(节点流) 
- SequenceInputStream（处理流）
- ObjectInputStream（处理流）  
- FileterInputStream(包括以下处理流)
	- LineNumbverInputStream
	- DataInputStream
	- BufferedInputStream
	- PushbackInputStream  

##### InputStream的基本方法 #####
//读取一个字节并以整数的形式返回（0~255），
//如果返回-1已到输入流的末尾
int read() throws IOException  

//读取一系列字节并存储到一个数组buffer,
//返回实际读取的字节数，如果读取前已到输入流的末尾返回-1
int read(byte[] buffer) throws IOException  

//去读length个字节
//并存储到一个字节数组buffer，从length位置开始  
//返回世界读取的字节数，如果读取前可以输入流的末尾返回-1  
int read(byte[] buffer,int offset,int length) throws IOException  

//关闭流，释放内存资源  
void close() throws IOException  

//跳过n个字节不读，凡是实际跳过的字节数
long skip(long n) throws IOException  
#### OutputStream ####
继承自OutputStream的流是用于程序中输入数据，且数据的单位为字节（8bit）；
包含一下类  
- FileOutputStream(节点流)  
- PipedOutputStream(节点流)
- ByteArrayOutputStream(节点流)
- ObjectOutputStream（处理流）
- FilterOutputStream（包括以下处理流）
	- DataOutputStream
	- BufferedOutputStream
	- PrintStream  

##### OutputStream的基本方法 #####
//向输出流中写入一个字节数据，该字节数据为参数b的低8位
void write(int b) throws IOException

//将一个字节类型的数组中的数据写入输出流
void write(byte[] b) throws IOException

//将一个字节类型的数组中的从指定位置（off）开始的len个字节写入到输出流
void write(byte[] b,int off,int len)throws IOException

//关闭流释放内存资源
void close() throws IOException

//将输出流中缓冲的数据全部写出到目的地
void flush() throws IOException

#### Reader ####
继承自Reader的流都是用于向程序输入数据，且数据的单位为字符（16 bit）；
Reader包喊以下类：
- CharArrayReader(节点流)
- PipedReader(节点流)
- StringReader(节点流)
- BufferedReader(处理流)
	- LineNumberReader(处理流)
- InputStreamReader(处理流)
	- FileReader(节点流)
- FileterReader(处理流)
	- PushbackReader(处理流)

#### Reader的基本方法 ####
//读取一个字符，并以整数的形式返回（0~255），
//如果返回-1，已到输入流的末尾
int read() throw IOException

//读取一系列字符，并存储到一个数组buffer，
//返回实际读取的字符数，如果读取前已到输入流的末尾，返回-1
int read(char[] cbuf) throw IOException

//读取length个字符
//并存储到一个数组buffer，从length位置开始
//返回实际读取的字符数，如果读取前以到输入流的末尾返回-1
int read(char[] cbuf,int offset,int length) throw IOException

//关闭流释放内存
void close() throw IOException

//跳过n个字符不读，返回实际跳过的字节数
long skip(long n) throw IOException


#### Writer ####
继承自Writer的流都是用于程序中输入数据，且数据的单位为字符（16 bit）；
Writer包含以下类：
- BufferedWriter(处理流)
- OutputStreamReader(处理流)
	- FileWriter(节点流)
- FilterWriter(处理流)
- FilterWriter(处理流)
- CharArrayWriter(节点流)
- PipedWriter(节点流)
- StringWriter(节点流)

#### Writer的基本方法 ####
//向输出流中写入一个字符数据，该字节数据为参数b的低16位
void write(int c) throws IOException  

//将一个字符类型的数组中的数据写入输出流
void write(char[] cbuf) throws IOException

//将一个字符类型的数组中的从指定位置（offset）开始的length个字符写入到输出流
void write(char[] cbuf,int offset,int length) throws IOException

//将一个字符串中的字符写入到输出流
void write(String string) throws IOException

//将一个字符串从offset开始的length个字符写入到输出流
void write(String string,int offset,int length) throws IOException

//关闭流释放内存资源
void close() throws IOException

//将输出流中缓冲的数据全部写出到目的地
void flush() throws IOException 
### 常见的节点流和处理流 ###
#### 节点流类型 ####
|类型|字符流|字节流|
|-|-|-|
|File(文件)|FileReader  FileWriter|FileInputStream  FileOutputStream|
|Memory Array|CharArrayReader  CharArrayWriter|ByteArrayInputStream  ByteArrayOutputStream|
|Memory String|StringReader  StringWriter|-|
|Pipe(管道)|PipedReader  PipedWriter|PipedInputStream  PipedOutputStream|

#### 访问文件 ####
- FileInputStream和FileOutputStream分别继承自InputStream和OutputStream用于向文件中输入和输出字节。
- FileInputStream和FileOutputStream常用构造方法
FileInputStream(String name) throws FileNOTFoundException  
FileInputStream(File File) throws FileNOTFoundException  
FileOutputStream(String name) throws FileNOTFoundException  
FileOutputStream(File File) throws FileNOTFoundException  
FileOutputStream(File File,boolean append) throws FileNOTFoundException  
- FileInputStream和FileOutputStream类支持其父类InputStream和OutputStream所提供的数据读写方法
- 注意：
	- 在实例化FileInputStream和FileOutputStream流时要用try-catch语句以处理其可能抛出的FileNOTFoundException。  
	- 在读写数据时也要用try-catch语句以处理可能抛出的IOException
	- FileNOTFoundException是IOException的子类

#### 节点流 ####
节点流可以从一个特定的数据源（节点）读取数据（如：文件，内存）

````
	public static void streamTest(){
		int b = 0;
		FileInputStream in = null;
		try{
			in = new FileInputStream("E:\\test\\test.txt");
		}catch(FileNotFoundException e){
			System.out.println("can't find file");
			System.exit(-1);
		}
		
		try{
			long num = 0;
			while((b=in.read())!=-1){	
				System.out.print((char)b);
				num++;
			}
			in.close();
			System.out.println();
			System.out.println("共读取了"+num+"个字节");
		}catch(IOException e){
			System.out.println("文件读取错误");
			System.exit(-1);
		}
	}
````

````
public static void testOutPut(){
		int b = 0;
		FileInputStream in = null;
		FileOutputStream out = null;
		try{
			in = new FileInputStream("E:/test/test_in.txt");
			out = new FileOutputStream("E:/test/test_out.txt");
			while((b=in.read())!=-1){
				out.write(b);
			}
			in.close();
			out.close();
		}catch(FileNotFoundException e){
			System.out.println("can't find file");
			System.exit(-1);
		}catch(IOException e){
			System.out.println("文件读取错误");
			System.exit(-1);
		}
		System.out.println("已复制该文件");
	}
````
````
public static void testFileReader(){
		FileReader fr = null;
		int c = 0;
		try{
			fr = new FileReader("E:/test/test_file.txt");
			//int In = 0;
			while((c = fr.read())!=-1){
				//char ch = (char)fr.read();
				System.out.print(c);
			}
			fr.close();
		}catch(FileNotFoundException e){
			System.out.println("can't find file");
		}catch(IOException e){
			System.out.println("文件读取错误");
		}
	}
````

````
	public static void testFileWriter(){
		FileWriter fw = null;
		FileReader fr = null;
		int c = 0;
		try{
			fr = new FileReader("E:/test/test_file.txt");
			fw = new FileWriter("E:/test/test_file_write.txt");
			while((c = fr.read())!=-1){
				fw.write(c);
				//System.out.print(c);
			}
			fr.close();
			fw.close();
		}catch(IOException e){
			e.printStackTrace();
			System.out.println("文件读写错误");
			System.exit(-1);
		}
	}
````
#### 处理流 ####
处理流是“连接”在已存在的流（节点流或处理流）之上，通过对数据的处理为程序提供更强大的读写功能。

|处理类型|字符流|字节流|
|-|-|-|
|Buffering|BufferdReader  BufferedWriter|BufferedInputStream  BufferedOutputStream|
|Filtering|FilterReader  FilterWriter|FilterInputStream  FilteroutputStream|
|Converting between bytes and character|InputStreamReader  OutputStreamWriter|-|
|Object Serialization|-|ObjectInputStream  ObjectOutputStream|
|Data conversion|-|DataInputStream  DataOutputStream|
|Counting|LineNumberReader|LineNumberInputStream|
|Peeking ahead|PusbackReader|PushbackInputStream|
|Printing|PrintWriter|PrintStream|

#### 缓冲流 ####
- 缓冲流要"套接"在相应的节点上，对读写的数据提供了缓冲的功能，提高了读写的效率，同时增加了一些新的方法；
- J2SDK提供了四种缓冲流，其常用的构造方法为：
	- BufferedReader(Reader in)
	- BufferedReader(Reader in,int sz)//sz为自定义缓冲区大小
	- BufferedWriter(Writer out)
	- BufferedWriter(Writer out,int sz)
	- BufferedInputStream(InputStream in)
	- BufferedInputStream(InputStream in,int sz)
	- BufferedOutputStream(OutputStream out)
	- BufferedOutputStream(OutputStream out,int sz)

- 缓冲输入流支持其父类的mark和reset方法
- BufferedReader提供了readline方法，用于读取一行字符串（以\r或\n分隔）
- BufferedWriter提供了newLine方法，用于写入一个行分隔符。
- 对于输出的缓冲流，写出的数据会先在内存中缓存，使用flush方法将会使内存中的数据立刻写出

````
	public static void bufferStream1(){
		try{
			FileInputStream fis = new FileInputStream("E:/test/test_file.txt");//创建链接，准备开始读取数据
			BufferedInputStream bis = new BufferedInputStream(fis);//对fis进行处理，加上缓冲区
			int c = 0;
			System.out.println(bis.read());//读取一个字符
			System.out.println(bis.read());//读取一个字符
			bis.mark(100);//标记，从第100个字符开始读
			for(int i=0;i<=10&&(c=bis.read())!=-1;i++){
				System.out.print(c+" ");
			}
			System.out.println();
			bis.reset();//回到标记的点
			for(int i=0;i<=10&&(c=bis.read())!=-1;i++){
				System.out.print(c+" ");
			}
			bis.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
````
````
	public static void bufferStream2(){
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter("E:/test/test_file_write.txt"));//创建writer链接
			BufferedReader br = new BufferedReader(new FileReader("E:/test/test_file.txt"));//创建reader链接
			String s = null;
			for(int i=0;i<=100;i++){//写入100行数据
				s = String.valueOf(Math.random());//产生一个随机数
				bw.write(s);
				bw.newLine();//写一个换行符，写一个新行
				System.out.print(s+" ");
			}
			bw.flush();
			while((s=br.readLine())!=null){//读取刚刚写入的数据
				System.out.println(s);
			}
			bw.close();
			br.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
````
### 文件流 ###

### 缓冲流 ###

### 数据流 ###
- DataInputStream和DataOutputStream分别继承自InputStream和OutputStream,它属于处理流，需要分别“套接”在InputStream和OutputStream类型的节点流上
- DataInputStream和DataOutputStream提供了可以存取与机器无关的java原始类型数据（如：int，double等）的方法。
- DataInputStream和DataOutputStream的构造方法为
	- DataInputStream(InputStream in)
	- DataOutputStream(OutputStream out)

````
	public static void testDataStream(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();//在内存中分配一个字节数组，准备往里写数据
		DataOutputStream dos = new DataOutputStream(baos);//套一层
		try{
			dos.writeDouble(Math.random());//往dos里面写一个double随机数
			dos.writeBoolean(true);//把一个boolean类型写进去
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());//从baos中读取数据
			DataInputStream dis = new DataInputStream(bais);//通过DataInputStream转换为数组
			System.out.println(dis.available());
			System.out.println(dis.readDouble());
			System.out.println(dis.readBoolean());
			dos.close();
			dis.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
````
### 转换流 ###
- InputStreamReader和OutputStreamWriter用与自己数据到字符之间的转换。
- InputStreamReader需要和InputStream"套接"
- OutputStreamWriter需要和OutputStream"套接"
- 转换流在构造时可以指定其编码集合，例如
	- InputStream isr = new InputStreamReader(System.in,"ISO8859_1");

````
	public static void testTransform1(){
		try{
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream("E:/test/test.txt"));//创建缓冲写的接口,若不用OutputStreamWriter接口，写中文的时候比较麻烦
			osw.write("this is a test write");//写的数据
			System.out.println(osw.getEncoding());//获取字符编码
			osw.close();
			osw = new OutputStreamWriter(new FileOutputStream("E:/test/test.txt",true),"ISO8859_1");//创建缓冲写的接口，ture：在原来文件的基础上，追加。ISO8859_1：字符编码
			osw.write("this is a test write new");
			System.out.println(osw.getEncoding());
			osw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
````
````
	public static void testTransform2(){//阻塞式
		//把system.in转换成reader
		InputStreamReader isr = new InputStreamReader(System.in);//等待标准输入，
		BufferedReader br = new BufferedReader(isr);//把reader转换成BufferedReader,方便使用里面的readLine方法
		String s = null;
		try{
			s = br.readLine();//读一行
			while(s!=null){
				if(s.equalsIgnoreCase("exit")){//忽略大小写
					break;
				}
				System.out.println(s.toUpperCase());//转换成大写
				s = br.readLine();
			}
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
````
### Print流(打印流) ###
- PrintWriter和PrintStream都属于输出流，分别针对字符和字节
- PrintWriter和PrintStream提供重载的print
- Println方法用于多种数据类型的输出
- PrintWriter和PrintStream的输出操作不会抛出异常，用户通过检测错误状态获取错误信息
- PrintWriter和PrintStream有自动flush功能

构造方法
- PrintWriter(Writer out)  
- PrintWriter(Writer out,boolean autoFlush)
- PrintWriter(OutputStream out) 
- PrintWriter(OutputStream out,boolean autoFlush) 
- PrintStream(OutputStream out)
- PrintStream(OutputStream out,boolean autoFlush)

````
	public static void testPrint1(){
		PrintStream ps = null;
		try{
			FileOutputStream fos = new FileOutputStream("E:/test/test.txt");
			ps = new PrintStream(fos);
		}catch(Exception e){
			
		}
		if(ps != null){
			System.setOut(ps);//重新设置System.out的值为ps
		}
		int ln = 0;
		for(char i = 0;i<=6000;i++){
			System.out.print(i+" ");
			if(ln++ >= 100){
				System.out.println();
				ln = 0;
			}
		}
	}

````
````
	public static void testPrint2(String args){
		String fileName = args;
		if(fileName != null){
			list(fileName,System.out);
		}
	}
	public static void list(String fileName,PrintStream ps){
		try{
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String s = null;
			while((s=br.readLine()) != null){
				ps.println(s);
			}
			br.close();
		}catch(Exception e){
			ps.println("无法读取文件 ");
		}
	}

````
````

````
### Object流 ###
直接将object写入或读出 
- transient关键字
- serializable接口
- externalizable接口（自己控制序列化过程）

````
	public static void testObjectIO() throws Exception{
		//序列化
		T t = new T();
		t.k = 8;
		FileOutputStream fos = new FileOutputStream("E:/test/test.txt");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(t);;
		oos.flush();
		oos.close();
		
		//反序列化
		FileInputStream fis = new FileInputStream("E:/test/test.txt");
		ObjectInputStream ois = new ObjectInputStream(fis);
		T tReaded = (T)ois.readObject();
		System.out.println(tReaded.i+" "+tReaded.j+" "+tReaded.d+" "+tReaded.k+" ");
	}
	static class T implements Serializable{//实现Serializable接口，表示T可以被序列化
		int i = 0;
		int j = 9;
		double d = 2.3;
		int k = 0;
	}
````
运行结果：0 9 2.3 8 

````
	static class T implements Serializable{//实现Serializable接口，表示T可以被序列化
		int i = 0;
		int j = 9;
		double d = 2.3;
		transient int k = 0;
	}
````
运行结果：0 9 2.3 0 
transient修饰的变量，在序列化的时候不予以考虑


