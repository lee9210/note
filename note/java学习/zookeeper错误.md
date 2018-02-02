Starting zookeeper ... /home/wusc/zookeeper-3.4.5/bin/zkServer.sh: line 113: /home/wusc/zookeeper-3.4.5/data/zookeeper_server.pid: Permission denied
FAILED TO WRITE PID
bash-4.1$ /home/wusc/zookeeper-3.4.5/bin/zkServer.sh: line 109: ./zookeeper.out: Permission denied

权限不足的问题
\# chmod a+wxr zookeeper-3.4.5/  
\# cd zookeeper-3.4.5/  
在zkServier.sh start 就可以了

主机不能访问虚拟机web项目
解决方案：
关闭虚拟机防火墙
/etc/init.d/iptables stop 