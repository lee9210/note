### 网络基础 ###
#### 什么是计算机网络 ####
把分布在不同地理区域的计算机与专门的外部设备用通信线路互连成一个规模大、功能强的网络系统，从而使众多的计算机可以方便的互相传递信息，共享硬件、软件、数据信息等资源。  
 
#### 计算机网络的主要功能 ####
- 资源共享
- 信息传输与集中处理
- 均衡负荷与分布处理
- 综合信息服务（www/综合业务数字网络ISDN）
#### 网络通信协议 ####
计算机网络中实现通信必须有一些约定即通信协议，对速率、传输代码、代码结构、传输控制步骤、出错控制等标准定制
#### 网络通信接口 ####
为了使两个结点之间能进行通话，必须在他们之间简历通信工具（即接口），使彼此之间能进行信息交换。接口包括两部分：
- 硬件设置：实现结点之间的信息传送
- 软件设置：规定双方进行通信的约定协议	 

#### 通信协议分层的思想 ####
分层：由于结点之间练习和复杂，在指定协议时，把发杂成份分解成一些简单的成份，再将他们复合起来。常用的复合方式是层式方式，即同层间可以通信、上一层可以调用下一层，而与再下一层不发生关系。各层互不影响，利于系统的开发和扩展。
分层规定：把用户应用程序作为最高层，把物理通信线路作为最低层，将其间的协议处理分为若干层，规定每层处理的任务，也规定每层的接口标准。
第五层<------第五层协议----->第五层
第四层<------第四层协议----->第四层
第三层<------第三层协议----->第三层
第二层<------第二层协议----->第二层
第一层<------第一层协议----->第一层
-------------物理介质------------

### TCP/IP协议 ###
IP提供的主要功能有：
- 无线数据报传送
- 数据报路由选择和差错控制
#### TCP和UDP ####
TCP:
专门设计用于在不可靠的因特网上提供可靠的端到端的字节流通信的协议。它是一种面向连接的协议。TCP连接是字节流而非报文流
UDP：
向应用程序提供了一种发送封装的原始ip数据报的方法、并且发送时候无需建立连接。是一种不可靠的连接
### IP地址 ###
### socket通信 ###
- 两个java应用程序可以通过一个双向的网络通信连接实现数据交换，这个双向链路的一端称为一个socket。
- socket通常用来实现client-server连接。
- java.net包中定义的两个类socket和serversocket，分别用来实现双向连接的client和server端
- 建立连接时所需的地址信息为远程计算机的ip地址和端口号（port number）
	- TCP和UDP端口是分开的，每一个有65536个端口

服务器端：
````
package com.socket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TestServer {
    public static void main(String[] args) throws Exception{
        ServerSocket serverSocket = new ServerSocket(6666);//开放端口，等待连接
        while (true){
            Socket socket = serverSocket.accept();//接收客户端连接信息。accept（）是阻塞式的
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            String data = dataInputStream.readUTF();//readUTF()阻塞式
            System.out.println(data);
            System.out.println("a client accept！");
            OutputStream outputStream = socket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeUTF("hello client");
            dataOutputStream.flush();
        }
    }
}

````
客户端：
````
package com.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TestClient {
    public static void main(String[] args) throws Exception{
        Socket socket = new Socket("127.0.0.1",6666);//申请和服务器连接
        OutputStream outputStream = socket.getOutputStream();//输出管道
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeUTF("hello server");
        InputStream inputStream = socket.getInputStream();
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        String input = dataInputStream.readUTF();
        System.out.println(input);
        dataOutputStream.flush();
        dataOutputStream.close();
        socket.close();
    }
}

````