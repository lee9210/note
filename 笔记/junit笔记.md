# junit断言调试函数 #

#### 断言：assertXXX方法 ####
assertArrayEquals("message",A,B):断言A数组和B数组相等
assertEquals("message",A,B):断言A对象和B对象相等，这个断言在比较两个对象时调用了equals()方法
assertSame("message",A,B):断言A对象与B对象相同。assert方法是检查A与B是否有相同的值（使用equals方法），而assertSame方法则是检查A与B就是同一个对象(使用的是==操作符)。
assertTure("message",A):断言A条件为真。
assertNotNull("message",A):断言A对象不为null。

## 运行参数化测试 ##
Parameterized(参数化)的测试运行器运行使用不同的参数多次运行同一个测试。
例如：
````
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(value = Parameterized.class)											//------1
public class ParameterizedTest {
    private double expected;													//------2
    private double valueOne;													//------2
    private double valueTwo;													//------2

    @Parameterized.Parameters													//------3
    public static Collection<Integer[]> getTestParameters(){					//------4
        return Arrays.asList(new Integer[][] {
                {2,1,1},
                {3,2,1},
                {4,3,1}
        });
    }
    
    public ParameterizedTest(double expected,double valueOne,double valueTwo){
        this.expected = expected;
        this.valueOne = valueOne;
        this.valueTwo = valueTwo;
    }

    @Test
    public void sum(){															//------5
        Calculator calculator = new Calculator();								//------6
        assertEquals(expected,calculator.add(valueOne,valueTwo),0);				//------7
    }

}
````
要使用Parameterized的测试运行器来运行一个测试类，就要满足以下要求。按照上注释
1. 测试类必须使用@RunWith注释，并且要将Parameterized类作为它的参数
2. 其次必须声明测试中所使用的实例变量
3. 同时提供一个用@Parameters注释的方法（次数提供的是getTestParameters方法）。此外，这个方法的签名必须是public static java.util.Collection，无任何参数。Collection元素必须是相同长度的数组。这个数组的长度必须要喝这个唯一的公共构造函数的参数数组数量相匹配。
4. 为测试指定需要的构造函数。参数的初始化方法
5. 实现@Test方法
6. 实例化程序
7. 用断言调用提供的参数。


## 用Suite组合测试 ##
如果没有提供一个自己的Suite，那么测试运行器会自动创建一个Suite。
默认的Suite会扫描测试类，找出所有以@Test注释的方法。默认的Suite会在内部为每个@Test方法创建一个测试类的实例。然后JUnit就会独立地执行每个@Test方法，以避免潜在的负面影响。

Suite对象其实是一个Runner，可以执行测试类中所有@Test注释的方法。














123