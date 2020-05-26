# 虚拟机类加载机制

## 概述
- 代码编译的结果从本地机器码转变为字节码，是存储格式发展的一小步，却是编程语言发展的一大步

- 虚拟机把描述类的数据从Class文件加载到内存，并对数据进行校验、转换解析和初始化，最终形成可以被虚拟机直接使用的Java类型，这就是虚拟机的类加载机制

- Java语言运行期类加载的特性
    - 在Java语言里面，类型的加载、连接和初始化过程都是在程序运行期间完成的，这种策略虽然会令类加载时稍微增加一些性能开销，但是会为Java应用程序提供高度的灵活性
    - Java里天生可以动态扩展的语言特性就是依赖运行期动态加载和动态连接这个特点实现的。
    - 例如，如果编写一个面向接口的应用程序，可以等到运行时再指定其实际的实现类；
    - 用户可以通过Java预定义的和自定义类加载器，让一个本地的应用程序可以在运行时从网络或其他地方加载一个二进制流作为程序代码的一部分
    

- 在实际情况中，每个Class文件都有可能代表着Java语言中的一个类或接口，后文中直接对“类”的描述都包括了类和接口的可能性
- 与前面介绍Class文件格式时的约定一致，笔者本章所提到的“Class文件”并非特指某个存在于具体磁盘中的文件，这里所说的“Class文件”应当是一串二进制的字节流，无论以何种形式存在都可以。


## 类加载的时机

- 类从被加载到虚拟机内存中开始，到卸载出内存为止，它的整个生命周期包括
    - 加载（Loading）、验证（Verification）、准备（Preparation）、解析（Resolution）、初始化（Initialization）、使用（Using）和卸载（Unloading）7个阶段
    - 其中验证、准备、解析3个部分统称为连接（Linking）
- 加载、验证、准备、初始化和卸载这5个阶段的顺序是确定的，类的加载过程必须按照这种顺序按部就班地开始. (注意，这里笔者写的是按部就班地“开始”，而不是按部就班地“进行”或“完成”，强调这点是因为这些阶段通常都是互相交叉地混合式进行的，通常会在一个阶段执行的过程中调用、激活另外一个阶段。)

- 而解析阶段则不一定：它在某些情况下可以在初始化阶段之后再开始，这是为了支持Java语言的运行时绑定（也称为动态绑定或晚期绑定)

- 第一个阶段加载Java虚拟机规范中并没有进行强制约束，这点可以交给虚拟机的具体实现来自由把握

- 初始化阶段，虚拟机规范则是严格规定了有且只有(**有且只有**)5种情况必须立即对类进行“初始化”（而加载、验证、准备自然需要在此之前开始）：
    - 遇到new、getstatic、putstatic或invokestatic这4条字节码指令时，如果类没有进行过初始化，则需要先触发其初始化。生成这4条指令的最常见的Java代码场景是：
        - 使用new关键字实例化对象的时候
        - 读取或设置一个类的静态字段（被final修饰、已在编译期把结果放入常量池的静态字段除外）的时候
        - 读取或设置一个类的静态字段（被final修饰、已在编译期把结果放入常量池的静态字段除外）的时候
        
    - 使用java.lang.reflect包的方法对类进行反射调用的时候，如果类没有进行过初始化，则需要先触发其初始化。
    - 当初始化一个类的时候，如果发现其父类还没有进行过初始化，则需要先触发其父类的初始化
    - 当虚拟机启动时，用户需要指定一个要执行的主类（包含main()方法的那个类），虚拟机会先初始化这个主类
    - 当使用JDK 1.7的动态语言支持时，如果一个java.lang.invoke.MethodHandle实例最后的解析结果REF_getStatic、REF_putStatic、REF_invokeStatic的方法句柄，并且这个方法句柄所对应的类没有进行过初始化，则需要先触发其初始化。
- 这5种场景中的行为称为对一个类进行主动引用。除此之外，所有引用类的方式都不会触发初始化，称为被动引用
    - 对于静态字段，只有直接定义这个字段的类才会被初始化. 子类来引用父类中定义的静态字段，只会触发父类的初始化而不会触发子类的初始化.至于是否要触发子类的加载和验证，在虚拟机规范中并未明确规定，这点取决于虚拟机的具体实现。对于Sun HotSpot虚拟机来说，可通过-XX:+TraceClassLoading参数观察到此操作会导致子类的加载但不初始化
    - 通过数组定义来引用类，不会触发此类的初始化
        - 但是这段代码里面触发了另外一个名为"[Lorg.fenixsoft.classloading.SuperClass"的类的初始化阶段
        - 它是一个由虚拟机自动生成的、直接继承于java.lang.Object的子类，创建动作由字节码指令newarray触发。
        - 这个类代表了一个元素类型为org.fenixsoft.classloading.SuperClass的一维数组，数组中应有的属性和方法（用户可直接使用的只有被修饰为public的length属性和clone()方法）都实现在这个类里
    - 常量在编译阶段会存入调用类的常量池中，本质上并没有直接引用到定义常量的类，因此不会触发定义常量的类的初始化。
        - 虽然在Java源码中引用了ConstClass类中的常量HELLOWORLD，但其实在编译阶段通过常量传播优化，已经将此常量的值"hello world"存储到了NotInitialization3类的常量池中，以后NotInitialization对常量ConstClass.HELLOWORLD的引用实际都被转化为NotInitialization类对自身常量池的引用了
        - 也就是说，实际上NotInitialization的Class文件之中并没有ConstClass类的符号引用入口，这两个类在编译成Class之后就不存在任何联系了。
        
- 接口初始化
    - 接口中不能使用"static{}"语句块，但编译器仍然会为接口生成"＜clinit＞()"类构造器(注意区别于实例构造器<init>())，用于初始化接口中所定义的成员变量
    - 一个接口在初始化时，并不要求其父接口全部都完成了初始化，只有在真正使用到父接口的时候（如引用接口中定义的常量）才会初始化
  
## 类加载的全过程  加载、验证、准备、解析和初始化这5个阶段
- 加载 获取定义类的字节流并放入虚拟机生成Class对象
    - 在加载阶段，虚拟机需要完成以下3件事情：
        - 通过一个类的全限定名来获取定义此类的二进制字节流. (没有指明二进制字节流要从一个Class文件中获取，准确地说是根本没有指明要从哪里获取、怎样获取. 如：运行时计算生成，这种场景使用得最多的就是动态代理技术，在java.lang.reflect.Proxy中，就是用了ProxyGenerator.generateProxyClass来为特定接口生成形式为"*$Proxy"的代理类的二进制字节流)
        - 将这个字节流所代表的静态存储结构转化为方法区的运行时数据结构。
        - 在内存中生成一个代表这个类的java.lang.Class对象，作为方法区这个类的各种数据的访问入口。(并没有明确规定是在Java堆中, 对于HotSpot虚拟机而言，Class对象比较特殊，它虽然是对象，但是存放在方法区里面)
    - 数组类与非数组类加载
        - 非数组类加载: 加载阶段既可以使用系统提供的引导类加载器来完成，也可以由用户自定义的类加载器去完成，开发人员可以通过定义自己的类加载器去控制字节流的获取方式
        - 数组类
            - 数组类本身不通过类加载器创建，它是由Java虚拟机直接创建的
            - 数组类的元素类型（Element Type，指的是数组去掉所有维度的类型）最终是要靠类加载器去加载的(注意是加载 区别初始化)
            - 一个数组类（下面简称为C）创建过程就遵循以下规则：
                - 如果数组的组件类型（Component Type，指的是数组去掉一个维度的类型）是引用类型，那就递归采用本节中定义的加载过程去加载这个组件类型，数组C将在加载该组件类型的类加载器的类名称空间上被标识（这点很重要，在7.4节会介绍到，一个类必须与类加载器一起确定唯一性）。
                - 如果数组的组件类型不是引用类型（例如int[]数组），Java虚拟机将会把数组C标记为与引导类加载器关联
                - 数组类的可见性与它的组件类型的可见性一致，如果组件类型不是引用类型，那数组类的可见性将默认为public。
- 验证
    - 验证是连接阶段的第一步，这一阶段的目的是为了确保Class文件的字节流中包含的信息符合当前虚拟机的要求，并且不会危害虚拟机自身的安全。       

- 准备
    - 正式为类变量在方法区分配内存并设置类变量初始值的阶段
    - 类变量 （被static修饰的变量），而不包括实例变量，实例变量将会在对象实例化时随着对象一起分配在Java堆中
    - 初始值“通常情况”下是数据类型的零值
    ```
    //变量value在准备阶段过后的初始值为0而不是123，因为这时候尚未开始执行任何Java方法，而把value赋值为123的putstatic指令是程序被编译后，存放于类构造器＜clinit＞()方法之中，所以把value赋值为123的动作将在初始化阶段才会执行
    public static int value=123；
    ```
    ```
    //编译时Javac将会为value生成ConstantValue属性，在准备阶段虚拟机就会根据ConstantValue的设置将value赋值为123。
    //在“通常情况”下初始值是零值，那相对的会有一些“特殊情况”：如果类字段的字段属性表中存在ConstantValue属性，那在准备阶段变量value就会被初始化为ConstantValue属性所指定的值
    //被final修饰
    public static final int value=123；

    ```
    
- 解析
    - 虚拟机将常量池内的符号引用替换为直接引用的过程
    - 符号引用（Symbolic References）:符号引用以一组符号来描述所引用的目标，符号可以是任何形式的字面量，只要使用时能无歧义地定位到目标即可。符号引用与虚拟机实现的内存布局无关，引用的目标并不一定已经加载到内存中. 各种虚拟机实现的内存布局可以各不相同，但是它们能接受的符号引用必须都是一致的，因为符号引用的字面量形式明确定义在Java虚拟机规范的Class文件格式中。
    - 直接引用（Direct References）: 直接引用可以是直接指向目标的指针、相对偏移量或是一个能间接定位到目标的句柄。直接引用是和虚拟机实现的内存布局相关的，同一个符号引用在不同虚拟机实例上翻译出来的直接引用一般不会相同。如果有了直接引用，那引用的目标必定已经在内存中存在
    ```
    关于方法调用
    
    1、Class文件的编译过程中不包含传统编译中的连接步骤，所有方法调用中的目标方法在Class文件里面都是一个常量池中的符号引用，而不是方法在实际运行时内存布局中的入口地址。
    
    2、在类加载的解析阶段，会将其中的一部分符号引用转化为直接引用，这类方法（编译期可知，运行期不可变）的调用称为解析（Resolution）。
    
    主要包括静态方法和私有方法两大类，前者与类型直接关联，后者在外部不可被访问，这两种方法各自的特点决定了它们都不可能通过继承或别的方式重写其他版本，因此它们都适合在类加载阶段进行解析。
    
    3、只要能被invokestatic和invokespecial指令调用的方法，都可以在解析阶段中确定唯一的调用版本，符合这个条件的有静态方法、私有方法、实例构造器、父类方法4类，它们在类加载的时候就会把符号引用解析为该方法的直接引用。
    ```
- 初始化
    - 初始化阶段，才真正开始执行类中定义的Java程序代码（或者说是字节码）. 类初始化阶段是类加载过程的最后一步，前面的类加载过程中，除了在加载阶段用户应用程序可以通过自定义类加载器参与之外，其余动作完全由虚拟机主导和控制
    - 在准备阶段变量已经赋过一次系统要求的初始值，而在初始化阶段，则根据程序员通过程序制定的主观计划去初始化类变量和其他资源，或者可以从另外一个角度来表达：初始化阶段是执行类构造器＜clinit＞()方法的过程
    - ＜clinit＞() (Java)
        - ＜clinit＞()方法是由编译器自动收集类中的所有类变量的赋值动作和静态语句块（static{}块）中的语句合并产生的.
        - 编译器收集的顺序是由语句在源文件中出现的顺序所决定的
        - 静态语句块中只能访问到定义在静态语句块之前的变量，定义在它之后的变量，在前面的静态语句块可以赋值，但是不能访问
        
        - ＜clinit＞()方法与类的构造函数（或者说实例构造器＜init＞()方法）不同，它不需要显式地调用父类构造器，虚拟机会保证在子类的＜clinit＞()方法执行之前，父类的＜clinit＞()方法已经执行完毕。因此在虚拟机中第一个被执行的＜clinit＞()方法的类肯定是java.lang.Object。
        - 由于父类的＜clinit＞()方法先执行，也就意味着父类中定义的静态语句块要优先于子类的变量赋值操作
        
        - ＜clinit＞()方法对于类或接口来说并不是必需的，如果一个类中没有静态语句块，也没有对类变量的赋值操作，那么编译器可以不为这个类生成＜clinit＞()方法
        - 接口中不能使用静态语句块，但仍然有变量初始化的赋值操作，因此接口与类一样都会生成＜clinit＞()方法。但接口与类不同的是，执行接口的＜clinit＞()方法不需要先执行父接口的＜clinit＞()方法。只有当父接口中定义的变量使用时，父接口才会初始化。另外，接口的实现类在初始化时也一样不会执行接口的＜clinit＞()方法。
        
        - 虚拟机会保证一个类的＜clinit＞()方法在多线程环境中被正确地加锁、同步，如果多个线程同时去初始化一个类，那么只会有一个线程去执行这个类的＜clinit＞()方法，其他线程都需要阻塞等待，直到活动线程执行＜clinit＞()方法完毕。如果在一个类的＜clinit＞()方法中有耗时很长的操作，就可能造成多个进程阻塞，在实际应用中这种阻塞往往是很隐蔽的

    - 实例化子类时，父类与子类中的静态代码块、实例代码块、静态变量、实例变量、构造函数的执行顺序是怎样的？
    
        - 代码执行的优先级为：
            - first：类初始化 (同一个加载器下，一个类型只会初始化一次)
            - second：实例化过程
    
        - 注意：子类实例化并不会实例化父类对象，仅仅是为父类中的属性在堆中开辟了一段内存空间。
        - 详细顺序为：
    
            1. 父类静态代码块与父类静态变量（取决于源代码书写顺序）
            
            2. 子类静态代码块与子类静态变量（取决于源代码书写顺序）
            
            3. 父类实例变量与父类代码块（取决于源代码书写顺序）
            
            4. 父类实例构造函数
            
            5. 子类实例变量与子类代码块（取决于源代码书写顺序）
            
            6. 子类实例构造函数
   

- 为什么构造代码块先于构造函数执行
```
那么，为什么构造代码块先于构造函数执行呢？

因为编译器会把构造代码块插入到不含this();的构造函数中的super();后面。

super()是调用父类构造函数，先有父亲，再有儿子，所以在super();之后。

this()是调用自身构造函数，为了保证构造代码块在类初始化时只执行一次，所以只会插入到不含this();的构造函数中。

构造代码块的作用：构造函数的公共模块。

```
- 关于调用父类构造器 见super关键字    
- If a subclass constructor invokes a constructor of its superclass, either explicitly or implicitly, you might think that there will be a whole chain of constructors called, all the way back to the constructor of Object. In fact, this is the case. It is called constructor chaining, and you need to be aware of it when there is a long line of class descent.
```

    - If a constructor does not explicitly invoke a superclass constructor, the Java compiler automatically inserts a call to the no-argument constructor of the superclass.   
    - If the super class does not have a no-argument constructor, you will get a compile-time error. Object does have such a constructor, so if Object is the only superclass, there is no problem.
    - If a subclass constructor invokes a constructor of its superclass, either explicitly or implicitly, you might think that there will be a whole chain of constructors called, all the way back to the constructor of Object. In fact, this is the case. It is called constructor chaining, and you need to be aware of it when there is a long line of class descent.
   
    
```
- 创建子类对象时，父类对象会也被一起创建-没有创建父类对象，但是调用了父类的构造函数。构造函数就是一个成员方法而已，并没有太多特别之处。
                      
```
调用父类构造方法是真的，但是根本没有创建父类对象，只不过是调用父类构造方法来初始化属性。如果说调用父类构造方法就等于创建父类对象，那就真的无稽之谈。
new指令开辟空间，用于存放对象的各个属/性引用等，反编译字节码你会发现只有一个new指令，所以开辟的是一块空间，一块空间就放一个对象。
然后，子类调用父类的属性，方法啥的，那并不是一个实例化的对象。在字节码中子类会有个u2类型的父类索引，属于CONSTANT_Class_info类型，通过CONSTANT_Class_info的描述可以找到CONSTANT_Utf8_info,然后可以找到指定的父类啊啥的。
你的方法啊，属性名称都是在这个上面解析出来的，然后实际变量内容存储在new出来的空间那里。。。super这个关键字只不过是访问了这个空间特定部分的数据（也就是专门存储父类数据的内存部分）。。。。。。默认的hashcode和equals（直接使用的==比较）都是一样的，
所以，这根本就在一个空间里，也不存在单独的出来的父类对象。

如果说子类可以强行转换成父类进行使用，那是因为java虚拟机有个静态类型（外观类型）和实际类型的概念。如Object t=new Point(2,3);那么Object属于静态类型（外观类型），Point属于实际类型。
静态类型和实际类型在程序中都可以发生变法，区别是静态类型的变化仅仅发生在使用时发生，而变量本身的静态类型不会改变，并且最终的静态类型是在编译期间可知的；而实际变量类型的变化结果只有在运行期间才能被确定，编译器在编译的时候并不知道变量的实际类型是什么。
先在这个空间里创造出父类的那些内容，然后再裹上子类的内容


public class A extends B {

    public A() {
        System.out.println(this.hashCode());
        System.out.println(super.equals(this));
    }

    public static void main(String[] args) {
        new A();
    }

}

public class B {

    public B() {
        System.out.println(this.hashCode());
    }
}

401625763
401625763
true

Process finished with exit code 0


```
- 类与对象的本质区别：类是一堆执行指令的集合，而对象是这些指令执行的结果。类存储在JVM的方法区(元空间（Metaspace) MetaspaceSize MaxMetaspaceSize)中，而对象存储在堆中。类似于汇编中的代码段和数据段

- Java对象的内存布局
```
Java对象的内存布局是由对象所属的类确定。
也可以这么说，当一个类被加载到虚拟机中时，由这个类创建的对象的布局就已经确定下来的啦。
Hotspot中java对象的内存布局：
每个java对象在内存中都由对象头和对象体组成。
对象头是存放对象的元信息，包括该对象所属类对象Class的引用以及hashcode和monitor的一些信息。
对象体主要存放的是java对象自身的实例域以及从父类继承过来的实例域，并且内部布局满足由下规则：
```
- 创建对象的两个字节码指令:  new 和 调用构造函数invoke_special <init>
```
当我们new一个对象时，其实jvm已经把这个对象的整个空间已经分配好，并且整个对象的实例域布局已经确定下来啦。
实例化方法<init>就是将对象实例域的值设置到相应空间中
<init>方法以调用父类的<init>方法开始，以自身构造方法作为结束。实例域的声明与实例初始化语句块的位置关系会影响编译器生成的<init>方法的字节码顺序

没有创建父类对象，但是调用了父类的构造函数。构造函数就是一个成员方法而已，并没有太多特别之处。

appledeiMac:Downloads apple$
appledeiMac:Downloads apple$
appledeiMac:Downloads apple$ cat TestSuperClassNotBeCreatedWhenSubClassBeCreated.java
public class TestSuperClassNotBeCreatedWhenSubClassBeCreated {


  public static void main(String[] args) {
  	TestSuperClassNotBeCreatedWhenSubClassBeCreated t = new TestSuperClassNotBeCreatedWhenSubClassBeCreated();
  }

}appledeiMac:Downloads apple$
appledeiMac:Downloads apple$
appledeiMac:Downloads apple$
appledeiMac:Downloads apple$ javap -c TestSuperClassNotBeCreatedWhenSubClassBeCreated.class
Compiled from "TestSuperClassNotBeCreatedWhenSubClassBeCreated.java"
public class TestSuperClassNotBeCreatedWhenSubClassBeCreated {
  public TestSuperClassNotBeCreatedWhenSubClassBeCreated();
    Code:
       0: aload_0
       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
       4: return

  public static void main(java.lang.String[]);
    Code:
       0: new           #2                  // class TestSuperClassNotBeCreatedWhenSubClassBeCreated
       3: dup
       4: invokespecial #3                  // Method "<init>":()V
       7: astore_1
       8: return
}
appledeiMac:Downloads apple$
```

- 关于super this,  这两个变量对应的字节码指令都是 aload_0 它们并非指向不同对象 
```
appledeiMac:calculation apple$
appledeiMac:calculation apple$ cat C.java
package so.dian.legolas.api.calculation;

public class C {

    public C() {
    }
}
appledeiMac:calculation apple$ javap -c C.class
Compiled from "C.java"
public class so.dian.legolas.api.calculation.C {
  public so.dian.legolas.api.calculation.C();
    Code:
       0: aload_0
       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
       4: return
}
appledeiMac:calculation apple$

appledeiMac:calculation apple$
appledeiMac:calculation apple$ cat A.java
package so.dian.legolas.api.calculation;

public class A extends B {

    public A() {
        System.out.println(this.hashCode());
        System.out.println(super.equals(this));
    }

    public static void main(String[] args) {
        new A();
    }

}

appledeiMac:calculation apple$
appledeiMac:calculation apple$
appledeiMac:calculation apple$ cat B.java
package so.dian.legolas.api.calculation;

public class B {

    protected int i;
    public B() {
        System.out.println(this.hashCode());
    }
}
appledeiMac:calculation apple$
appledeiMac:calculation apple$
appledeiMac:calculation apple$ javap -c A.class
Compiled from "A.java"
public class so.dian.legolas.api.calculation.A extends so.dian.legolas.api.calculation.B {
  public so.dian.legolas.api.calculation.A();
    Code:
       0: aload_0
       1: invokespecial #1                  // Method so/dian/legolas/api/calculation/B."<init>":()V
       4: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
       7: aload_0
       8: invokevirtual #3                  // Method java/lang/Object.hashCode:()I
      11: invokevirtual #4                  // Method java/io/PrintStream.println:(I)V
      14: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
      17: aload_0
      18: aload_0
      19: invokespecial #5                  // Method java/lang/Object.equals:(Ljava/lang/Object;)Z
      22: invokevirtual #6                  // Method java/io/PrintStream.println:(Z)V
      25: return

  public static void main(java.lang.String[]);
    Code:
       0: new           #7                  // class so/dian/legolas/api/calculation/A
       3: dup
       4: invokespecial #8                  // Method "<init>":()V
       7: pop
       8: return
}
appledeiMac:calculation apple$
appledeiMac:calculation apple$ javap -c B.class
Compiled from "B.java"
public class so.dian.legolas.api.calculation.B {
  protected int i;

  public so.dian.legolas.api.calculation.B();
    Code:
       0: aload_0
       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
       4: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
       7: aload_0
       8: invokevirtual #3                  // Method java/lang/Object.hashCode:()I
      11: invokevirtual #4                  // Method java/io/PrintStream.println:(I)V
      14: return
}
appledeiMac:calculation apple$

```
## 类加载器

- 类加载器:通过一个类的全限定名来获取描述此类的二进制字节流的动作的代码模块 (类加载过程的加载阶段)

- 两个类相等的判断
    - **对于任意一个类，都需要由加载它的类加载器和这个类本身一同确立其在Java虚拟机中的唯一性，每一个类加载器，都拥有一个独立的类名称空间**
    - 这句话可以表达得更通俗一些：比较两个类是否“相等”，只有在这两个类是由同一个类加载器加载的前提下才有意义，否则，即使这两个类来源于同一个Class文件，被同一个虚拟机加载，只要加载它们的类加载器不同，那这两个类就必定不相等。
    - 这里所指的“相等”，包括代表类的Class对象的equals()方法、isAssignableFrom()方法、isInstance()方法的返回结果，也包括使用instanceof关键字做对象所属关系判定等情况
    
    
- 类加载器的种类
    - Java虚拟机的角度
        - 一种是启动类加载器（Bootstrap ClassLoader） . 这个类加载器使用C++语言实现(HotSpot)，是虚拟机自身的一部分
        - 另一种就是所有其他的类加载器(全都继承自抽象类java.lang.ClassLoader)，这些类加载器都由Java语言实现，独立于虚拟机外部
    - Java开发人员的角度(应用程序都是由这3种类加载器互相配合进行加载的，如果有必要，还可以加入自己定义的类加载器)
        - 启动类加载器（Bootstrap ClassLoader）
            - 这个类将器负责将存放在＜JAVA_HOME＞\lib目录中的，或者被-Xbootclasspath参数所指定的路径中的，并且是虚拟机识别的（仅按照文件名识别，如rt.jar，名字不符合的类库即使放在lib目录中也不会被加载）类库加载到虚拟机内存中
            - 启动类加载器无法被Java程序直接引用，用户在编写自定义类加载器时，如果需要把加载请求委派给引导类加载器，那直接使用null代替即可
            
        - 扩展类加载器（Extension ClassLoader）
            - 这个加载器由sun.misc.Launcher $ExtClassLoader实现，它负责加载＜JAVA_HOME＞\lib\ext目录中的，或者被java.ext.dirs系统变量所指定的路径中的所有类库
            - 开发者可以直接使用扩展类加载器
        - 应用程序类加载器（Application ClassLoader）(系统类加载器)
            - 这个类加载器由sun.misc.Launcher$App-ClassLoader实现。(由于这个类加载器是ClassLoader中的getSystemClassLoader()方法的返回值，所以一般也称它为系统类加载器) 它负责加载用户类路径（ClassPath）上所指定的类库
            - 开发者可以直接使用这个类加载器
            - 如果应用程序中没有自定义过自己的类加载器，一般情况下这个就是程序中默认的类加载器
      
           
- 双亲委派模型（Parents Delegation Model）
    - 类双亲委派模型: 加载器之间的这种层次关系: 启动类加载器 > 扩展类加载器 > 应用程序类加载器 > 自定义类加载器
    - (双亲委派模型要求除了顶层的启动类加载器外，其余的类加载器都应当有自己的父类加载器)
    - (这里类加载器之间的父子关系一般不会以继承（Inheritance）的关系来实现，而是都使用组合（Composition）关系来复用父加载器的代码)
    - 并不是一个强制性的约束模型，而是Java设计者推荐给开发者的一种类加载器实现方式。
    - 双亲委派模型的工作过程是:如果一个类加载器收到了类加载的请求，它首先不会自己去尝试加载这个类，而是把这个请求委派给父类加载器去完成，每一个层次的类加载器都是如此，因此所有的加载请求最终都应该传送到顶层的启动类加载器中，只有当父加载器反馈自己无法完成这个加载请求（它的搜索范围中没有找到所需的类）时，子加载器才会尝试自己去加载
    - Java类随着它的类加载器一起具备了一种带有优先级的层次关系(例如类java.lang.Object，它存放在rt.jar之中，无论哪一个类加载器要加载这个类，最终都是委派给处于模型最顶端的启动类加载器进行加载，因此Object类在程序的各种类加载器环境中都是同一个类)
    
- 实现双亲委派的代码都集中在java.lang.ClassLoader的loadClass()方法之中
    ```
        /**
         * 辑清晰易懂：先检查是否已经被加载过，若没有加载则调用父加载器的loadClass()方法，若父加载器为空则默认使用启动类加载器作为父加载器。
         * 如果父类加载失败，抛出ClassNotFoundException异常后，再调用自己的findClass()方法进行加载
         * @param name
         * @param resolve
         * @return
         * @throws ClassNotFoundException
         */
        protected Class<?> loadClass(String name, boolean resolve)
                throws ClassNotFoundException
        {
            synchronized (getClassLoadingLock(name)) {
                // First, check if the class has already been loaded
                Class<?> c = findLoadedClass(name);
                if (c == null) {
                    long t0 = System.nanoTime();
                    try {
                        if (parent != null) {
                            c = parent.loadClass(name, false);
                        } else {
                            c = findBootstrapClassOrNull(name);
                        }
                    } catch (ClassNotFoundException e) {
                        // ClassNotFoundException thrown if class not found
                        // from the non-null parent class loader
                    }
    
                    if (c == null) {
                        // If still not found, then invoke findClass in order
                        // to find the class.
                        long t1 = System.nanoTime();
                        c = findClass(name);
    
                        // this is the defining class loader; record the stats
                        sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
                        sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
                        sun.misc.PerfCounter.getFindClasses().increment();
                    }
                }
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
        }
        
    ```
    
- 自定义的类包名不能以 java 开头， {@link ClassLoader#preDefineClass(String, ProtectionDomain)} 不允许加载类的全限定名以 java 开头的类 

## 双亲委派模型的应用
- 不在ClassPath路径，加载特定路径下或网络上的class文件
- 隔离
    - tomcat中对每个Web应用都有自己专用的一个WebAppClassLoader类加载器用来隔绝不同应用之间的class文件
- 字节码加解密
    - 一些核心类库，可能会把字节码加密，这样加载类的时候就必须对字节码进行解密，可以通过findClass读取URL中的字节码，然后加密，最后把字节数组交给defineClass()加载
- 同时加载不同版本的同名包
- OSGi实现模块化热部署的关键则是它自定义的类加载器机制的实现。
    - 每一个程序模块（OSGi中称为Bundle）都有一个自己的类加载器，当需要更换一个Bundle时，就把Bundle连同类加载器一起换掉以实现代码的热替换。
    - 在OSGi环境下，类加载器不再是双亲委派模型中的树状结构，而是进一步发展为更加复杂的网状结构
       
- 线程上下文类加载器（Thread Context ClassLoader）
    - 保证多个需要通信的线程间的类加载器应该是同一个, 防止因为不同的类加载器, 导致类型转换异常(ClassCastException). 
## 破坏双亲委派模型

- JDK 1.2 之前 用户去继承java.lang.ClassLoader的唯一目的就是为了重写loadClass()方法.自己实现loadClass()方法可能会破坏
    - JDK 1.2之后已不提倡用户再去覆盖loadClass()方法，而应当把自己的类加载逻辑写到findClass()方法中，在loadClass()方法的逻辑里如果父类加载失败，则会调用自己的findClass()方法来完成加载，这样就可以保证新写出来的类加载器是符合双亲委派规则的。
    - 如果需要破坏双亲委派机制则直接覆盖loadClass方法
    
- 父类加载器请求子类加载器去完成类加载的动作，这种行为实际上就是打通了双亲委派模型的层次结构来逆向使用类加载器，实际上已经违背了双亲委派模型的一般性原则.
    - 双亲委派很好地解决了各个类加载器的基础类的统一问题（越基础的类由越上层的加载器进行加载)
    - 在双亲委派模型下rt.jar基础类只会被加载一次且只会被引导类加载器加载 比如保证一个java.lang.String只有一个Class. 否则可能导致每个类加载器都会产生一个String Class, 也可以写一个String满足自己的意图，保证了基础类的唯一性不可替代以及安全性。
    - Java设计团队只好引入了一个不太优雅的设计：线程上下文类加载器（Thread Context ClassLoader）。这个类加载器可以通过java.lang.Thread类的setContextClassLoaser()方法进行设置，如果创建线程时还未设置，它将会从父线程中继承一个，如果在应用程序的全局范围内都没有设置过的话，那这个类加载器默认就是应用程序类加载器。
    - 可以使用这个线程上下文类加载器去加载类
    - 在JDBC中就破坏了双亲委派模型，DriverManager中通过ServiceLoader(java spi)获取Driver接口的具体实现类,而Driver接口实现类的类加载就是通过 Thread.currentThread().getContextClassLoader()得到线程上下文类加载器来加载的(DriverManager.LazyIterator)
    - DriverManager 是被引导类加载器加载的 但是Driver接口的具体实现类肯定不在rt.jar里 所以必须打破双亲委派模型

    
    
            