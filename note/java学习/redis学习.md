#### HelloWorld ####
````
import redis.clients.jedis.Jedis;

public class JedisTest {
	
	public static void main(String[] args){
		Jedis jedis = new Jedis("192.168.137.151",6379);//创建客户端，设置ip和端口
//		jedis.auth("1234560");//设置密码
		jedis.set("name","helloworld");//设置值
		String value = jedis.get("name");//获取值
		System.out.println(value);
		jedis.close();//释放链接资源
	}
}
````
运行结果
````
helloworld
````

### redis数据结构 ###






