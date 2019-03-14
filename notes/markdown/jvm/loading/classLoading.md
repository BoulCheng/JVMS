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
        - 虽然在Java源码中引用了ConstClass类中的常量HELLOWORLD，但其实在编译阶段通过常量传播优化，已经将此常量的值"hello world"存储到了NotInitialization类的常量池中，以后NotInitialization对常量ConstClass.HELLOWORLD的引用实际都被转化为NotInitialization类对自身常量池的引用了
        - 也就是说，实际上NotInitialization的Class文件之中并没有ConstClass类的符号引用入口，这两个类在编译成Class之后就不存在任何联系了。
        
- 接口初始化
    - 接口中不能使用"static{}"语句块，但编译器仍然会为接口生成"＜clinit＞()"类构造器，用于初始化接口中所定义的成员变量
    - 一个接口在初始化时，并不要求其父接口全部都完成了初始化，只有在真正使用到父接口的时候（如引用接口中定义的常量）才会初始化
    

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
            - 这个类加载器由sun.misc.Launcher $App-ClassLoader实现。(由于这个类加载器是ClassLoader中的getSystemClassLoader()方法的返回值，所以一般也称它为系统类加载器) 它负责加载用户类路径（ClassPath）上所指定的类库
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
    
    
    
    
            