# 运行时数据区域
- 程序计数器
    - 当前线程所执行的字节码行号指示器
    - 每个线程有一个独立的程序计数器
    - 线程私有
    - 没有任何OutOfMemoryError
    
- 虚拟机栈(生命周期与线程相同)
    - Java方法执行的内存模型(区别native方法)
    - 线程私有
    - 方法的调用到完成，对应一个栈帧在虚拟机栈中入栈到出栈
    - 栈帧中局部变量表所需内存空间在编译期间完成分配，包括基本数据类型和对象引用等
    - 线程请求的栈深度大于虚拟机允许的深度，抛出StackOverflowError
    - 虚拟机栈动态扩展无法申请足够的内存，抛出OutOfMemoryError

- 本地方法栈
    - 为虚拟机使用到的native方法服务，而虚拟机栈为虚拟机执行Java方法即字节码服务
    - HotSpot虚拟机本地方法栈和虚拟机栈合二为一
    - 会抛出StackOverflowError、OutOfMemoryError

- Java堆
    - 存放对象实例和数组
    - 线程共享
    - 虚拟机启动时创建
    - 垃圾收集器管理的主要区域
    - Java堆划分 
        - 内存回收角度 (垃圾收集器采用分代收集算法)
            - 新生代和老年代(更细: Eden空间、From Survivor空间、To Survivor空间等)
        - 内存分配角度
            - Java可能划分出多个线程私有的分配缓存区(TLAB)
    - Java堆可以处于物理上不连续的内存空间中，只要逻辑上连续即可，如磁盘空间
    - Java堆可扩张实现的调整 -Xmx -Xms 
    - OutOfMemoryError
    
- 方法区(HotSpot虚拟机中的永久代 PermGen) **JDK8已移除永久代-The proposed implementation will allocate class meta-data in native memory and move interned Strings and class statics to the Java heap.**
    - 存放被虚拟机加载的类信息、常量、静态变量等
    - 线程共享
    - JVMS把方法区描述为堆的一个逻辑部分，但取名为Non-Heap，为了和Java堆区分开
    - HopSpot虚拟机使用永久代来实现方法区是为了垃圾收集器可以像管理Java堆一个管理方法区，但更容易内存溢出(永久代有 -XX:MaxPermSize 上线)，其他虚拟机没有永久代的概念，
    - JDK1.7的HotSpot中已经把字符串常量池从永久代中移除
    - 回收目标主要是常量池的回收和对类型的卸载
    - 可以处于物理上不连续的内存空间，可固定大小实现也可扩展实现
    - OutOfMemoryError
    
- *运行时常量池(方法区的一部分)*
    - 存放(类加载后)class文件中的常量池(编译器生成的字面量和符号引用)
    - 运行时常量池相对于Class文件常量池的另一个重要特性是具备动态性，因为并非只有编译期可以产生常量，运行期也可能产生新的常量放入方法区运行时常量池(如String.intern())
    - OutOfMemoryError
    
- *直接内存DirectMemory 不是虚拟机运行时数据区*
   - 不是虚拟机运行时数据区 也不是JVMS中定义的内存区域
   - NIO类可以使用Native函数库直接分配堆外内存，然后通过一个存储在Java堆中的DirectByteBuffer对象作为这块内存的引用进行操作，避免了Java堆和Native堆来回复复制数据
   - 不受Java堆大小限制
   - -XX:MaxDirectMemorySize 未设置则默认为最大堆内存大小，即与 -Xmx 相同
   - OutOfMemoryError
