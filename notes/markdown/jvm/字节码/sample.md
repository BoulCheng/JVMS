
### i++
```
appledeiMac:transactional apple$ 
appledeiMac:transactional apple$ javac TestAASuf.java
appledeiMac:transactional apple$ javap -c TestAASuf
警告: 二进制文件TestAASuf包含com.zlb.spring.practice.transactional.TestAASuf
Compiled from "TestAASuf.java"
public class com.zlb.spring.practice.transactional.TestAASuf {
  public com.zlb.spring.practice.transactional.TestAASuf();
    Code:
       0: aload_0
       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
       4: return

  public static void main(java.lang.String[]);
    Code:
       0: iconst_0
       1: istore_1
       2: iload_1
       3: iinc          1, 1
       6: istore_1
       7: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
      10: iload_1
      11: invokevirtual #3                  // Method java/io/PrintStream.println:(I)V
      14: return
}
appledeiMac:transactional apple$ cat TestAASuf.java
package com.zlb.spring.practice.transactional;

/**
 * Created by yuanming
 * Created 2021-05-04
 * Modify:
 */
public class TestAASuf {

    public static void main(String[] args){
        int i = 0;
        i = i++;
        System.out.println(i);
    }
}
appledeiMac:transactional apple$ 

```
- 首先，由于是静态函数，初始时局部变量数组的“0”处存储的是args参数。“iconst_0”将整数0压入操作数栈，此时操作数栈为[0]。然后，“istore_1”将操作数栈的栈顶元素放入局部变量数组的“1”处，局部变量数组的内容是[args,0]，此时操作数栈是[](也就是空栈)。接着，“iload_1”将局部变量数组里的“1”处的值压入操作数栈，操作数栈变成[0]。接下来注意了，“iinc”操作码是直接把局部变量数组的“1”处的值增加1，也就是说局部变量数组变成了[args,1]。接着，“istore_1”将操作数栈的栈顶元素，也就是0，放入局部变量数组的“1”处，局部变量数组又变成了[args,0]。“getstatic”载入“System.out”域，然后“iload_1”将局部变量表里“1”处的值(也就是0)压入操作数栈，作为参数传入“println”函数输出到命令行。也就是最终输出结果为0。
  
### ++i
```
appledeiMac:transactional apple$ 
appledeiMac:transactional apple$ javac TestAA.java
appledeiMac:transactional apple$ 
appledeiMac:transactional apple$ javap -c TestAA
警告: 二进制文件TestAA包含com.zlb.spring.practice.transactional.TestAA
Compiled from "TestAA.java"
public class com.zlb.spring.practice.transactional.TestAA {
  public com.zlb.spring.practice.transactional.TestAA();
    Code:
       0: aload_0
       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
       4: return

  public static void main(java.lang.String[]);
    Code:
       0: iconst_0
       1: istore_1
       2: iinc          1, 1
       5: iload_1
       6: istore_1
       7: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
      10: iload_1
      11: invokevirtual #3                  // Method java/io/PrintStream.println:(I)V
      14: return
}
appledeiMac:transactional apple$ cat TestAA.java 
package com.zlb.spring.practice.transactional;

/**
 * Created by yuanming
 * Created 2021-05-04
 * Modify:
 */
public class TestAA {

    public static void main(String[] args){
        int i = 0;
        i = ++i;
        System.out.println(i);
    }
}
appledeiMac:transactional apple$ 

```
- 与之前代码不同之处在于，源代码的i从先赋值再再增变成了先自增再赋值。对应的字节码中，先是在局部变量数组里面进行自增运算，然后将得到结果(1)压入操作数栈，最后再保存到局部变量表里面的就是1了，输出结果当然也是1

- 其实，问题的关键在于自增运算发生的地方：局部变量数组！正常情况下，数值的操作应该都在操作数栈里完成。可能是为了减少运行时间吧