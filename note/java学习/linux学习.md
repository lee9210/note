# Linux各目录的作用 #
## /bin/   ##
存放系统命令的目录，普通用户和超级用户都可以执行。不过放在/bin下的命令在单用户模式下也可以执行  
/sbin/  
保存和系统环境设置相关的命令，只有超级用户可以使用这些命令进行系统环境设置，但是有些命令可以允许普通用户查看  
## /usr/bin/ ##
存放根文件系统不必要的系统管理命令，例如多数服务程序。只有超级用户可以使用。在所有的"sbin"目录中保存的命令只有超级用户可以使用，"bin"目录中保存的命令所有用户都可以使用
## /boot/   ##
系统启动目录，保存系统启动相关的文件，如内核文件和启动引导程序(grub)文件等
## /dev/   ##
设备文件保存位置。
## /etc/  ##
设置文件保存位置。系统内所有采用默认安装方式(rpm安装)的服务的配置文件全部都保存在这个目录中，如用户账户和密码，服务的启动脚本，常用服务的配置文件等。
## /home/ ##
普通用户的家目录。简历每个用户时，每个用户要有一个默认登陆位置，这个位置就是用户的家目录，所有普通用户的家目录就是在/home下简历一个和用户名相同的目录。如用户user1的家目录就是/home/user1
## /lib/ ##
系统调用的函数库保存位置
## /lost+found/ ##
当系统意外崩溃或机器意外关机，而产生一些文件碎片放在这里。当系统启动的过程中fsck工具会检查这里，并修复已经损坏的文件系统。这个目录只在每个分区中出现，例如/lost+found就是根分区的备份恢复目录，/boot/lost+found就是/boot分区的备份恢复目录
## /media/ ##
挂载目录。系统建议是用来挂载媒体设备的，例如软盘和光盘
## /mnt/ ##
挂载目录。早期Linux中只有这一个关在目录，并没有细分。现在这个目录系统建议挂载额外设备，如U盘，移动硬盘和其他操作系统的分区。
## /misc/ ##
挂载目录。系统建议用来关在NFS服务的共享目录。
## /opt/ ##
第三方安装的软件保存位置。这个目录就是防止和安装其他软件的位置，手工安装的源码包和软件都可以安装到这个目录中。不过更习惯把软件放置到/usr/local/目录中，也就是说/usr/local/目录也可以用来安装软件
## /proc/ ##
虚拟文件系统，该目录中的数据并不保存到硬盘中，而是保存到内存当中。主要保存系统的内核，进程，外部设备状态和网络状态灯。如/proc/cpuinfo是保存CPU信息的，/proc/devices是保存设备驱动的列表的，、、/proc/filesstems是保存文件系统列表的，/proc/net是保存网络协议信息的
## /sys/ ##
虚拟文件系统。和/proc目录相似，都是保存在内存当中的，主要是保存于内核相关信息的
## /root/ ##
超级用户的家目录。普通用户在"/home"下，超级用于家目录直接在"/"下
## /srv/ ##
服务数据目录。一些系统服务启动之后，可以在这个目录中保存所需要的数据
## /tmp/ ##
临时目录，系统存放临时文件的目录，该目录下所有用户都可以访问和写入。建议此目录不能保存重要数据，最好每次开机都把该目录清空。
## /usr/ ##
系统软件资源目录。"Unix Software Resource"的缩写，所以不是存放用户数据，而是存放系统软件资源的目录。系统中安装的软件大多数保存在这里。
## /var/ ##
动态数据保存位置。主要保存缓存、日志以及软件运行所产生的文件。
# 服务器注意事项 #
- 远程服务器不允许关机，只能重启
- 重启时，应该关闭服务
- 不要在服务器访问高分运行高负载命令	(大数据量的压缩复制扫描)
- 远程配置防火墙时不要把自己提出服务器(ip地址，端口号，包中数据)
- 指定合理的密码规范并定期更细
- 合理分配权限(越少越好)
- 定期备份重要数据和日志(系统备份的时候，etc,lib,vim,usr)
# 命令格式 #
格式：命令 [-选项] [参数]
例如：ls -la /etc
## 说明 ##
- 个别命令使用不遵循此格式(ls命令可以不遵循此格式)
- 当有多个选项时，可以写在一起
- 简化选项与完整选项(-a 等于 --all)
## 目录处理命令 ##
## ls ##
名称：ls  
路径：/bin/ls  
描述：显示目录文件  
语法：ls 选项[-ald] [文件或目录]  
	 -a  显示所有文件，包括隐藏文件  
	 -l  详细信息显示  
     -d  查看目录属性  
	 -h  人性化显示，不以byte显示  
	 -i  查询文件的i节点
### -rw-r--r-- ###
-文件类型(-:二进制文件；d:目录；l:软链接文件)  
rw- r-- r--  
u   g   o  
u所有者	g所属组  o其他人  
r读 w写 x执行
## mkdir ##
名称：mkdir  
路径：/bin/mkdir  
语法：mkdir -p [目录名]
描述：创建新目录  
	 -p 递归创建  
例如：  
$mkdir -p /tmp/japan/boduo  
$mkdir /tmp/japan/longze/ tmp/japan/cangjing
## cd ##
名称：cd  
路径：shell内置命令  
语法：cd [目录]  
描述：切换目录  
例如：  
$cd /tmp/japan/boduo 切换到指定目录  
$cd ..  回到上级目录
## pwd ##
名称：pwd  
路径：/bin/pwd  
语法：pwd  
描述：显示当前目录  
例如：  
$pwd  
/tmp/japan
## rmdir ##
名称：rmdir  
原意：remove empty directories  
路径:/bin/rmdir  
语法：rmdir [目录名]  
描述：删除空目录  
例如：  
$rmdir /tmp/japan/boduo
## cp ##
名称：cp  
路径：/bin/cp  
语法：cp -rp [原文件或目录] [目标目录]  
　　　　　-r　复制目录  
　　　　　-p　保留文件属性  
描述：复制文件或目录  
例如：  
$cp -r /tmp/japan/cangjing /root  
将目录/tmp/japan/cangjing复制到目录/root下  
$cp -rp /tmp/japan/boduo /tmp/japan/longze /root  
将目录/tmp/japan/boduo和longze复制到目录/root下，保持目录属性 
## mv ##
名称：mv  
路径：/bin/mv  
语法：mv [原文件或目录] [目标目录]  
描述：剪切文件、改名  
例如：  
剪切：$mv /tmp/japan/cangjing /root  
改名：$mv /tmp/japan/longze /root/nvshen
## rm ##
名称：rm  
路径：/bin/rm  
语法：rm -rf [文件或目录]  
　　　　　－r 删除目录    
　　　　　－f 强制执行  
描述：删除文件
# 文件处理命令 #
## touch ##
语法：touch [文件名/目录+文件名]  
描述：创建空文件  
例如：
$touch japanlovestory.list  
创建带空格的文件，需要带空格  
## cat ##
语法：cat [文件名]  
描述：显示文件内容  
　　　-n  显示行号  
例如：  
$cat /etc/issue  
$cat  -n /ect/services  
## more ##
语法：more [文件名]  
　　　(空格)或f　翻页  
　　　(enter)　　换行  
　　　q或Q　　　　退出  
描述：分页显示文件内容  
例如：
$more /etc/services　　
## less ##
语法：lese [文件名]  
描述：和more相似，可向上翻页  
## head ##
语法：head [文件名]  
描述:显示文件前面几行（-n  指定行数）  
例如：$head -n 20 /ect/services
## tail ##
语法：tail [文件名]  
描述:显示文件后面几行（-n  指定行数/-f 动态显示文件末尾内容）  
例如：$tail -n 20 /ect/services
## ln ##
语法：ln -s [原文件] [目标文件]  
　　　　　-s 创建软链接  
描述：生成链接文件  
例如：  
$ln -s /etc/issue /tmp/issue.soft  
创建文件/etc/issue的软链接/tmp/issue.soft  
$ln /etc/issue /tmp/issue.hard  
创建文件/etc/issue的硬链接/tmp/issue.hard  
### 软链接 ###
特征：类似windows快捷方式  
1.lrwxrwxrwx　　1 软链接  
2.文件大小-只是符号链接  
3./tmp/issue.soft->/etc/issue（箭头指向源文件）
### 硬链接 ###
1.拷贝cp -p+同步更新  
2.通过i节点识别（硬链接为同一节点，软链接为不同节点）  
3.不能跨分区  
4.不能针对目录使用  

## 权限管理命令 ##
## chmod ##
语法：chmod [{ugoa}{+-=}{rwx}] [文件或目录] (a表示所有人/g所属组/u表示所有者/o表示其他人) 
　　　　　　[mode==421] [文件或目录]  
　　　　　　-R 递归修改  
描述：修改文件或目录权限  
例如：  
$chmod u+x [file name]//给所有者增加执行的权限  
$chmod g+w,o-r [file name]//给所属组增加写的权限，给其他人减掉读的权限  
$chmod g=rwx [file name]//让所属组拥有读写执行的权限  
$chmod -R 777 [file name]//为所有者，所属组，其他人增加读写执行的权限
### 权限的数字表示 ###
r -- 4  
w -- 2  
x -- 1  
rwxrw-r--    
7　6　4  
## chown ##
名称：chown  
语法：chown [用户] [文件或目录]  
描述：改变文件或目录的所有者（只有root可以修改）  
例如：  
$chown aaa bbb  
改变文件bbb的所有者为aaa
## chgrp ##
名称：chgrp  
语法：chgrp [用户组] [文件或目录]  
描述：改变文件或目录的所属组  
例如：  
$chgrp aaa bbb  
改变文件bbb的所属组为aaa  
## umask ##
名称：umask  
语法：umask [-S]  
　　　－S  以rwx形式显示新建文件缺省权限  
描述：显示、设置文件的缺省权限  
例如：  
$umask -S  
## 文件搜索命令 ##
## find ##
名称：find  
语法：find [搜索范围] [匹配条件]  
描述：文件搜索  
例如：  
$find /etc -name init  
在etc目录下搜索文件名为init的文件  
$find /etc -name *init*  
在etc目录下查找文件名包括init的文件（*通配符）  
$find /etc -name init???  
在etc目录下查找文件名为init后面有三个字符的文件（?通配符）  
$find /etc -iname init  
在etc目录下查找文件名为init的文件（加了i大写的也会找到）  
$find /etc -size +204800  
在etc目录下查找大于100M的文件(+n:大于/-n:小于/n:等于)  
$find /etc -size +1638400 -a -size -204800  
在etc目录下查找大于80M小于100M的文件(-a:两个条件同时满足/-o:两个条件满足任意一个即可)  
$find /etc -user aaa  
在etc目录下查找所有者为aaa的文件(-group:根据所属组查找)  
$find /etc -cmin -5  
在etc目录下查找5分钟内被修改过属性的文件和目录(-amin:访问时间/-cmin:文件属性/-mmin:文件内容)  
$find /etc -name inittab -exec ls -l {} \;  
在etc下查找inittab文件并显示其详细信息(-exec/ok 命令 {} \;对搜索结果执行操作（{} \；为固定格式）)  
### -type ###
根据文件类型查找  
f:文件  
d:目录  
l:软链接文件  
### -inum ###
根据i节点查找
## locate ##
名称：locate  
语法：locate 文件名  
描述：在文件资料库中查找文件  
例如：  
$locate inittab  
查找文件名为inittab的文件
## which ##
名称：which  
语法：which 命令  
描述：搜索命令所在目录或别名信息  
例如：  
$which ls
## grep ##
名称：grep  
语法：grep -iv [指定字串] [文件]  
描述：在文件中搜寻字串匹配的行并输出  
　　　－ｉ不区分大小写  
　　　－v 排除指定字串  
例如：#grep mysql /root/install.log
## 帮助命令 ##
## man ##
语法：man [命令或配置文件]  
描述：获得帮助信息  
例如：  
$man ls
查看ls命令的帮助信息  
$man services  
查看配置文件services的帮助信息  
## help ##
语法：help 命令  
描述：获得shell内置命令的帮助信息  
例如：  
$help umask  
查看umask命令的帮助信息
## 用户管理命令 ##
## useradd ##
权限：useradd  
语法：useradd 用户名  
描述：添加新用户  
例如：$useradd aaa(添加aaa用户)
## passwd ##
语法：passwd 用户名  
描述：设置用户密码  
例如：$passwd  aaa(设置aaa用户的密码)
## who ##
语法：who  
描述：查看登陆用户信息  
## w ##
名称：w  
语法：w  
描述：查看登陆用户详细信息
## 压缩解压命令 ##
## gzip ##
语法：gzip [文件]  
描述：压缩文件  
压缩后文件格式：.gz  
## gunzip ##
语法：gunzip [压缩文件]  
描述：解压缩.gz的压缩文件  
## tar ##
语法：tar 选项[-zcf] [压缩后文件名] [目录]  
　　　　　－ｃ打包  
　　　　　－ｖ显示详细信息  
　　　　　－ｆ指定文件名  
　　　　　－ｚ打包同时压缩  
描述：打包目录  
压缩后文件格式：.tar.gz
## zip ##
语法：zip 选项[-r] [压缩后文件名] [文件或目录]  
　　　－ｒ　压缩目录  
描述：压缩文件或目录  
压缩后文件格式：.zip
## bunzip2 ##
语法：bunzip2 选项[-k] [文件]  
　　　－ｋ 产生压缩文件后保留原文件  
描述：压缩文件  
压缩后文件格式：.bz2  
## 网络命令 ##
## write ##
语法：write <用户名>  
描述：给用户发信息，以Ctrl+D保存结束（在线用户）
## wall ##
语法：wall [message]  
描述：发广播信息
## ping ##
语法：ping 选项 ip地址  
　　　－ｃ　指定发送次数　　
描述：测试网络联通性　　
例如：ping -c 2 127.0.0.1(ping两次)
## ifconfig ##
权限：root  
语法：ifconfig 网卡名称 ip地址  
描述：查看和设置网卡信息  
例如：#ifconfig eth0 192.168.1.1  
## mail ##
语法：mail [用户名]  
描述：查看发送电子邮件  
例如：#mail root  
查看用输入序列号，删除用（d 序列号）
## last ##
语法：last  
描述：列出目前与过去登入系统的用户信息
## lastlog ##
语法：lastlog  
描述：检查某特定用户上次登陆的时间  
例如：#lastlog -u 502（用户的uid）
## traceroute ##
语法：traceroute  
描述：显示数据包到主机间的路径  
例如：traceroute www.baidu.com
## netstat ##
语法：netstat [选项]  
描述：显示网络相关信息  
选项：
-t： tcp协议  
-u： udp协议  
-l： 监听  
-r： 路由  
-n： 显示ip地址和端口号  
例如：
$netstat -tlun 查看本机监听的端口  
$netstat -an 查看本机所有的网络链接  
$netstat -rn 查看本机路由表  
### TCP协议 ###
面向连接的协议，更加可靠  
### UDP协议 ###  
速度更快，不可靠  
## setup ##
语法：setup  
描述：配置网络  
## mount ##
语法：mount [-t 文件系统] 设备文件名 挂载点  
例如：mount -t iso9660 /dev/sr0/mnt/cdrom  
## shutdown ##
语法：shutdown [选项] 时间  
选项：  
-c:取消前一个关机命令  
-h:关机    
-r:重启
### 系统运行界别 ###
0 关机  
1 单用户（类似于windows的安全模式，启动最小最核心服务。主要用来做修复）  
2 不完全多用户，不含NFS服务（NFS:network file system主要用于两套系统文件的互传）  
3 完全多用户  
4 未分配  
5 图形界面  
6 重启  

## vim编辑器 ##
打开一个文件，若没有，则创建一个  
按下"i"进入插入模式，insert。左下角会出现"insert"的字符  
### 插入命令 ###
a:在光标所在字符后插入  
A:在光标所在行尾插入  
i:在光标所在字符钱插入  
I:在光标所在行行首插入  
o:在光标下插入  
O:在光标上插入
### 定位命令 ###
在编辑模式下输入以下命令
:set nu：设置行号  
:set nonu：取消行号  
gg：到第一行  
G：到最后一行  
nG：到第n行  
:n：到第n行  
$:移至行尾  
0:移至行首
### 删除命令 ###
在编辑模式下输入以下命令
x:删除光标所在处字符  
nx:删除光标所在处后n个字符  
dd:删除光标所在行，ndd删除n行  
dG:删除光标所在行到文件末尾内容  
D:删除光标所在行尾内容  
:n1,n2d:删除指定范围的行（n1表示起始行，n2表示终止行）
### 复制和剪切命令 ###
在编辑模式下输入以下命令
yy:复制当前行  
nyy:复制当前以下n行  
dd:剪切当前行  
ndd:剪切当前以下n行  
p、P:粘贴在当前光标所在行下或行上
### 替换和取消命令 ###
在编辑模式下输入以下命令  
r:取代光标所在处字符  
R:从光标处开始替换字符，按Esc结束  
u:取消上一步操作
### 搜索和搜索替换命令 ###
在编辑模式下输入以下命令  
/string:搜索指定字符串，搜索时忽略大小写：set ic  
n:搜索指定字符串的下一个出现位置  
:%s/old/new/g:全文替换指定字符串  
:n1,n2s/old/new/g：在一定范围内替换指定字符串（n1表示起始行，n2表示终止行）
### 保存和退出命令 ###
在编辑模式下输入以下命令  
:w:保存修改  
:w new_filename:另存为指定文件  
:wq:保存修改并退出  
ZZ:快捷键，保存修改并退出  
:q!:不保存修改退出  
:wq!:保存修改并退出（文件所有者及root可使用）
### 其他 ###
导入命令执行结果：r！命令（例如":r !filename"）  
定义快捷键：map 快捷键 触发命令  
例如：  
:map ^P I#<ESC>（^为Ctrl）  
:map ^B 0x  
连续行注释  
:n1,n2s/^/#/g  
:n1,n2s/^/#//g  
:n1,n2s/^/\/\//g(\为转义符)  
替换：  
ab aa bb(aa替换成bb)
#### tip ####
若想定义的命令永久有效，可以在home目录下对应用户文件夹中的".vimrc"文件。可以设置某些编辑模式的命令
## 软件包管理 ##
软件包分类：
1. 源码包（脚本安装包）
2. 二进制包（RPM包，系统默认包）
源码包优点：
1. 开源，如果有足够的能力，可以修改源代码；
2. 可以自由选择所需的功能
3. 软件是编译安装，所以更加适合自己的系统，更加稳定效率高
4. 卸载方便
源码包缺点：
1. 安装步骤较多，尤其安装较大的软件集合时，容易出现拼写错误
2. 编译过程时间长，安装比二进制安装时间长
3. 因为是编译安装，安装过程中一旦报错新手很难解决
### RPM包 ###
二进制包的优点：
1. 包管理系统简单，只通过几个命令就可以实现包的安装、升级、查询和卸载
2. 安装速度比源码包安装快的多
二进制包的缺点：
1. 经过编译，不再可以看到源码
2. 功能选择不如源码包灵活
3. 依赖性
### RPM包命名原则 ###
软件包全名：httpd-2.2.15-15.e16.centos.1.i686.rpm  
httpd　　　　　软件包名  
2.2.15　　　　　软件版本  
15　　　　　　软件发布的次数  
e16.centos　　适合的Linux平台  
i686　　　　　适合的硬件平台  
rpm　　　　　rpm包扩展名  
- 包名：操作已经安装的软件包时，使用包名。是搜索/var/lib/rpm/中的数据库  
- 包全名：操作的包是没有安装的软件包时，使用包全名。而且要注意路径
### RPM包依赖性 ###
- 树形依赖：a->b->c(卸载的时候，反过来)
- 环形依赖：a->b->c->a
- 模块依赖：模块依赖查询网站：www.rpmfind.net
### RPM安装 ###
#### rpm -ivh 包全名 ####
选项：  
-i(install):安装  
-v(verbose):显示详细信息  
-h(hash):显示进度  
--nodeps:不检测依赖性
### RPM卸载 ###
#### rpm -e 包名 ####
选项：  
-e(erase):卸载  
--nodeps:不检查依赖性
### 查询是否安装 ###
#### rpm -q 包名 ####
查询是否安装  
选项：-q 查询（query）
#### rpm -qa ####
查询搜油已经安装的RPM包  
选项：-a 所有（all）
#### rpm -qi 包名 ####
查询软件包详细信息  
选项：  
-i：查询软件信息（information）  
-p：查询未安装包信息（package）
#### rpm -ql 包名 ####
查询包中文件安装位置  
选项：  
-l：列表（list）  
-p：查询未安装包信息（package）
#### rpm -qf 系统文件名 ####
查询系统文件属于哪个RPM包  
选项：-f:查询系统文件属于哪个软件包（file）
#### rpm -qR 包名 ####
查询软件包的依赖性  
选项：  
-R：查询软件包的依赖性（require）  
-p：查询未安装包信息（package）  
#### rpm -V 已安装的包名 ####
rpm包校验  
选项：-V：校验指定的rpm包中的文件（verify）  
验证内容汇总的8个信息的具体内容如下：  
- S 文件大小是否改变  
- M 文件的类型或文件的权限（rwx）是否改变  
- 5 文件的MD5校验和是否改变（可以当成文件内容是否改变）  
- D 设备中的，从代码是否改变  
- L 文件路径是否改变  
- U 文件的属主（所有者）是否改变  
- G 文件的属组是够改变  
- T 文件的修改时间是否改变
文件类型：
- c 配置文件（config file）
- d 普通文档（documentation）
- g "鬼"文件（ghost file），很少见，就是该文件不应该被之歌rpm包包含
- l 授权文件（license file）
- r 描述文件（read me）
#### rpm2cpio 包全名 | \ ####
cpio -idv.文件绝对路径  
rpm2cpio：将rpm包转换为cpio格式的命令
cpio：是一个标准工具，它用于创建软件档案文件和从档案文件中提取文件
例如：  
1. rpm -qf /bin/ls *#查询ls命令属于那个软件包*  
2. mv /bin/ls /tmp/ *#造成ls命令误删除假象*  
3. rpm2cpio /mnt/cdrom/Packages/coreutils-8.4-l9.el6.i686.rpm | cpio -idv ./bin/ls *#提取RPM包中的ls命令到当前目录的/bin/ls下*  
4. cp /root/bin/ls /bin/ *#把ls命令复制会/bin/目录，修复文件丢失*
## yum在线管理 ##
### IP地址配置和网络yum源 ###
#### IP地址配置 ####
1. setup(使用setup工具)
2. vi /etc/sysconfig/network-scripts/ifcfg-eh0（把ONBOOT="no"改为NOBOOT="yes"）启动网卡
3. service network restart(重启网络服务)
#### 网络yum源 ####
#### vi /etc/yum.repos.d/CentOS-Base.repo ####
- [base]:容器名称，一定要放在[]中  
- name:容器说明，可以自己随意写  
- mirrorlist:镜像站点，可以注释掉  
- baseurl:yum源服务器的地址。默认是CentOS官方的yum源服务器，是可以使用的，可以改成自己喜欢的yum源地址  
- enabled:不写或写成enable=1都是生效，写成enable=0就是不生效  
- gpgcheck:如果是1是指RPM的数字证书生效，如果是0则不生效  
- gpgkey:数字证书的公匙文件保存位置。不用修改
#### yum命令 ####
#### 查询 ####  
$yum list(查询所有可用软件包列表)  
$yum search 关键字（搜索服务器上所有关键字相关的包）  
#### 安装 #### 
$yum -y install 包名  
选项：  
install 安装  
-y　　自动回答yes  
#### 升级 ####
$yum -y update 包名  
选项：  
update 升级  
-y　　　自动回答yes
#### 卸载 ####
$yum -y remove 包名  
选项：  
remove  卸载  
-y　　　自动回答yes
### yum软件组命令管理 ###
$yum grouplist  
列出所有可用的软件组列表  
$yum groupinstall 软件组名  
安装指定软件组，组名可以有grouplist查询出来  
$yum groupremove 软件组名  
卸载指定软件组名
## 关盘yum源搭建 ##
1.挂载光盘：  
mount /dev/cdrom /mnt/cdrom/  
2.让网络yum源文件失效：  
cd /etc/yum.repos.d  
mv CentOS-Base.repo \CentOS-Base.repo.bak  
mv CentOS-Base-Debuginfo.repo \ CentOS-Base-Debuginfo.repo.bak  
mv CentOs-Vault.repo \ CentOs-Vault.repo.bak  
3.修改光盘yum源文件：  
vim CentOS-Media.repo  
name=CentOS-$releasever - Media  
baseurl=file:///mnt/cdrom  
//地址为光盘挂载地址
// file:///media/cdrom  
// file:///media/cdrecorder  
//注释这两个不存在的地址  
gpgcheck=1  
enabled=1  
//把enabled=0改成enabled=1，让这个yum源配置文件生效  
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-6  

### RPM包安装位置 ###
/etc/ 默认安装位置  
/usr/bin/ 可执行的命令安装目录  
/usr/share/doc/ 基本的软件使用手册保存位置  
/usr/share/man/ 帮助文件保存位置  
### 源码包安装位置 ###
安装在指定位置当中，一般是：/usr/local/软件名/
### 安装不同位置带来的的影响 ###
RPM包安装的服务可以使用系统服务管理命令(service)来管理，例如RPM包安装的apache的启动方法是：  
1. /etc/rc.d/init.d/httpd start  
2. service httpd start  
源码包安装的服务则不能被服务管理命令管理，因为没有安装到默认路径中。所以只能用绝对路径进行服务的管理，如：  
/usr/local/apache2/bin/apachectl start  
### 源码包安装过程 ###
#### 安装准备 ####
1. 安装C源编译器
2. 下载源码包：http://mirror.bit.edu.cn/apache/httpd/
#### 安装注意事项 ####
1. 源代码保存位置：/usr/local/src/
2. 软件安装位置：/usr/local/
3. 如何确定安装过程报错：  
	1.安装过程停止  
	2.出现error、warning或no的提示
#### 安装过程 ####
1. 下载源码包
2. 解压缩下载的源码包
3. 进入解压缩目录 
4. ./configure软件配置与检查  
	1.定义需要的功能选项。  
	2.检测系统环境是否符合安装要求。  
	3.包定义好的功能选项和检测系统环境的信息都写入Makefile文件，用于后续的编辑
5. make 编译  
	make clean 清空编译（编译报错的时候使用此命令）
6. make install 编译安装
#### 源码包的卸载 ####
不需要卸载命令，直接删除安装目录即可。不会遗留任何垃圾文件
### 脚本安装包 ###
- 脚本安装包并不是独立的软件包类型，常见的安装的是源码包
- 认为把安装过程携程了自动安装的脚本，只要执行脚本，定义简单的参数，就可以完成安装
- 非常类似于windows下软件的安装方式
## 用户和用户管理 ##
### 用户配置文件 ###
#### 用户信息文件 /etc/passwd ####
#### /etc/passwd ####  
- 第一字段：用户名称  
- 第二字段：密码标志  
- 第三字段：UID(用户ID)  
0：超级用户  
1-499：系统用户（伪用户）  
500-65535：普通用户  
- 第4字段：GID(用户初始组ID)  
- 第5字段：用户说明  
- 第6字段：家目录  
	普通用户：/home/用户名/  
	超级用户：/root/	   
- 第7字段：登陆之后的shell
初始组：就是指用户一登陆就立刻拥有这个用户组的相关权限，每个用户的出十足只能有一个，一般就是和这个用户的用户名相同的组名作为这个用户的初始组。  
附加组：指用户可以加入多个其他的用户组，并拥有这些组的权限，附加组可以有多个
shell：  
- linux的命令解释器。  
- 在/etc/passwd当中，除了标准shell是/bin/bash之外，还可以写如sbin/nologin
#### 影子文件/etc/shadow ####
- 第1字段：用户名  
- 第2字段：加密密码  
	加密算法升级为SHA512散列加密算法
	如果密码位是"!!"或"*"代表没有密码，不能登陆
- 第3字段：密码最后一次修改日期
	使用1970.1.1作为标准日期，每过一天时间戳加1
- 第4字段：两次密码的修改间隔时间（和第3字段相比）
- 第5字段：密码有效期（和第3字段相比）
- 第6字段：密码修改到期前的警告天数（和第5字段相比）
- 第7字段：密码过期后的宽限天数（和第5字段相比）
	0：代表密码过期后立刻失效
	-1：代表密码永远不会失效。
- 第8字段：账号失效时间
	要用时间戳表示
- 第9字段：保留 
时间戳换算：  
- 把时间戳换算为日期  
	date -d "1970-01-01 16066 days"  
- 把日期换算为时间
	echo $($(date --date="2017/01/01"+%s)/86400+1)
#### 组信息文件/etc/group和组密码文件/etc/gshadow ####
#### 组信息文件/etc/group ####
- 第一字段：组名
- 第二字段：组密码标志
- 第三字段：GID
- 第四字段：组中附加用户
#### 组密码文件/etc/gshadow ####
- 第一字段：组名
- 第二字段：组密码标志
- 第三字段：组管理员用户名
- 第四字段：组中附加用户
### 用户管理相关文件 ###
- 普通用户：/home/用户名/，所有者和所属组都是此用户，权限是700
- 超级用户：/root/，所有者和所属组都是root用户，权限是500  
### 用户管理命令 ###
#### 用户添加命令 useradd ####
#### 格式 ####
useradd [选项] 用户名  
选项：
-u UID:手工指定用户的UID号    
-d 家目录：手工指定用户的家目录   
-c 用户说明：手工指定用户的说明  
-g 组名：手工制动用户的初始组  
-G 组名：指定用户的附加组  
-s shell：手工指定用户的登陆shell。默认是/bin/bash
#### 默认添加用户 ####  
\# useradd sc  
- \# grep sc /etc/passwd  
- \# grep sc /ect/shadow  
- \# grep sc /ect/group  
- \# grep sc /ect/gshadow  
- \# ll -d /home/aaa  
- \# ll /var/spool/mail/aaa  
#### 指定选项添加用户 ####
useradd -u 550 -G root,bin -d /home/aaa1 \        　//"\"为换行符　
-c "test user" -s /bin/bash sc  
#### 用户默认值文件 ####
/etc/default/useradd  
- GROUP=100  \#用户默认组文件  
- HOME=/home \#用户家目录  
- INACTIVE=-1  \#密码过期宽限天数（shadow文件7字段） 
- EXPIRE=  \#密码失效时间（8）  
- SHELL=/bin/bash \#默认shell  
- SKEL=/etc/skel \#模板目录  
- CREATE_MAIL_SPOOL=yes \#是否建立邮箱
/etc/login.defs  
- PASS_MAX_DAYS 99999 \#密码有效期（5）  
- PASS_MIN_DAYS 0 \#密码膝盖间隔（4）  
- PASS_WARN_AGE 7 \#密码最小5位（PAM）  
- UID_MIN  500 \#密码到期警告（6）  
- GID_MIN  60000 \#最小和最大UID范围  
- ENCTYPT_METHOD  SHA512 \#加密模式
#### passwd命令格式 ####
\#passwd [选项] 用户名  
选项：  
-S  :查询用户密码的密码状态。仅root用户可用。  
-l  :暂时锁定用户。仅root用户可用
-u  :解锁用户。仅root用户可用  
--stdin ：可用通过管道符输出的数据作为用户的密码  
查看密码状态：  
\#passwd -S aaa  
aaa PS 2017-08-06 0 99999 7 -1  
*\#用户名 密码设定时间（2017-08-06） 密码修改时间间隔（0）  
\#密码有效期（99999） 警告时间（7） 密码不失效（-1）*  
锁定用户和解锁用户：  
\#passwd -l aaa  
\#passwd -u aaa  
使用字符串作为用户的密码：  
\# echo "123" | passwd --stdin aaa  
### 修改用户信息usermod/密码状态chage ###
#### usermod ####
\# usermod [选项] 用户名  
选项：  
-u UID:修改用户的UID  
-c 用户说明：修改用户的说明信息  
-G 组名：修改用户的附加组  
-L:临时锁定用户（Lock）  
-U:解锁用户锁定（Unlock）  
例如：  
\# usermod -c "test user" aaa  *#修改用户的说明*  
\# usermod -G root aaa *#把aaa用户加入root组*  
\# usermod -L aaa *#锁定用户*  
\# usermod -U aaa *#解锁用户*  
#### chage ####
\chage [选项] 用户名  
选项：  
-l：列出用户的详细密码状态  
-d 日期：修改密码最后一次更爱日期（shadow3字段）  
-m 天数：两次密码修改间隔（4字段）  
-M 天数：密码有效期（5字段）  
-W 天数：密码过期钱警告天数（6字段）  
-I 天数：密码过后宽限天数（7字段）  
-E 日期：账号时间时间（8字段）  
例如：  
\#chage -d 0 aaa  
*\#把密码修改日期归0，用户一登陆就要修改密码*
### 删除用户userdel/用户切换命令su ###  
#### userdel ####
\# userdel [-r] 用户名  
选项：-r：删除用户的同时，删除用户家目录  
手动删除用户：  
- vi /etc/passwd  
- vi /etc/shadow  
- vi /etc/group  
- vi /etc/gshadow  
- rm -rf /var/spool/mail/aaa  
- rm -rf /home/aaa  
查看用户ID  
\# id 用户名  
#### su ####
\su [选项] 用户名  
选项：  
-：选项只使用"-"代表连带用户的环境变量一起切换  
-c 命令：仅执行一次命令，而不用切换用户身份  
例如：  
$su - root *#切换成root * 
su - root -c "useradd user3"  *#不切换成root，但是执行useradd命令添加user3用户*
## 权限管理 ##
### ACL权限 ###
简介：类似windows系统权限控制，找到用户，给用户增加权限就可以了。  
查看分区ACL权限是否开启：  
\# dumpe2fs -h 指定分区    
*\#dumpe2fs命令是查询指定分区详细文件系统信息的命令*  
选项：  
-h:仅显示超级块中信息，而不显示磁盘块组的详细信息  
Default mount options:user_xattr acl \#显示支持acl权限  
#### 临时开启分区ACL权限 ####
\# mount -o remount,acl /  
*\#重新挂载根分区，并挂载加入acl权限*
#### 永久开启分区ACL权限 ####
\# vi /etc/fstab  
UUID=e2ca6f57-b15c-43ea-bca0-f239083d8bd2 / ext4 defaults,acl 1 1 
*\#加入acl*
\mount -o remount /  
*\#重新挂载文件系统和重启动系统，使修改生效*
#### 查看/设定ACL权限 ####
查看：  
\# getfacle 文件名 #查看acl权限  
设定：
\# setfacl 选线 文件名  
选项：  
-m:设定ACL权限  
-x:删除指定的ACL权限  
-b:删除所有的ACL权限  
-d:设定默认ACL权限  
-k:删除默认ACL权限  
-R:递归设定ACL权限  
给用户设定acl权限  
1. #useradd zhangsan  
2. #useradd lisi  
3. #useradd st  
4. #groupadd tgroup  
5. #mkdir /project  
6. #chown root:tgroup /project/  
7. #chmod 770 /project/  
8. #setfacl -m u:st:rx /project/  
\#给用户st赋予r-x权限，使用"u:用户名：权限"格式  
给用户组设定acl权限  
1. #groupadd tgroup2  
2. #setfacl -m g:group2:rwx project/  
*\#为组tgroup2分配acl权限。使用"g:组名：权限"格式*
#### 最大有效权限 ####
最大有效权限mask  
mask是用来指定最大有效权限的。如果给用户赋予了acl权限，是需要和mask的权限"相与"才能得到用户的真正权限  
修改最大有效权限  
\# setfacl -m m: 文件名  
*\#设定mask权限为r-x。使用"m：权限"格式*
#### 删除ACL权限 ####
删除acl权限  
\# setfacl -x u:用户名 文件名  
*\#删除指定用户的acl权限*  
\# setfacl -x g:组名 文件名  
*\#删除指定用户组的acl权限*
#### 默认/递归acl权限 ####
递归acl权限  
- 递归是父目录在设定acl权限时，所有的子文件和子目录也会拥有相同的ACL权限  
- setfacl -m u:用户名:权限 -R 文件名  
默认acl权限  
- 默认acl权限的作用是如果给父目录设定了默认acl权限，那么父目录中所有的新建子文件都会集成父目录的acl权限。  
- setfacl -m d:u:用户名：权限 文件名  
### 文件特殊权限 ###
#### SetUID ####
功能：  
- 只有可以执行的二进制程序才能设定SUID权限  
- 命令执行者要对该程序拥有x（执行）权限  
- 命令执行者在执行该程序时获得该程序文件属主的身份（在执行程序的过程中成为文件的属主）
- SetUID权限只在该程序执行过程中有效，也就是说身份改变只在程序执行过程中有效  
- passwd命令拥有SetUID全下，所以普通可以修改自己的密码  
\# ll usr/bin/passwd  
-rwsr-xr-x. l root root 25980 2月 22 2012 /usr/bin/passwd  
- cat命令没有SetUID权限，所以普通用户不能查看/etc/shadow文件内容  
\# ll /bin/cat  
-rwxr-xr-x root root 47976 6月 22 2012 /bin/cat  
设定SetUID的方法  
- 4代表SUID  
	chmod 4755 文件名  
	chmod u+s 文件名  
取消SetUID的方法  
- chmod 755 文件名  
- chmod u-s 文件名  
危险的SetUID  
- 关键目录应严格控制写权限。比如"/"、"/usr"等  
- 用户的密码设置要严格遵守密码三原则  
- 对系统中默认应该具有SetUID权限的文件做一列表，定时检查有没有这之外的文件被设置的SetUID权限
#### SetGID ####
SetGID针对文件的作用  
- 只有可执行的二进制程序才能设置SGID权限  
- 命令执行者要对该程序拥有x（执行）权限  
- 命令执行在执行程序的时候，组身份升级为该程序文件的属组  
- SetGID权限童谣只在该程序执行过程中有效，也就是说组身份改变只在程序执行过程中有效  
\# ll /usr/bin/locate  
-rwx--s--x l root slocate 35612 8月 24 2010 /usr/bin/locate  
\# ll /var/lib/mlocate/mlocate.db  
-rw-r----- l root slocate 1838850 1月 20 04:29 /var/lib/mlocate/mlocate.db  
- /usr/bin/locate是可执行二进制程序，可以赋予SGID  
- 执行用户aaa对/usr/bin/locate命令拥有执行权限  
- 执行/usr/bin/locate命令时，组身份会升级为slocate组，而slocate组对/var/lib/mlocate/mlocate.db数据库拥有r权限，所以普通用户可以使用locate命令查询mlocate.db数据库  
- 命令结束，aaa用户的组身份返回为aaa组  
SetGID针对目录的作用  
- 普通用户必须对此目录拥有r和x权限，才能进入此目录  
- 普通用户在此目录中的有效组会变成此目录的属组  
- 若普通用户对此目录拥有w权限时，新建的文件的默认属组是这个目录的属组  
设定SetGID  
- 2代表SUID  
	chmod 2755 文件名  
	chmod g+s 文件名  
例如：  
\# cd /tmp/  
\# mkdir dtest  
\# chmod g+s dtest  
\# ll -d dtest/  
\# chmod 777 dtest/  
\# su -aaa  
$ cd /tmp/dtest/  
$ touch abc  
$ ll
取消SetGID  
- chmod 755 文件名  
- chmod g-s 文件名
#### Sticky BIT ####
SBIT 粘着位作用  
- 粘着位目前只对目录有效  
- 普通用户对该目录拥有w和x权限  
- 即普通用户可以在此目录拥有写入权限  
- 如果没有粘着位，因为普通用户拥有w权限，所以可以删除此目录下所有文件，包括其他用户建立的文件。一旦赋予的粘着位，除了root可以删除所有文件，普通用户就算拥有w权限，也只能删除自己建立的文件，但是不能删除其他用户建立的文件  
\# ll -d /tmp/  
drwxrwxrwt. 3 root root 4096 12月 13 11:22 /tmp/  
设置与取消粘着位  
设置：  
- chmod 1755 目录名  
- chmod o+t 目录名  
取消：  
- chmod 777 目录名  
- chmod o-t 目录名  
### 文件系统属性chattr权限 ###
#### chattr命令格式 ####
\# chattr [+-=] [选项] 文件或目录名  
+：增加权限  
-：删除权限  
=：等于某权限  
选项：  
- i：如果对文件设置i属性，那么不允许对文件进行删除改名，也不能添加和修改数据；如果对目录设置i属性，那么只能修改目录下文件的数据，但不允许简历和删除文件。  
- a：如果对文件设置a属性，那么只能在文件中增加数据，但是不能删除也不能修改数据；如果对目录设置a属性，那么只允许在目录中建立和修改文件，但是不允许删除  
#### 查看文件系统属性 ####
\# lsattr 选项 文件名  
选项：  
-a：显示所有文件和目录  
-d：若目标是目录，仅列出目录本身的属性，而不是子文件的
#### 系统命令sudo权限 ####  
#### sudo权限 ####
- root把本来只能超级用户执行的命令赋予普通用户执行
- sudo的操作对象是系统命令  
#### sudo使用 ####
\# visudo  
*\#实际修改的是/etc/sudoers文件*
root ALL=(ALL) ALL  
*\#用户名 被管理主机的地址=（可使用的身份） 授权命令（绝对路径）*  
\#%wheel ALL=(ALL) ALL  
*\#%组名 被管理主机的地址=（可使用的身份） 授权命令（绝对路径）*  
#### 授权sc用户可以重启服务器 ####
\# visudo  
sc ALL=/sbin/shutdown -r now
#### 普通用户执行sudo赋予的命令 ####
\# su -sc  
$sudo -l  
*\#查看可用的sudo命令*  
$ sudo /sbin/shutdown -r now  
*\#普通用户执行sudo赋予的命令*



9.1


# shell基础 #
## shell概述 ##
把abcd翻译成计算机能识别的0101的命令。再把计算机的0101的命令翻译成abcd命令  

- shell是一个命令行解释器，为用户提供跟一个向linux内核发送请求以便运行程序的界面系统级程序，用户可以用shell来启动、挂起、停止甚至编写一些程序
- shell是一个功能强大的编程语言，易编写，易调试，灵活性强。shell是解释执行的脚本语言，在shell中可以直接调用linux系统命令
### shell分类 ###
- Bourne Shell：主文件名为sh  
- Bash:Bash与sh兼容，现在使用的Linux就是使用Bash作为用户的基本shell
### Linux支持的Shell ###
- /etc/shells
## shell脚本的执行方式 ##
#### echo 输出命令 ####
\# echo [选项] [输出内容]  
选项：  
-e 支持反斜线控制的字符转换  
<tr>
	<td>控制字符&nbsp;</td>
	<td>作用</td>
</tr>
<tr>
	<td>\\</td>
	<td>输出\本身</td>
</tr>
<tr>
	<td>\a</td>
	<td>输出警告音</td>
</tr>
<tr>
	<td>\b</td>
	<td>退格键，也就是向左删除键</td>
</tr>
<tr>
	<td>\c</td>
	<td>取消输出行末的换行符。和"-n"选项一致</td>
</tr>
<tr>
	<td>\e</td>
	<td>ESCAPE键</td>
</tr>
<tr>
	<td>\f</td>
	<td>换页键</td>
</tr>
<tr>
	<td>\n</td>
	<td>换行键</td>
</tr>
<tr>
	<td>\r</td>
	<td>回车键</td>
</tr>
<tr>
	<td>\t</td>
	<td>制表符，也就是table键</td>
</tr>
<tr>
	<td>\v</td>
	<td>垂直制表符</td>
</tr>
<tr>
	<td>\0nnn</td>
	<td>按照八进制ASCⅡ码表输出字符，其中0为数字0，nnn是三位八进制数</td>
</tr>
<tr>
	<td>\0xhh</td>
	<td>按照十六进制ASCⅡ码表输出字符，其中hh是两位十六进制数</td>
</tr>

### 第一个脚本 ###
\# vi hello.sh  
\#!/bin/Bash　　//表示shell脚本，不能省略 
\#The first program
\# Author: 

echo -e "hello world"
### 脚本执行 ###
- 赋予执行权限，直接允许
	- chmod 755 hello.sh
	- ./hello.sh
- 通过Bash调用执行脚本
	- Bash hello.sh
## Bash的基本功能 ##
### 历史命令与命令补全 ###
\# history [选项] [历史命令保存文件]  
选项：  
-c:清空历史命令  
-w:把缓存中的历史命令写入历史命令保存文件~/.bash_history  

- 历史命令默认会保存1000条，可以在环境变量配置文件/etc/profile中修改  
#### 历史命令的调用 ####
- 使用上、下箭头调用以前的历史命令
- 使用"!n"重复执行第n条历史命令
- 使用"!!"重复执行上一条命令
- 使用"!字串"重复执行最后一条以该字串开头的命令  
例如：前面执行了：#server network restart，在后面的命令中输入"!ser"就会执行最有一条以"ser"开头的命令
#### 命令与文件补全 ####
- 在Bash中，命令与文件补全是非常方便与常用的功能，只要在输入命令或文件时，按"tab"键就会自动进行补全
### 命令别名与常用快捷键 ###
#### 命令别名 ####
\# alias 别名="原命令" //设定命令别名  
\# alias //查询命令别名   
#### 命令执行时顺序 ####
1. 第一顺位执行用绝对路径或相对路径的命令  
2. 第二顺位执行别名  
3. 第三顺位执行Bash的内部命令。
4. 第四顺位执行按照$PATH环境变量定义的目录查找顺序找到的第一个命令。 
#### 让别名永久生效 ####
\ vi /root/.bashrc  
#### 删除别名 ####
\# unalias 别名  
#### bash常用快捷键 ####  
<tr>
	<td>快捷键&nbsp;</td>
	<td>作用</td>
</tr>
<tr>
	<td>crtl+A</td>
	<td>把光标移动到命令行开头。如果我们输入的命令过长，想把光标移动到命令行开头时使用</td>
</tr>
<tr>
	<td>crtl+E</td>
	<td>把光标移动到命令行结尾</td>
</tr>
<tr>
	<td>crtl+C</td>
	<td>强制终止当前的命令。</td>
</tr>
<tr>
	<td>crtl+L</td>
	<td>清屏，相当于clear命令</td>
</tr>
<tr>
	<td>crtl+U</td>
	<td>删除或剪切光标之前的命令</td>
</tr>
<tr>
	<td>crtl+K</td>
	<td>删除或剪切光标之后的命令</td>
</tr>
<tr>
	<td>crtl+Y</td>
	<td>粘贴Ctrl+u或ctrl+k剪切的内容</td>
</tr>
<tr>
	<td>crtl+R</td>
	<td>在历史命令中搜索，按下ctrl+R后，就会出现搜索界面。只要输入搜索命令，就会从历史命令中搜索</td>
</tr>
<tr>
	<td>crtl+D</td>
	<td>退出当前终端</td>
</tr>
<tr>
	<td>crtl+Z</td>
	<td>暂停，并放入后台。这个快捷键牵扯工作管理的内容</td>
</tr>
<tr>
	<td>crtl+S</td>
	<td>暂停屏幕输出</td>
</tr>
<tr>
	<td>crtl+Q</td>
	<td>恢复屏幕输出</td>
</tr>
 	
