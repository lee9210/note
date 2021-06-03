### SqlSessionFactory初始化 ###
创建SqlSession会话的工厂类，默认使用DefaultSqlSessionFactory创建。
1. 读取xml配置文件转换成InputStream
2. XMLConfigBuilder将InputStream转换成可读取配置（这儿主要使用的是parse()函数，转换成Configuration）
3. 使用转换成的Configuration创建SqlSessionFactory（转换成DefaultSqlSessionFactory）

主要使用的模式：建造者模式


































































123