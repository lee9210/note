### java基本数据类型 ###
逻辑型 -- boolean
文本型 -- char
整数型 -- byte,short,int,long
浮点数型 -- float;double
#### 字符型 ####
- char型数据用来表示通常意义上的字符
- 字符常量为用单引号括起来的单个字符。如
	- char eChar = "a";char cChar = "中"
- java字符采用Unicode编码，每个字符占两个字节，因而可以用十六进制编码形式表示，例如
	- char c1 = "\u0061";
	- 注：Unicode是全球语言统一编码
- java语言中还允许使用转移字符'\'来将其后的字符转变为其他的含义，例如：
	- char c2 = "\n";//'\n'代表换行符
#### 整数型 ####
- jva各整数类型有固定的表数范围和字段长度，其不受具体操作系统的影响，以保证java程序的可移植性。
- java语言整形常亮的三种表示形式
	- 十进制：15,55，-58
	- 八进制，要求以0开头，如：012.
	- 十六进制，要求以0X或0x开头，如0x45.
- java语言的整形常亮默认为int型，声明long型常量后可以加"l"或"L"，如：
	- int i1 = 600;//正确
	- long l1 = 4545455454544L;//必须加l否则会出错

|类型|占用存储空间|表数范围|
|-|-|-|
|byte|1字节|-128~127|
|short|2字节|-2^15~2^15-1|
|int|4字节|-2^31~2^31-1|
|long|8字节|-2^63~2^63-1|
#### 浮点类型 ####
- 与整数类型类似，java浮点类型有固定的表数范围和字段长度，不受平台影响。
- java浮点类型常亮有两种表示形式
	- 十进制数形式，例如：3.14；3.140,；.314
	- 科学计数法形式，如3.14e2；3.14E2；100E-2
- java浮点型常量默认为double型，如要声明一个常量为float型，则需在睡后面加f或F，如：
	- double d = 1254.5;//正确 float f = 1.23f;//必须加f，否则会出错

|类型|占用存储空间|表数范围|
|-|-|-|
|float|4字节|-3.403E38~3.403E38|
|double|8字节|-1.798E308~1.798E308|
### 运算符 ###

### 表达式和语句 ###

### 分支 ###

### 循环 ###

### 方法 ###

### 变量的作用域 ###

### 递归调用 ###



3