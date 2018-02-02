【程序1】   题目：古典问题：有一对兔子，从出生后第3个月起每个月都生一对兔子，小兔子长到第四个月后每个月又生一对兔子，假如兔子都不死，问每个月的兔子总数为多少？
数列为
1,1,2,3,5,8,13,21...
````
public static void main(String args[]) {
    int[] array = new int[10];
    array[0] = 1;
    array[1] = 1;
    for(int i=2;i<10;i++){
        array[i] = array[i-1]+array[i-2];
    }
    System.out.println(Arrays.toString(array));
}
````

【程序2】   题目：判断101-200之间有多少个素数，并输出所有素数。
````
package com.sort;

public class Sort02 {
    public static void main(String[] args){
        for(int i=101;i<200;i++){
            boolean sushu = true;
            int index = i-1;
            while (index>1){
                int result = i%index;
                if(result == 0){
                    sushu = false;
                    index = 0;
                }else{
                    index--;
                }
            }
            if(sushu){
                System.out.println(i);
            }
        }
    }
}
````

【程序3】   题目：打印出所有的 水仙花数 ，所谓 水仙花数 是指一个三位数，其各位数字立方和等于该数本身。例如：153是一个 水仙花数 ，因为153=1的三次方＋5的三次方＋3的三次方。
````
public class Sort03 {
    public static void main(String[] args){
        for(int i=100;i<1000;i++){
            String num = String.valueOf(i);
            int first = Integer.valueOf(num.substring(0,1));
            int second = Integer.valueOf(num.substring(1,2));
            int third = Integer.valueOf(num.substring(2,3));
            double result = Math.pow(first, 3)+Math.pow(second, 3)+Math.pow(third, 3);
            if(result == i){
                System.out.println(i);
            }
        }
    }
}
````


【程序5】   题目：利用条件运算符的嵌套来完成此题：学习成绩=90分的同学用A表示，60-89分之间的用B表示，60分以下的用C表示。
````
public class Sort05 {
    public static void main(String[] args){
        int[] grades = {95,45,78};
        for(int grade:grades){
            String c = grade>80?"A":(grade<60?"C":"B");
            System.out.println(c);
        }
    }
}
````

【程序7】   题目：输入一行字符，分别统计出其中英文字母、空格、数字和其它字符的个数。
````
public class sort07 {
    public static void main(String[] args){
        String str = "45wrq4wrQtasdfw5er5qw4er 8 f a 1 s @#$fasdfasd" +
                "@#4d#$%@ #$@34  f@ %#$%@# $%45q!@#$!@  #$34r34r4sdf5aads  AFzxcADFASDZXCsdfawef";
        char[] strArray = str.toCharArray();
        int number = 0;
        int zifu = 0;
        int zimu = 0;
        int kongge = 0;
        for(int i=0;i<strArray.length;i++){
            char item = strArray[i];
            if(item>='0'&&item<='9'){//单引号是字符，双引号是字符串
                number ++;
            }else if(('a'<=item&&item<='z')||('A'<=item&&item<='Z')){
                zimu++;
            }else if(item == ' '){
                kongge++;
            }else{
                zifu++;
            }
        }
        System.out.println(number);
        System.out.println(zifu);
        System.out.println(zimu);
        System.out.println(kongge);
    }
}

````