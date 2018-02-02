### 注解基本概念 ###
#### 元数据 ####
元数据就是数据的数据。也就是说，元数据是描述数据的。就像数据表中的字段一样，每个字段描述了这个字段下的数据的含义。
元数据可以用于创建文档，跟踪代码中的依赖性，甚至执行基本编译时检查。许多元数据工具，如XDoclet，将这些功能添加到核心java语言中，暂时称为java编程功能的一部分。
一般来说，袁术的好处分为三类：文档编制，编译器检查和代码分析。代码级文档最常为引用。元数据提供了一种有用的方法来指明方法是否取决于其他方法，他们是否完整，特定类是否必须引用其他类，等待。
#### 什么是注解 ####
java中的注解就是java源代码的元数据，也就是说注解是用来描述java源代码的，基本语法就是@后面跟注解的名称。有预定义注解，也有自定义注解
### JAVA中预定义注解 ###
#### Override ####
标识某一个方法是否正确覆盖了它的父类的方法。
````
@Override
public int hashCode(){...}
````
#### Deprecated ####
作用：标识已经不建议使用这个类成员
这个注解是一个标记注解。
````
@Deprecated
public int getUserName(){...}
````
#### SuppressWarnings ####
作用：用来抑制警告信息。
````
@SuppressWarnings(value = {"unchecked"})
public static void test(){List list= new ArrayList();}
````
### 自定义注解 ###
#### 无成员自定义注解 ####
````
public @interface MyAnno{}
````
使用这个注解的代码如下
````
@MyAnno
public class UserModel{}
````
#### 添加成员 ####
注解是用来描述源代码的数据，所以通常需要为注解提供数据成员。定义数据成员后不需要分别定义访问和修改的方法。相反，只需要定义一个方法，以成员的名称命名它。数据类型应该是方法返回值的类型。
````
public @interface MyAnno{
	String schoolName();
}
````
使用方法：
````
@MyAnno(schoolName = "university")
public class UserModel{}
````
设置默认值：
````
public @interface MyAnno{
	String schoolName() default "university";
}
````
使用方法：
````
@MyAnno
public class UserModel{}
````
### 对注解的注解 ###
#### 指定Target ####
最明显的元注释就是允许何种程序元素具有定义的注解类型。这种元注释被称为Target。表示注解的作用域。
````
public enum ElementType {
    TYPE,//类,接口
    FIELD,//字段声明
    METHOD,//方法声明
    PARAMETER,//参数声明
    CONSTRUCTOR,//构造方法
    LOCAL_VARIABLE,//局部变量声明
    ANNOTATION_TYPE,//注解
    PACKAGE,//包声明
    TYPE_PARAMETER,//类型参数声明
    TYPE_USE//标注任意类型(不包括class)
}
````
#### 设置保持性Retetion ####
这个元注解和java编译器处理注解的注解烈性的方式有关。编译器有几种不同的选择：
1. RUNTIME：将注解保留在编译后的类文件中，并在第一次加载类时读取它。
2. CLASS：将注解保留在编译后的类文件中，但是在运行时忽略它。
3. SOURCE：按照规定使用注解，但是并不将它暴露到编译后的类文件中。
#### 添加公共文档Document ####
生成javadoc的时候会包含注解的信息

#### Inherited ####
父类的注解并不会被子类继承。如果要继承，就加上Inherited
Inherited允许子类继承

### 读取注解 ###
要读取注解的内容，就需要使用反射的技术。

````
import java.lang.annotation.*;

@Target({ElementType.METHOD,ElementType.TYPE})
/**
 * tagete表示注解的作用域。ElementType包含的有
 * CONSTRUCTOR:构造方法
 * FIELD:字段声明
 * LOCAL_VALUE:局部变量声明
 * METHOD:方法声明
 * PACKAGE:包声明
 * PARAMETER:参数声明
 * TYPE:类接口
 */

@Retention(RetentionPolicy.RUNTIME)
/**
 * Retention生命周期，包含的有
 * SOURCE:只在源码显示，编译时会丢弃
 * CLASS:编译时会记录到class中，运行时忽略
 * RUNTIME:运行时存在，可以通过反射读取
 */
@Inherited
/**
 * Inherited允许子类继承
 */
@Documented
/**
 * 生成javadoc的时候会包含注解的信息
 */
public @interface Description {
    String desc() default "up";
    String author();
    int age() default 18;
}

````
### 练习 ###
需求：
1. 有一张用户表，字段包括用户id，用户名，昵称，年龄，性别，所在城市，邮箱，手机号。
2. 方便对每个字段或字段的组合条件进行检索，并打印出sql
3. 使用方式要足够简单。

主要测试类：
````
package com.zhu.zhutest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Test {
    public static void main(String[] args){
        UserInfo user1 = new UserInfo();
        user1.setId(10);//查询用户id为10的用户

        UserInfo user2 = new UserInfo();
        user2.setUserName("tom");//模糊查询用户名为tom的用户

        UserInfo user3 = new UserInfo();
        user3.setEmail("abc@qq.com");//查询邮箱为其中任意一个的用户

        String sql1 = query(user1);
        String sql2 = query(user2);
        String sql3 = query(user3);

        System.out.println(sql1);
        System.out.println(sql2);
        System.out.println(sql3);
    }

    public static String query(UserInfo user){
        StringBuffer sql = new StringBuffer();
        //1.获取class
        Class c = user.getClass();

        //2.获取table的名字。检查是否是一个table的对象
        boolean exist = c.isAnnotationPresent(Table.class);//判断是否是有table注解
        if(!exist){
            return null;
        }else{
            Table t = (Table)c.getAnnotation(Table.class);//获取table的值
            String tableName = t.value();
            sql.append("select * form ").append(tableName).append("where 1=1");
        }
        //3.遍历所有字段
        Field[] fields = c.getDeclaredFields();//获取属性值
        for(Field field:fields){
            //4.处理每个字段对应的sql
            //4.1 拿到字段名
            boolean columnExist = field.isAnnotationPresent(Column.class);//是否有注解
            if(!columnExist){
                continue;
            }
            Column column = field.getAnnotation(Column.class);//获取属性值的注解的属性
            String columnName = column.value();//获取column的值
            //4.2 拿到字段值
            String fieldName = field.getName();//获取属性值
            String getMethodName = "get"+ fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);//获取get方法名
            Object fieldValue = null;
            try {
                Method getMethod = c.getMethod(getMethodName);//通过方法名获取get方法
                fieldValue = getMethod.invoke(user);//通过get方法获取值；
            } catch (Exception e) {
                e.printStackTrace();
            }
            //4.3 拼装sql
            if(fieldValue == null ||(fieldValue instanceof Integer && (Integer)fieldValue== 0)){
                continue;
            }else if(fieldValue instanceof String){
                if(fieldValue.toString().contains(",")){
                    String[] values = fieldValue.toString().split(",");
                    sql.append(" and ").append(columnName).append(" in (");
                    for(String value : values){
                        sql.append("'").append(value).append("'").append(",");
                    }
                    sql.deleteCharAt(sql.length()-1);
                    sql.append(")");
                }else {
                    sql.append(" and ").append(columnName).append(" like ").append("'").append(fieldValue).append("'");
                }
            }
        }
        return sql.toString();
    }
}
````
entity：
````
package com.zhu.zhutest;

@Table("user")
public class UserInfo {
    @Column("id")
    private int id;

    @Column("user_name")
    private String userName;

    @Column("nick_name")
    private String nickName;

    @Column("age")
    private int age;

    @Column("city")
    private String city;

    @Column("sex")
    private String sex;

    @Column("email")
    private String email;

    @Column("phone")
    private String phone;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
````
table注解：
````
package com.zhu.zhutest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    String value();
}
````
column注解：
````
package com.zhu.zhutest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    String value();
}
````

//md文件
