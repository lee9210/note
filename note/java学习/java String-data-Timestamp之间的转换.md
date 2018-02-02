#### String转data ####
````
String time = "2017-12-17 07:59:59";
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
Date dateTime = sdf.parse(time);
System.out.println(dateTime);
````
结果为一个时间戳
````
Sun Dec 17 07:59:59 CST 2017
````
#### string转timesamp ####
````
String time = "2017-12-17 07:59:59";
Timestamp timestamp = Timestamp.valueOf(time);
System.out.println(timestamp);
````
运行结果为：
````
2017-12-17 07:59:59.0
````
#### Date转String ####
````
Date now = new Date();
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
String nowString = sdf.format(now);
System.out.println(nowString);
````
运行结果为：
````
2017-12-17 22:11:04
````
#### Date转Timestamp ####
date不能直接转换成timestamp，需要借助string
````
Date now = new Date();
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
String nowString = sdf.format(now);
Timestamp timestamp = Timestamp.valueOf(nowString);
System.out.println(timestamp);
````
运行结果：
````
2017-12-17 22:14:40.0
````

#### Timestamp转date ####
Timestamp类是date的子类
````
Timestamp ts = new Timestamp(System.currentTimeMillis());
Date now = ts;
System.out.println(now);
````
#### Timestamp转String ####
````
Timestamp ts = new Timestamp(System.currentTimeMillis());
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
String now = sdf.format(ts);
System.out.println(now);
````
运行结果：
````
2017-12-17 22:23:12
````

#### date转long ####
````
Date now = new Date();
long nowLong = now.getTime();
System.out.println(nowLong);
````
运行结果：
````
1513520743554
````
#### long转date ####
````
long now = 1513520743554L;
Date nowDate = new Date(now);
System.out.println(nowDate);
````
运行结果：
````
Sun Dec 17 22:25:43 CST 2017
````
#### long转String ####
````
SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
long now  = 1513520743554L;
String nowString = sdf.format(now/1000);
System.out.println(nowString);
````
#### 获取当前时间 ####
````
Calendar now = Calendar.getInstance();
int year = now.get(Calendar.YEAR);
int month = now.get(Calendar.MONTH)+1;
int day = now.get(Calendar.DAY_OF_MONTH);
int hour = now.get(Calendar.HOUR_OF_DAY);
int minite = now.get(Calendar.MINUTE);
long second = now.getTimeInMillis();
````