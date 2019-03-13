# oom

- 目的有两个：第一，通过代码验证Java虚拟机规范中描述的各个运行时区域存储的内容；

- 第二，希望读者在工作中遇到实际的内存溢出异常时，能根据异常的信息快速判断是哪个区域的内存溢出，知道什么样的代码可能会导致这些区域内存溢出，以及出现这些异常后该如何处理。

- 实例基于Sun公司的HotSpot虚拟机运行


## Java堆溢出
- Java堆用于存储对象实例，只要不断地创建对象，并且保证GC Roots到对象之间有可达路径来避免垃圾回收机制清除这些对象，那么在对象数量到达最大堆的容量限制后就会产生内存溢出异常。

- 将堆的最小值-Xms参数与最大值-Xmx参数设置为一样即可避免堆自动扩展

- 参数-XX:+HeapDumpOnOutOfMemoryError可以让虚拟机在出现内存溢出异常时Dump出当前的内存堆转储快照以便事后进行分析

- 要解决这个区域的异常，一般的手段是先通过内存映像分析工具（如Eclipse Memory Analyzer）对Dump出来的堆转储快照进行分析，重点是确认内存中的对象是否是必要的，也就是要先分清楚到底是出现了内存泄漏（Memory Leak）还是内存溢出（Memory Overflow）
    - 如果是内存泄露(没有及时清理内存垃圾,导致资源耗尽)，可进一步通过工具查看泄露对象到GC Roots的引用链。于是就能找到泄露对象是通过怎样的路径与GC Roots相关联并导致垃圾收集器无法自动回收它们的。掌握了泄露对象的类型信息及GC Roots引用链的信息，就可以比较准确地定位出泄露代码的位置
    - 如果不存在泄露，内存溢出(内存越界，要求分配的内存超出了系统能给的，提供的内存不够),换句话说，就是内存中的对象确实都还必须存活着，那就应当检查虚拟机的堆参数（-Xmx与-Xms），与机器物理内存对比看是否还可以调大，从代码上检查是否存在某些对象生命周期过长、持有状态时间过长的情况，尝试减少程序运行期的内存消耗。
    
 
- 堆内存泄漏 
    - 当被分配的对象可达但已无用（未对作废数据内存单元的引用置null）即会引起。
    
- -Xms20m -Xmx20m

## 虚拟机栈和本地方法栈溢出
- 由于在HotSpot虚拟机中并不区分虚拟机栈和本地方法栈，因此，对于HotSpot来说，虽然-Xoss参数（设置本地方法栈大小）存在，但实际上是无效的，栈容量只由-Xss参数设定

- 虚拟机栈和本地方法栈，在Java虚拟机规范中描述了两种异常：
    - 如果线程请求的栈深度大于虚拟机所允许的最大深度，将抛出StackOverflowError异常。
    - 如果虚拟机在扩展栈时无法申请到足够的内存空间，则抛出OutOfMemoryError异常。
    
- -Xss160k
    - 每个线程的堆栈大小	
    - 操作系统分配给每个进程的内存是有限制的，譬如32位的Windows限制为2GB。虚拟机提供了参数来控制Java堆和方法区的这两部分内存的最大值。剩余的内存为2GB（操作系统限制）减去Xmx（最大堆容量），再减去MaxPermSize（最大方法区容量），程序计数器消耗内存很小，可以忽略掉。如果虚拟机进程本身耗费的内存不计算在内，剩下的内存就由虚拟机栈和本地方法栈“瓜分”了。每个线程分配到的栈容量越大，可以建立的线程数量自然就越少，建立线程时就越容易把剩下的内存耗尽
    - JDK5.0以后每个线程堆栈大小为1M,以前每个线程堆栈大小为256K.更具应用的线程所需内存大小进行 调整.
    - 在相同物理内存下,减小这个值能生成更多的线程.但是操作系统对一个进程内的线程数还是有限制的,不能无限生成,经验值在3000~5000左右
    - 一般小的应用， 如果栈不是很深， 应该是128k够用的 大的应用建议使用256k。这个选项对性能影响比较大，需要严格的测试。（校长）和threadstacksize选项解释很类似,官方文档似乎没有解释,在论坛中有这样一句话:"-Xss is translated in a VM flag named ThreadStackSize"一般设置这个值就可以了

## 方法区和运行时常量池溢出

- String.intern()是一个Native方法，它的作用是
    - 如果字符串常量池中已经包含一个等于此String对象的字符串，则返回代表池中这个字符串的String对象；否则，将此String对象包含的字符串添加到常量池中，并且返回此String对象的引用
    - 在JDK 1.6及之前的版本中，由于常量池分配在永久代内，我们可以通过-XX:PermSize和-XX:MaxPermSize限制方法区大小，从而间接限制其中常量池的容量
- JDK8 修改(http://openjdk.java.net/jeps/122)
    - Move part of the contents of the permanent generation in Hotspot to the Java heap and the remainder to native memory.
    - Hotspot's representation of Java classes (referred to here as class meta-data) is currently stored in a portion of the Java heap referred to as the permanent generation. In addition, interned Strings and class static variables are stored in the permanent generation.
    - **The proposed implementation will allocate class meta-data in native memory and move interned Strings and class statics to the Java heap.**
    - Hotspot will explicitly allocate and free the native memory for the class meta-data. Allocation of new class meta-data would be limited by the amount of available native memory rather than fixed by the value of -XX:MaxPermSize, whether the default or specified on the command line.
    - -XX:PermSize=10M -XX:MaxPermSize=10M 已被移除Java HotSpot(TM) 64-Bit Server VM warning: ignoring option PermSize=2m; support was removed in 8.0. Java HotSpot(TM) 64-Bit Server VM warning: ignoring option MaxPermSize=2m; support was removed in 8.0.
    - Java class metadata (the virtual machines internal presentation of Java class) is allocated in native memory (referred to here as metaspace). If metaspace for class metadata is exhausted, a java.lang.OutOfMemoryError exception with a detail MetaSpace is thrown. The amount of metaspace that can be used for class metadata is limited by the parameter MaxMetaSpaceSize, which is specified on the command line. When the amount of native memory needed for a class metadata exceeds MaxMetaSpaceSize, a java.lang.OutOfMemoryError exception with a detail MetaSpace is thrown.
    - If MaxMetaSpaceSize, has been set on the command-line, increase its value. MetaSpace is allocated from the same address spaces as the Java heap. Reducing the size of the Java heap will make more space available for MetaSpace. This is only a correct trade-off if there is an excess of free space in the Java heap. See the following action for Out of swap space detailed message.
    - -XX:MetaspaceSize=2m -XX:MaxMetaspaceSize=20m  (meta-data设置)
    