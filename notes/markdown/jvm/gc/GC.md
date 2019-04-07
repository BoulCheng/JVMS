# 垃圾收集器与内存分配策略

- 当需要排查各种内存溢出、内存泄漏问题时，当垃圾收集成为系统达到更高并发量的瓶颈时，我们就需要对这些“自动化”的技术(内存动态分配和垃圾收集技术)实施必要的监控和调节。

- 程序计数器、虚拟机栈、本地方法栈3个区域随线程而生，随线程而灭；栈中的栈帧随着方法的进入和退出而有条不紊地执行着出栈和入栈操作;因此这几个区域的内存分配和回收都具备确定性，在这几个区域内就不需要过多考虑回收的问题，因为方法结束或者线程结束时，内存自然就跟随着回收了

- Java堆和方法区,内存的分配和回收都是动态的，垃圾收集器所关注的是这部分内存，本章后续讨论中的“内存”分配与回收也仅指这一部分内存。


- 引用计数算法
    - 给对象中添加一个引用计数器，每当有一个地方引用它时，计数器值就加1；当引用失效时，计数器值就减1；任何时刻计数器为0的对象就是不可能再被使用的
    - 很难解决对象之间相互循环引用的问题。
    

- 可达性分析算法(JVM)
    - 通过一系列的称为"GC Roots"的对象作为起始点，从这些节点开始向下搜索，搜索所走过的路径称为引用链（Reference Chain），当一个对象到GC Roots没有任何引用链相连（用图论的话来说，就是从GC Roots到这个对象不可达）时，则证明此对象是不可用的。
    - 在Java语言中，可作为GC Roots的对象包括下面几种：
        - 虚拟机栈（栈帧中的本地变量表）中引用的对象。
        - 方法区中类静态属性引用的对象。
        - 方法区中常量引用的对象。
        - 本地方法栈中JNI（即一般说的Native方法）引用的对象。
        
        
- 引用
    - JDK 1.2以前，Java中的引用的定义很传统：如果reference类型的数据中存储的数值代表的是另外一块内存的起始地址，就称这块内存代表着一个引用。一个对象在这种定义下只有被引用或者没有被引用两种状态
    - 我们希望能描述这样一类对象：当内存空间还足够时，则能保留在内存之中；如果内存空间在进行垃圾收集后还是非常紧张，则可以抛弃这些对象。很多系统的缓存功能都符合这样的应用场景
    - 在JDK 1.2之后，Java对引用分为,4种引用强度依次逐渐减弱:
        - 强引用（Strong Reference）
            - 只要强引用还存在，垃圾收集器永远不会回收掉被引用的对象。
            - 类似"Object obj=new Object()"这类的引用
        - 软引用（Soft Reference）
            - 描述一些还有用但并非必需的对象
            - 对于软引用关联着的对象，在系统将要发生内存溢出异常之前，将会把这些对象列进回收范围之中进行第二次回收。如果这次回收还没有足够的内存，才会抛出内存溢出异常
            - 在JDK 1.2之后，提供了SoftReference类来实现软引用
        - 弱引用（Weak Reference）
            - 描述非必需对象的
            - 被弱引用关联的对象只能生存到下一次垃圾收集发生之前
            - 当垃圾收集器工作时，无论当前内存是否足够，都会回收掉只被弱引用关联的对象
            - 在JDK 1.2之后，提供了WeakReference类来实现弱引用。
        - 虚引用（Phantom Reference）
            - 也称为幽灵引用或者幻影引用
            - 一个对象是否有虚引用的存在，完全不会对其生存时间构成影响，也无法通过虚引用来取得一个对象实例。
            - 为一个对象设置虚引用关联的唯一目的就是能在这个对象被收集器回收时收到一个系统通知
            - 在JDK 1.2之后，提供了PhantomReference类来实现虚引用。
- finalize()
    - finalize()方法都只会被系统自动调用一次，如果对象面临下一次回收，它的finalize()方法不会被再次执行
    - 如果对象在进行可达性分析后发现没有与GC Roots相连接的引用链
        - 那它将会被第一次标记并且进行一次筛选，当对象没有覆盖finalize()方法，或者finalize()方法已经被虚拟机调用过，虚拟机将这两种情况都视为“没有必要执行” 直接真正回收
        - 否则会执行finalize()方法，如果对象要在finalize()重新与引用链上的任何一个对象建立关联 则该对象仍然可以存活
            - 对象将会放置在一个叫做F-Queue的队列之中，并在稍后由一个由虚拟机自动建立的、低优先级的Finalizer线程去执行它。这里所谓的“执行”是指虚拟机会触发这个方法，但并不承诺会等待它运行结束

- 方法区回收(JDK8以前)
    - 废弃常量
        - 假如一个字符串"abc"已经进入了常量池中，但是当前系统没有任何一个String对象是叫做"abc"的，换句话说，就是没有任何String对象引用常量池中的"abc"常量，也没有其他地方引用了这个字面量
    - 无用的类 类需要同时满足下面3个条件才能算是“无用的类”：
        - 该类所有的实例都已经被回收，也就是Java堆中不存在该类的任何实例。
        - 加载该类的ClassLoader已经被回收。
        - 该类对应的java.lang.Class对象没有在任何地方被引用，无法在任何地方通过反射访问该类的方法。
             
        - 仅仅是“可以”，而并不是和对象一样，不使用了就必然会回收。是否对类进行回收，HotSpot虚拟机提供了-Xnoclassgc参数进行控制
        - 还可以使用-verbose:class以及-XX:+TraceClassLoading、-XX:+TraceClassUnLoading查看类加载和卸载信息
        - 在大量使用反射、动态代理、CGLib等ByteCode框架、动态生成JSP以及OSGi这类频繁自定义ClassLoader的场景都需要虚拟机具备类卸载的功能，以保证永久代不会溢出
        
- 垃圾收集算法
    - 标记-清除算法(最基础的收集算法)
        - 分为“标记”和“清除”两个阶段：首先标记出所有需要回收的对象，在标记完成后统一回收所有被标记的对象，它的标记过程其实在前一节讲述对象标记判定时已经介绍过了
        - 不足：
            - 一个是效率问题，标记和清除两个过程的效率都不高；
            - 另一个是空间问题，标记清除之后会产生大量不连续的内存碎片
                - 空间碎片太多可能会导致分配较大对象，无法找到足够的连续内存而不得不提前触发另一次垃圾收集动作
        - 最基础的收集算法:后续的收集算法都是基于这种思路并对其不足进行改进而得到的
        
    - 复制算法(新生代收集算法)
        - 用内存按容量划分为大小相等的两块，每次只使用其中的一块。当这一块的内存用完了，就将还存活着的对象复制到另外一块上面，然后再把已使用过的内存空间一次清理掉
        - 每次都是对整个半区进行内存回收，内存分配时也就不用考虑内存碎片等复杂情况，只要移动堆顶指针，按顺序分配内存即可，实现简单，运行高效
        - 不足：内存缩小为了原来的一半
            
        - 新生代
            - 内存分为一块较大的Eden空间和两块较小的Survivor空间.HotSpot虚拟机默认Eden和Survivor的大小比例是8:1，也就是每次新生代中可用内存空间为整个新生代容量的90%（80%+10%），只有10%的内存会被“浪费”
            - 每次使用Eden和其中一块Survivor。当回收时，将Eden和Survivor中还存活着的对象一次性地复制到另外一块Survivor空间上，最后清理掉Eden和刚才用过的Survivor空间
            - 当Survivor空间不够用时，需要依赖其他内存（这里指老年代）进行分配担保（Handle Promotion）。
            - 如果另外一块Survivor空间没有足够空间存放上一次新生代收集下来的存活对象时，这些对象将直接通过分配担保机制进入老年代。
        - 不足：
            - 对象存活率较高时就要进行较多的复制操作，效率将会变低
            - 更关键的是，如果不想浪费50%的空间，就需要有额外的空间进行分配担保，以应对被使用的内存中所有对象都100%存活的极端情况
            - 老年代一般不能直接选用这种算法。
            
    - 标记-整理算法(老年代收集算法)
        - 标记过程仍然与“标记-清除”算法一样，但后续步骤不是直接对可回收对象进行清理，而是让所有存活的对象都向一端移动，然后直接清理掉端边界以外的内存
        
    - 分代收集算法
        - 只是根据对象存活周期的不同将内存划分为几块
        - 一般是把Java堆分为新生代和老年代，这样就可以根据各个年代的特点采用最适当的收集算法。  
            - 在新生代中，每次垃圾收集时都发现有大批对象死去，只有少量存活，那就选用复制算法，只需要付出少量存活对象的复制成本就可以完成收集
            - 老年代中因为对象存活率高、没有额外空间对它进行分配担保，就必须使用“标记—清理”或者“标记—整理”算法来进行回收。
            
- HotSpot的算法实现
    - HotSpot虚拟机上实现这些垃圾收集算法时，必须对算法的执行效率有严格的考量，才能保证虚拟机高效运行。
    - 枚举根节点- 在OopMap的协助下，HotSpot可以快速且准确地完成GC Roots枚举
        - 可作为GC Roots的节点主要在全局性的引用（例如常量或类静态属性）与执行上下文（例如栈帧中的本地变量表）中
        - OopMap(Ordinary Object Pointer Map)存放执行上下文和全局的引用位置，在GC停顿时直接得知哪些地方存放着对象引用,并不需要一个不漏地检查完所有执行上下文和全局的引用位置
        - 在类加载完成的时候，HotSpot就把对象内什么偏移量上是什么类型的数据计算出来，在JIT编译过程中，也会在特定的位置记录下栈和寄存器中哪些位置是引用
        
        - 可达性分析对执行时间的敏感还体现在GC停顿上，因为这项分析工作必须在一个能确保一致性的快照中进行——这里“一致性”的意思是指在整个分析期间整个执行系统看起来就像被冻结在某个时间点上，不可以出现分析过程中对象引用关系还在不断变化的情况，该点不满足的话分析结果准确性就无法得到保证。
        - 这点是导致GC进行时必须停顿所有Java执行线程（Sun将这件事情称为"Stop The World"）的其中一个重要原因，即使是在号称（几乎）不会发生停顿的CMS收集器中，枚举根节点时也是必须要停顿的
        
        - GC停顿(Stop The World)
            - 这项工作实际上是由虚拟机在后台自动发起和自动完成的，在用户不可见的情况下把用户正常工作的线程全部停掉.
            - 每次GC都会短暂停顿所有线程
        
    - 安全点(Safepoint)
        - 记录了指令OopMap信息的特定的位置，这些位置称为安全点（Safepoint）
        - 即程序执行时并非在所有地方都能停顿下来开始GC，只有在到达安全点时才能暂停
        - 如何在GC发生时让所有线程（这里不包括执行JNI调用的线程）都“跑”到最近的安全点上再停顿下来
            - 抢先式中断：在GC发生时，首先把所有线程全部中断，如果发现有线程中断的地方不在安全点上，就恢复线程，让它“跑”到安全点上
            - 主动式中断：当GC需要中断线程的时候，不直接对线程操作，仅仅简单地设置一个标志，各个线程执行时主动去轮询这个标志，发现中断标志为真时就自己中断挂起。轮询标志的地方和安全点是重合的，另外再加上创建对象需要分配内存的地方。
        
    - 安全区域(Safe Region)
        - Safepoint机制保证了程序**执行时**，在不太长的时间内就会遇到可进入GC的Safepoint
        - 程序“不执行”的时候呢？所谓的程序不执行就是没有分配CPU时间
        - 典型的例子就是线程处于Sleep状态或者Blocked状态，这时候线程无法响应JVM的中断请求，“走”到安全的地方去中断挂起，
        
        - 安全区域是指在一段代码片段之中，引用关系不会发生变化.在这个区域中的任意地方开始GC都是安全的.们也可以把Safe Region看做是被扩展了的Safepoint。
        - 在线程执行到Safe Region中的代码时，首先标识自己已经进入了Safe Region，那样，当在这段时间里JVM要发起GC时，就不用管标识自己为Safe Region状态的线程了。在线程要离开Safe Region时，它要检查系统是否已经完成了根节点枚举（或者是整个GC过程），如果完成了，那线程就继续执行，否则它就必须等待直到收到可以安全离开Safe Region的信号为止。
        
- 垃圾收集器(HotSpot虚拟机, 因此不同的厂商、不同版本的虚拟机所提供的垃圾收集器都可能会有很大差别)
    - 老年代收集器、新生代收集器
    - 收集器与收集器之间的搭配
    
    - 当JVM用于运行服务器后台程序时建议用Server模式。 java -version 可以查看模式
    - 垃圾收集器的上下文语境中
        - 并行（Parallel）：指多条垃圾收集线程并行工作，但此时用户线程仍然处于等待状态。
        - 并发（Concurrent）：指用户线程与垃圾收集线程同时执行（但不一定是并行的，可能会交替执行，存在线程间切换），用户程序在继续运行，而垃圾收集程序运行于另一个CPU上
        
    - Serial收集器
        - 新生代收集器 复制算法 (Client模式下的默认新生代收集器)
        - 一个单线程的收集器
        - 但它的“单线程”的意义并不仅仅说明它只会使用一个CPU或一条收集线程去完成垃圾收集工作，更重要的是在它进行垃圾收集时，必须暂停其他所有的工作线程，直到它收集结束
        - 从Serial收集器到Parallel收集器，再到Concurrent Mark Sweep（CMS）乃至GC收集器的最前沿成果Garbage First（G1）收集器，我们看到了一个个越来越优秀（也越来越复杂）的收集器的出现，用户线程的停顿时间在不断缩短，但是仍然没有办法完全消除（这里暂不包括RTSJ中的收集器）
        
    - Serial Old收集器(PS MarkSweep)
        - Serial收集器的老年代版本
        - 老年代 单线程 标记-整理算法
        - Client模式
            - 主要意义也是在于给Client模式下的虚拟机使用
        - Server模式
            - 作为CMS收集器的后备预案，在并发收集发生Concurrent Mode Failure时使用
            
    - ParNew收集器
        - 新生代 复制算法
        - Serial收集器的多线程版本，并行
        - 除了使用多条线程进行垃圾收集之外，其余行为与Serial收集器完全一样
        - 是许多运行在Server模式下的虚拟机中首选的新生代收集器，其中有一个与性能无关但很重要的原因是，除了Serial收集器外，目前只有它能与CMS收集器配合工作
        - ParNew收集器也是使用-XX:+UseConcMarkSweepGC选项后的默认新生代收集器，也可以使用-XX:+UseParNewGC选项来强制指定它。
    
        - ParNew收集器在单CPU的环境中绝对不会有比Serial收集器更好的效果，甚至由于存在线程交互的开销，该收集器在通过超线程技术实现的两个CPU的环境中都不能百分之百地保证可以超越Serial收集器
        - 当然，随着可以使用的CPU的数量的增加，它对于GC时系统资源的有效利用还是很有好处的
        - 它默认开启的收集线程数与CPU的数量相同，在CPU非常多的环境下，可以使用-XX:ParallelGCThreads参数来限制垃圾收集的线程数。
    
    - Parallel Scavenge 收集器
        - 新生代收集器 复制算法
        - 并行的多线程收集器， 经常称为“吞吐量优先”收集器
        - Parallel Scavenge收集器的特点是它的关注点与其他收集器不同。Parallel Scavenge收集器的目标则是达到一个可控制的吞吐量（Throughput）。CMS等收集器的关注点是尽可能地缩短垃圾收集时用户线程的停顿时间
        - 吞吐量就是CPU用于运行用户代码的时间与CPU总消耗时间的比值，即吞吐量=运行用户代码时间/（运行用户代码时间+垃圾收集时间）
        - 高吞吐量则可以高效率地利用CPU时间，尽快完成程序的运算任务，主要适合在后台运算而不需要太多交互的任务
        - 两个参数用于精确控制吞吐量
            - 控制最大垃圾收集停顿时间的-XX:MaxGCPauseMillis参数
                - 收集器将尽可能地保证内存回收花费的时间不超过设定值
                - GC停顿时间缩短是以牺牲吞吐量和新生代空间来换取的：
                    - 系统把新生代调小一些，收集300MB新生代肯定比收集500MB快吧，这也直接导致垃圾收集发生得更频繁一些，原来10秒收集一次、每次停顿100毫秒，现在变成5秒收集一次、每次停顿70毫秒。停顿时间的确在下降，但吞吐量也降下来了
            - 直接设置吞吐量大小的-XX:GCTimeRatio 参数
               - 提示虚拟机需要在收集器中花费不超过应用程序执行时间的1 /（1 + nnn）。
               - 例如，-XX:GCTimeRatio=19设定GC总时间为5％目标，吞吐量目标为95％。也就是说，应用程序的时间应该是收集器的19倍。
               - 默认情况下，该值为99，这意味着应用程序应至少获得收集器时间的99倍。也就是说，收集器的运行时间不应超过总时间的1％。这被选为服务器应用程序的不错选择。如果值太高将导致堆的大小增大到最大值。
               
            - -XX:GCTimeRatio=nnn
                - A hint to the virtual machine that it's desirable that not more than 1 / (1 + nnn) of the application execution time be spent in the collector.
                - For example -XX:GCTimeRatio=19 sets a goal of 5% of the total time for GC and throughput goal of 95%. That is, the application should get 19 times as much time as the collector.
                - By default the value is 99, meaning the application should get at least 99 times as much time as the collector. That is, the collector should run for not more than 1% of the total time. This was selected as a good choice for server applications. A value that is too high will cause the size of the heap to grow to its maximum.
        - -XX:+UseAdaptiveSizePolicy
            - 自适应调节策略也是Parallel Scavenge收集器与ParNew收集器的一个重要区别。
                - 如果GC暂停时间大于暂停时间(-XX:MaxGCPauseMillis=nnn)目标，则减少新生代大小以更好地实现目标
                - 如果满足暂停时间目标，则考虑应用程序的吞吐量目标(-XX:GCTimeRatio)。如果未满足应用程序的吞吐量目标，则增加新生代的大小以更好地实现目标。
                - 如果同时满足暂停时间目标和吞吐量目标，则减少新生代的大小以减少占用空间。
            - The implementation of -XX:+UseAdaptiveSizePolicy checks (in this order):
                - If the GC pause time is greater than the pause time goal then reduce the generations sizes to better attain the goal.
                - If the pause time goal is being met then consider the application's throughput goal. If the application's throughput goal is not being met, then increase the sizes of the generations to better attain the goal.
                - If both the pause time goal and the throughput goal are being met, then the size of the generations are decreased to reduce footprint.
            - 这是一个开关参数，当这个参数打开之后，就不需要手工指定新生代的大小（-Xmn）、Eden与Survivor区的比例（-XX:SurvivorRatio）、晋升老年代对象年龄（-XX:PretenureSizeThreshold）等细节参数了，虚拟机会根据当前系统的运行情况收集性能监控信息，动态调整这些参数以提供最合适的停顿时间或者最大的吞吐量，这种调节方式称为GC自适应的调节策略（GC Ergonomics）                
            
    - Parallel Old收集器
        - Parallel Scavenge收集器的老年代版本
        - 老年代 并行的多线程收集器 标记-整理算法
        - 注重吞吐量以及CPU资源敏感的场合，都可以优先考虑Parallel Scavenge加Parallel Old收集器
        
    - CMS收集器（Concurrent Mark Sweep）
        - 老年代
        - 标记-清除算法(Mark Sweep)
        - 是一种以获取最短回收停顿时间为目标的收集器
        - 目前很大一部分的Java应用集中在互联网站或者B/S系统的服务端上，这类应用尤其重视服务的响应速度，希望系统停顿时间最短，以给用户带来较好的体验
        - 整个过程分为4个步骤: 初始标记、重新标记这两个步骤仍然需要"Stop The World"
            - 初始标记（CMS initial mark）
                - 需要"Stop The World"
                - 仅仅只是标记一下GC Roots能直接关联到的对象，速度很快
                
            - 并发标记（CMS concurrent mark）
                - 并发标记阶段就是进行GC RootsTracing的过程
                
            - 重新标记（CMS remark）
                - 需要"Stop The World"
                - 为了修正并发标记期间因用户程序继续运作而导致标记产生变动的那一部分对象的标记记录
                - 这个阶段的停顿时间一般会比初始标记阶段稍长一些，但远比并发标记的时间短
                - 与用户线程一起工作
                
            - 并发清除（CMS concurrent sweep）
                - 耗时长
                - 与用户线程一起工作
                
        - 由于整个过程中耗时最长的并发标记和并发清除过程收集器线程都可以与用户线程一起工作，所以，从总体上来说，CMS收集器的内存回收过程是与用户线程一起并发执行的
        - 并发收集、低停顿，也称之为并发低停顿收集器（Concurrent Low Pause Collector）
        - 3个明显的缺点：
            - CMS收集器对CPU资源非常敏感：
                - 其实，面向并发设计的程序都对CPU资源比较敏感，在并发阶段，它虽然不会导致用户线程停顿，但是会因为占用了一部分线程（或者说CPU资源）而导致应用程序变慢，总吞吐量会降低
                - CMS默认启动的回收线程数是（CPU数量+3）/4，也就是当CPU在4个以上时，并发回收时垃圾收集线程不少于25%的CPU资源，并且随着CPU数量的增加而下降。但是当CPU不足4个（譬如2个）时，CMS对用户程序的影响就可能变得很大，如果本来CPU负载就比较大，还分出一半的运算能力去执行收集器线程，就可能导致用户程序的执行速度忽然降低了50%，其实也让人无法接受
                
            - 无法处理浮动垃圾（Floating Garbage），可能出现"Concurrent Mode Failure"失败而导致另一次Full GC的产生
                - 由于CMS并发清理阶段用户线程还在运行着，伴随程序运行自然就还会有新的垃圾不断产生，这一部分垃圾出现在标记过程之后，CMS无法在当次收集中处理掉它们，只好留待下一次GC时再清理掉。这一部分垃圾就称为“浮动垃圾”
                - 也是由于在垃圾收集阶段用户线程还需要运行，那也就还需要预留有足够的内存空间给用户线程使用。
                - 要是CMS运行期间预留的内存无法满足程序需要，就会出现一次"Concurrent Mode Failure"失败，这时虚拟机将启动后备预案：临时启用Serial Old收集器来重新进行老年代的垃圾收集，这样停顿时间就很长了。
                - 参数-XX:CMSInitiatingOccupancyFraction设置得太高很容易导致大量"Concurrent Mode Failure"失败，性能反而降低。
                - he default value for this initiating occupancy threshold is approximately 92%, but the value is subject to change from release to release. This value can be manually adjusted using the command-line option -XX:CMSInitiatingOccupancyFraction=<N>, where <N> is an integral percentage (0 to 100) of the tenured generation size.
                  
            - 大量空间碎片产生
                - MS是一款基于“标记—清除”算法实现的收集器
                - 空间碎片过多时，将会给大对象分配带来很大麻烦，往往会出现老年代还有很大空间剩余，但是无法找到足够大的连续空间来分配当前对象，不得不提前触发一次Full GC
                
                - XX:+UseCMSCompactAtFullCollection开关参数（默认就是开启的）
                    - 用于在CMS收集器顶不住要进行FullGC时开启内存碎片的合并整理过程
                    - 内存整理的过程是无法并发的，空间碎片问题没有了，但停顿时间不得不变长
                    
                - -XX:CMSFullGCsBeforeCompaction
                    - 用于设置执行多少次不压缩的Full GC后，跟着来一次带压缩的（默认值为0，表示每次进入Full GC时都进行碎片整理）
                