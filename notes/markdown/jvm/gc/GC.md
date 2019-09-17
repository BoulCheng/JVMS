# 垃圾收集器与内存分配策略

- 当需要排查各种内存溢出、内存泄漏问题时，当垃圾收集成为系统达到更高并发量的瓶颈时，我们就需要对这些“自动化”的技术(内存动态分配和垃圾收集技术)实施必要的监控和调节。

- 程序计数器、虚拟机栈、本地方法栈3个区域随线程而生，随线程而灭；栈中的栈帧随着方法的进入和退出而有条不紊地执行着出栈和入栈操作;因此这几个区域的内存分配和回收都具备确定性，在这几个区域内就不需要过多考虑回收的问题，因为方法结束或者线程结束时，内存自然就跟随着回收了

- Java堆和方法区,内存的分配和回收都是动态的，垃圾收集器所关注的是这部分内存，本章后续讨论中的“内存”分配与回收也仅指这一部分内存。

- 大多数情况下，对象在新生代Eden区中分配。当Eden区没有足够空间进行分配时，虚拟机将发起一次Minor GC
- 当 Survivor可能不足以容纳Eden和另一个Survivor中的存活对象。如果Survivor中的存活对象溢出，多余的对象将被移到老年代，如果此时老年代满了而无法容纳更多的对象，则会触发Full GC

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
            
        - 默认的，新生代 ( Young ) 与老年代 ( Old ) 的比例的值为 1:2 ( 该值可以通过参数 –XX:NewRatio 来指定 )，即：新生代 ( Young ) = 1/3 的堆空间大小。老年代 ( Old ) = 2/3 的堆空间大小。其中，新生代 ( Young ) 被细分为 Eden 和 两个 Survivor 区域，这两个 Survivor 区域分别被命名为 from 和 to，以示区分。
          默认的，Edem : from : to = 8 : 1 : 1 ( 可以通过参数 –XX:SurvivorRatio 来设定 )，即： Eden = 8/10 的新生代空间大小，from = to = 1/10 的新生代空间大小。
          JVM 每次只会使用 Eden 和其中的一块 Survivor 区域来为对象服务，所以无论什么时候，总是有一块 Survivor 区域是空闲着的。
          因此，新生代实际可用的内存空间为 9/10 ( 即90% )的新生代空间
          
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
        
    - 安全区域(Safe Region) **引用关系不会发生变化的代码区域**
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
               
            - For example, -XX:GCTimeRatio=19 sets a goal of 1/20 or 5% of the total time in garbage collection. The default value is 99, resulting in a goal of 1% of the time in garbage collection.
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
                - 与用户线程一起工作
                
            - 重新标记（CMS remark）
                - 需要"Stop The World"
                - 为了修正并发标记期间因用户程序继续运作而导致标记产生变动的那一部分对象的标记记录
                - 这个阶段的停顿时间一般会比初始标记阶段稍长一些，但远比并发标记的时间短
                
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
                
    - G1收集器
        - G1是一款面向服务端应用的垃圾收集器
        - G1可以不需要其他收集器配合就能独立管理整个GC堆
        - HotSpot开发团队赋予它的使命是（在比较长期的）未来可以替换掉JDK 1.5中发布的CMS收集器
        - 与其他GC收集器相比，G1具备如下特点:
            - 并行与并发:使用多个CPU（CPU或者CPU核心）来缩短Stop-The-World停顿的时间
            - 分代收集:分代概念在G1中依然得以保留,G1可以不需要其他收集器配合就能独立管理整个GC堆，但它能够采用不同的方式去处理新创建的对象和已经存活了一段时间、熬过多次GC的旧对象以获取更好的收集效果。
            - 空间整合:G1从整体来看是基于“标记—整理”算法实现的收集器，从局部（两个Region之间）上来看是基于“复制”算法实现的，但无论如何，这两种算法都意味着G1运作期间不会产生内存空间碎片
            - 可预测的停顿:这是G1相对于CMS的另一大优势，降低停顿时间是G1和CMS共同的关注点，但G1除了追求低停顿外，还能建立可预测的停顿时间模型，能让使用者明确指定在一个长度为M毫秒的时间片段内，消耗在垃圾收集上的时间不得超过N毫秒，这几乎已经是实时Java（RTSJ）的垃圾收集器的特征了。
            
        - 使用G1收集器时，Java堆的内存布局就与其他收集器有很大差别，它将整个Java堆划分为多个大小相等的独立区域（Region），虽然还保留有新生代和老年代的概念，但新生代和老年代不再是物理隔离的了，它们都是一部分Region（不需要连续）的集合。
        - G1收集器之所以能建立可预测的停顿时间模型，是因为它可以有计划地避免在整个Java堆中进行全区域的垃圾收集,G1跟踪各个Region里面的垃圾堆积的价值大小（回收所获得的空间大小以及回收所需时间的经验值），在后台维护一个优先列表，每次根据允许的收集时间，优先回收价值最大的Region（这也就是Garbage-First名称的来由）,这种使用Region划分内存空间以及有优先级的区域回收方式，保证了G1收集器在有限的时间内可以获取尽可能高的收集效率。
        
        - 如果不计算维护Remembered Set的操作，G1收集器的运作大致可划分为以下几个步骤：
            - 初始标记（Initial Marking）
                - 初始标记阶段仅仅只是标记一下GC Roots能直接关联到的对象
                - 这阶段需要停顿线程，但耗时很短
            - 并发标记（Concurrent Marking）
                - 并发标记阶段是从GC Root开始对堆中对象进行可达性分析，找出存活的对象，这阶段耗时较长，但可与用户程序并发执行
            - 最终标记（Final Marking）
                - 最终标记阶段则是为了修正在并发标记期间因用户程序继续运作而导致标记产生变动的那一部分标记记录
                - 这阶段需要停顿线程，但是可**并行**执行
            - 筛选回收（Live Data Counting and Evacuation）
                - 筛选回收阶段首先对各个Region的回收价值和成本进行排序，根据用户所期望的GC停顿时间来制定回收计划
                - 这个阶段其实也可以做到与用户程序一起并发执行，但是因为只回收一部分Region，时间是用户可控制的，而且停顿用户线程将大幅提高收集效率
        - 应用追求低停顿，那G1现在已经可以作为一个可尝试的选择，如果你的应用追求吞吐量，那G1并不会为你带来什么特别的好处。
        
        
        
- 查看垃圾收集器(默认)

```
zhenglubiaodeMacBook-Pro:six zlb$
zhenglubiaodeMacBook-Pro:six zlb$
zhenglubiaodeMacBook-Pro:six zlb$ touch GCTest
zhenglubiaodeMacBook-Pro:six zlb$ rm -rf GCTest
zhenglubiaodeMacBook-Pro:six zlb$
zhenglubiaodeMacBook-Pro:six zlb$ vi GCTest
zhenglubiaodeMacBook-Pro:six zlb$
zhenglubiaodeMacBook-Pro:six zlb$ cat GCTest
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

public class GCTest {
    public static void main(String args[]) {
        List<GarbageCollectorMXBean> l = ManagementFactory.getGarbageCollectorMXBeans();
        for(GarbageCollectorMXBean b : l) {
            System.out.println(b.getName());
        }
    }
}
zhenglubiaodeMacBook-Pro:six zlb$
zhenglubiaodeMacBook-Pro:six zlb$ javac GCTest
错误: 仅当显式请求注释处理时才接受类名称 'GCTest'
1 个错误
zhenglubiaodeMacBook-Pro:six zlb$ mv GCTest GCTest.java
zhenglubiaodeMacBook-Pro:six zlb$
zhenglubiaodeMacBook-Pro:six zlb$ ls -l
total 40
-rw-r--r--   1 zlb  staff   387  9 17 15:37 GCTest.java
drwxr-xr-x   7 zlb  staff   224  5 12 17:32 elastic-job-study
drwxr-xr-x  14 zlb  staff   448  2 15  2019 fota-account
drwxr-xr-x  10 zlb  staff   320  1 24  2019 fota-commander
drwxr-xr-x  10 zlb  staff   320  4  4 11:50 fota-option
drwxr-xr-x   9 zlb  staff   288  2 11  2019 fota-policy
drwxr-xr-x  10 zlb  staff   320  8 13 14:13 fota-wbsocket
drwxr-xr-x  14 zlb  staff   448  1 24  2019 fota-web
drwxr-xr-x  14 zlb  staff   448 12 25  2018 fota_monitor
drwxr-xr-x   4 zlb  staff   128 12 27  2018 jdk
-rw-r--r--   1 zlb  staff   914  9 12 15:57 threadPool$1$1.class
-rw-r--r--   1 zlb  staff  1536  9 12 15:57 threadPool$1.class
-rw-r--r--   1 zlb  staff   717  9 12 15:57 threadPool.class
-rw-r--r--   1 zlb  staff  1129  9 12 15:57 threadPool.java
drwxr-xr-x   8 zlb  staff   256 12 27  2018 tools
zhenglubiaodeMacBook-Pro:six zlb$
zhenglubiaodeMacBook-Pro:six zlb$ javac GCTest.java
zhenglubiaodeMacBook-Pro:six zlb$
zhenglubiaodeMacBook-Pro:six zlb$ ls -l
total 48
-rw-r--r--   1 zlb  staff   843  9 17 15:39 GCTest.class
-rw-r--r--   1 zlb  staff   387  9 17 15:37 GCTest.java
drwxr-xr-x   7 zlb  staff   224  5 12 17:32 elastic-job-study
drwxr-xr-x  14 zlb  staff   448  2 15  2019 fota-account
drwxr-xr-x  10 zlb  staff   320  1 24  2019 fota-commander
drwxr-xr-x  10 zlb  staff   320  4  4 11:50 fota-option
drwxr-xr-x   9 zlb  staff   288  2 11  2019 fota-policy
drwxr-xr-x  10 zlb  staff   320  8 13 14:13 fota-wbsocket
drwxr-xr-x  14 zlb  staff   448  1 24  2019 fota-web
drwxr-xr-x  14 zlb  staff   448 12 25  2018 fota_monitor
drwxr-xr-x   4 zlb  staff   128 12 27  2018 jdk
-rw-r--r--   1 zlb  staff   914  9 12 15:57 threadPool$1$1.class
-rw-r--r--   1 zlb  staff  1536  9 12 15:57 threadPool$1.class
-rw-r--r--   1 zlb  staff   717  9 12 15:57 threadPool.class
-rw-r--r--   1 zlb  staff  1129  9 12 15:57 threadPool.java
drwxr-xr-x   8 zlb  staff   256 12 27  2018 tools
zhenglubiaodeMacBook-Pro:six zlb$
zhenglubiaodeMacBook-Pro:six zlb$ java GCTest
PS Scavenge
PS MarkSweep
zhenglubiaodeMacBook-Pro:six zlb$
zhenglubiaodeMacBook-Pro:six zlb$
zhenglubiaodeMacBook-Pro:six zlb$
zhenglubiaodeMacBook-Pro:six zlb$ java -XX:+PrintCommandLineFlags GCTest
-XX:InitialHeapSize=134217728 -XX:MaxHeapSize=2147483648 -XX:+PrintCommandLineFlags -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseParallelGC
PS Scavenge
PS MarkSweep
zhenglubiaodeMacBook-Pro:six zlb$
zhenglubiaodeMacBook-Pro:six zlb$
zhenglubiaodeMacBook-Pro:six zlb$
zhenglubiaodeMacBook-Pro:six zlb$ java -XX:+PrintFlagsFinal GCTest
[Global flags]
    uintx AdaptiveSizeDecrementScaleFactor          = 4                                   {product}
    uintx AdaptiveSizeMajorGCDecayTimeScale         = 10                                  {product}
    uintx AdaptiveSizePausePolicy                   = 0                                   {product}
    uintx AdaptiveSizePolicyCollectionCostMargin    = 50                                  {product}
    uintx AdaptiveSizePolicyInitializingSteps       = 20                                  {product}
    uintx AdaptiveSizePolicyOutputInterval          = 0                                   {product}
    uintx AdaptiveSizePolicyWeight                  = 10                                  {product}
    uintx AdaptiveSizeThroughPutPolicy              = 0                                   {product}
    uintx AdaptiveTimeWeight                        = 25                                  {product}
     bool AdjustConcurrency                         = false                               {product}
     bool AggressiveOpts                            = false                               {product}
     intx AliasLevel                                = 3                                   {C2 product}
     bool AlignVector                               = false                               {C2 product}
     intx AllocateInstancePrefetchLines             = 1                                   {product}
     intx AllocatePrefetchDistance                  = 192                                 {product}
     intx AllocatePrefetchInstr                     = 3                                   {product}
     intx AllocatePrefetchLines                     = 4                                   {product}
     intx AllocatePrefetchStepSize                  = 64                                  {product}
     intx AllocatePrefetchStyle                     = 1                                   {product}
     bool AllowJNIEnvProxy                          = false                               {product}
     bool AllowNonVirtualCalls                      = false                               {product}
     bool AllowParallelDefineClass                  = false                               {product}
     bool AllowUserSignalHandlers                   = false                               {product}
     bool AlwaysActAsServerClassMachine             = false                               {product}
     bool AlwaysCompileLoopMethods                  = false                               {product}
     bool AlwaysLockClassLoader                     = false                               {product}
     bool AlwaysPreTouch                            = false                               {product}
     bool AlwaysRestoreFPU                          = false                               {product}
     bool AlwaysTenure                              = false                               {product}
     bool AssertOnSuspendWaitFailure                = false                               {product}
     bool AssumeMP                                  = false                               {product}
     intx AutoBoxCacheMax                           = 128                                 {C2 product}
    uintx AutoGCSelectPauseMillis                   = 5000                                {product}
     intx BCEATraceLevel                            = 0                                   {product}
     intx BackEdgeThreshold                         = 100000                              {pd product}
     bool BackgroundCompilation                     = true                                {pd product}
    uintx BaseFootPrintEstimate                     = 268435456                           {product}
     intx BiasedLockingBulkRebiasThreshold          = 20                                  {product}
     intx BiasedLockingBulkRevokeThreshold          = 40                                  {product}
     intx BiasedLockingDecayTime                    = 25000                               {product}
     intx BiasedLockingStartupDelay                 = 4000                                {product}
     bool BindGCTaskThreadsToCPUs                   = false                               {product}
     bool BlockLayoutByFrequency                    = true                                {C2 product}
     intx BlockLayoutMinDiamondPercentage           = 20                                  {C2 product}
     bool BlockLayoutRotateLoops                    = true                                {C2 product}
     bool BranchOnRegister                          = false                               {C2 product}
     bool BytecodeVerificationLocal                 = false                               {product}
     bool BytecodeVerificationRemote                = true                                {product}
     bool C1OptimizeVirtualCallProfiling            = true                                {C1 product}
     bool C1ProfileBranches                         = true                                {C1 product}
     bool C1ProfileCalls                            = true                                {C1 product}
     bool C1ProfileCheckcasts                       = true                                {C1 product}
     bool C1ProfileInlinedCalls                     = true                                {C1 product}
     bool C1ProfileVirtualCalls                     = true                                {C1 product}
     bool C1UpdateMethodData                        = true                                {C1 product}
     intx CICompilerCount                          := 3                                   {product}
     bool CICompilerCountPerCPU                     = true                                {product}
     bool CITime                                    = false                               {product}
     bool CMSAbortSemantics                         = false                               {product}
    uintx CMSAbortablePrecleanMinWorkPerIteration   = 100                                 {product}
     intx CMSAbortablePrecleanWaitMillis            = 100                                 {manageable}
    uintx CMSBitMapYieldQuantum                     = 10485760                            {product}
    uintx CMSBootstrapOccupancy                     = 50                                  {product}
     bool CMSClassUnloadingEnabled                  = true                                {product}
    uintx CMSClassUnloadingMaxInterval              = 0                                   {product}
     bool CMSCleanOnEnter                           = true                                {product}
     bool CMSCompactWhenClearAllSoftRefs            = true                                {product}
    uintx CMSConcMarkMultiple                       = 32                                  {product}
     bool CMSConcurrentMTEnabled                    = true                                {product}
    uintx CMSCoordinatorYieldSleepCount             = 10                                  {product}
     bool CMSDumpAtPromotionFailure                 = false                               {product}
     bool CMSEdenChunksRecordAlways                 = true                                {product}
    uintx CMSExpAvgFactor                           = 50                                  {product}
     bool CMSExtrapolateSweep                       = false                               {product}
    uintx CMSFullGCsBeforeCompaction                = 0                                   {product}
    uintx CMSIncrementalDutyCycle                   = 10                                  {product}
    uintx CMSIncrementalDutyCycleMin                = 0                                   {product}
     bool CMSIncrementalMode                        = false                               {product}
    uintx CMSIncrementalOffset                      = 0                                   {product}
     bool CMSIncrementalPacing                      = true                                {product}
    uintx CMSIncrementalSafetyFactor                = 10                                  {product}
    uintx CMSIndexedFreeListReplenish               = 4                                   {product}
     intx CMSInitiatingOccupancyFraction            = -1                                  {product}
    uintx CMSIsTooFullPercentage                    = 98                                  {product}
   double CMSLargeCoalSurplusPercent                = 0.950000                            {product}
   double CMSLargeSplitSurplusPercent               = 1.000000                            {product}
     bool CMSLoopWarn                               = false                               {product}
    uintx CMSMaxAbortablePrecleanLoops              = 0                                   {product}
     intx CMSMaxAbortablePrecleanTime               = 5000                                {product}
    uintx CMSOldPLABMax                             = 1024                                {product}
    uintx CMSOldPLABMin                             = 16                                  {product}
    uintx CMSOldPLABNumRefills                      = 4                                   {product}
    uintx CMSOldPLABReactivityFactor                = 2                                   {product}
     bool CMSOldPLABResizeQuicker                   = false                               {product}
    uintx CMSOldPLABToleranceFactor                 = 4                                   {product}
     bool CMSPLABRecordAlways                       = true                                {product}
    uintx CMSParPromoteBlocksToClaim                = 16                                  {product}
     bool CMSParallelInitialMarkEnabled             = true                                {product}
     bool CMSParallelRemarkEnabled                  = true                                {product}
     bool CMSParallelSurvivorRemarkEnabled          = true                                {product}
    uintx CMSPrecleanDenominator                    = 3                                   {product}
    uintx CMSPrecleanIter                           = 3                                   {product}
    uintx CMSPrecleanNumerator                      = 2                                   {product}
     bool CMSPrecleanRefLists1                      = true                                {product}
     bool CMSPrecleanRefLists2                      = false                               {product}
     bool CMSPrecleanSurvivors1                     = false                               {product}
     bool CMSPrecleanSurvivors2                     = true                                {product}
    uintx CMSPrecleanThreshold                      = 1000                                {product}
     bool CMSPrecleaningEnabled                     = true                                {product}
     bool CMSPrintChunksInDump                      = false                               {product}
     bool CMSPrintEdenSurvivorChunks                = false                               {product}
     bool CMSPrintObjectsInDump                     = false                               {product}
    uintx CMSRemarkVerifyVariant                    = 1                                   {product}
     bool CMSReplenishIntermediate                  = true                                {product}
    uintx CMSRescanMultiple                         = 32                                  {product}
    uintx CMSSamplingGrain                          = 16384                               {product}
     bool CMSScavengeBeforeRemark                   = false                               {product}
    uintx CMSScheduleRemarkEdenPenetration          = 50                                  {product}
    uintx CMSScheduleRemarkEdenSizeThreshold        = 2097152                             {product}
    uintx CMSScheduleRemarkSamplingRatio            = 5                                   {product}
   double CMSSmallCoalSurplusPercent                = 1.050000                            {product}
   double CMSSmallSplitSurplusPercent               = 1.100000                            {product}
     bool CMSSplitIndexedFreeListBlocks             = true                                {product}
     intx CMSTriggerInterval                        = -1                                  {manageable}
    uintx CMSTriggerRatio                           = 80                                  {product}
     intx CMSWaitDuration                           = 2000                                {manageable}
    uintx CMSWorkQueueDrainThreshold                = 10                                  {product}
     bool CMSYield                                  = true                                {product}
    uintx CMSYieldSleepCount                        = 0                                   {product}
    uintx CMSYoungGenPerWorker                      = 67108864                            {pd product}
    uintx CMS_FLSPadding                            = 1                                   {product}
    uintx CMS_FLSWeight                             = 75                                  {product}
    uintx CMS_SweepPadding                          = 1                                   {product}
    uintx CMS_SweepTimerThresholdMillis             = 10                                  {product}
    uintx CMS_SweepWeight                           = 75                                  {product}
     bool CheckEndorsedAndExtDirs                   = false                               {product}
     bool CheckJNICalls                             = false                               {product}
     bool ClassUnloading                            = true                                {product}
     bool ClassUnloadingWithConcurrentMark          = true                                {product}
     intx ClearFPUAtPark                            = 0                                   {product}
     bool ClipInlining                              = true                                {product}
    uintx CodeCacheExpansionSize                    = 65536                               {pd product}
    uintx CodeCacheMinimumFreeSpace                 = 512000                              {product}
     bool CollectGen0First                          = false                               {product}
     bool CompactFields                             = true                                {product}
     intx CompilationPolicyChoice                   = 3                                   {product}
ccstrlist CompileCommand                            =                                     {product}
    ccstr CompileCommandFile                        =                                     {product}
ccstrlist CompileOnly                               =                                     {product}
     intx CompileThreshold                          = 10000                               {pd product}
     bool CompilerThreadHintNoPreempt               = true                                {product}
     intx CompilerThreadPriority                    = -1                                  {product}
     intx CompilerThreadStackSize                   = 0                                   {pd product}
    uintx CompressedClassSpaceSize                  = 1073741824                          {product}
    uintx ConcGCThreads                             = 0                                   {product}
     intx ConditionalMoveLimit                      = 3                                   {C2 pd product}
     intx ContendedPaddingWidth                     = 128                                 {product}
     bool ConvertSleepToYield                       = true                                {pd product}
     bool ConvertYieldToSleep                       = false                               {product}
     bool CrashOnOutOfMemoryError                   = false                               {product}
     bool CreateMinidumpOnCrash                     = false                               {product}
     bool CriticalJNINatives                        = true                                {product}
     bool DTraceAllocProbes                         = false                               {product}
     bool DTraceMethodProbes                        = false                               {product}
     bool DTraceMonitorProbes                       = false                               {product}
     bool Debugging                                 = false                               {product}
    uintx DefaultMaxRAMFraction                     = 4                                   {product}
     intx DefaultThreadPriority                     = -1                                  {product}
     intx DeferPollingPageLoopCount                 = -1                                  {product}
     intx DeferThrSuspendLoopCount                  = 4000                                {product}
     bool DeoptimizeRandom                          = false                               {product}
     bool DisableAttachMechanism                    = false                               {product}
     bool DisableExplicitGC                         = false                               {product}
     bool DisplayVMOutputToStderr                   = false                               {product}
     bool DisplayVMOutputToStdout                   = false                               {product}
     bool DoEscapeAnalysis                          = true                                {C2 product}
     bool DontCompileHugeMethods                    = true                                {product}
     bool DontYieldALot                             = false                               {pd product}
    ccstr DumpLoadedClassList                       =                                     {product}
     bool DumpReplayDataOnError                     = true                                {product}
     bool DumpSharedSpaces                          = false                               {product}
     bool EagerXrunInit                             = false                               {product}
     intx EliminateAllocationArraySizeLimit         = 64                                  {C2 product}
     bool EliminateAllocations                      = true                                {C2 product}
     bool EliminateAutoBox                          = true                                {C2 product}
     bool EliminateLocks                            = true                                {C2 product}
     bool EliminateNestedLocks                      = true                                {C2 product}
     intx EmitSync                                  = 0                                   {product}
     bool EnableContended                           = true                                {product}
     bool EnableResourceManagementTLABCache         = true                                {product}
     bool EnableSharedLookupCache                   = true                                {product}
     bool EnableTracing                             = false                               {product}
    uintx ErgoHeapSizeLimit                         = 0                                   {product}
    ccstr ErrorFile                                 =                                     {product}
    ccstr ErrorReportServer                         =                                     {product}
   double EscapeAnalysisTimeout                     = 20.000000                           {C2 product}
     bool EstimateArgEscape                         = true                                {product}
     bool ExitOnOutOfMemoryError                    = false                               {product}
     bool ExplicitGCInvokesConcurrent               = false                               {product}
     bool ExplicitGCInvokesConcurrentAndUnloadsClasses  = false                               {product}
     bool ExtendedDTraceProbes                      = false                               {product}
    ccstr ExtraSharedClassListFile                  =                                     {product}
     bool FLSAlwaysCoalesceLarge                    = false                               {product}
    uintx FLSCoalescePolicy                         = 2                                   {product}
   double FLSLargestBlockCoalesceProximity          = 0.990000                            {product}
     bool FailOverToOldVerifier                     = true                                {product}
     bool FastTLABRefill                            = true                                {product}
     intx FenceInstruction                          = 0                                   {ARCH product}
     intx FieldsAllocationStyle                     = 1                                   {product}
     bool FilterSpuriousWakeups                     = true                                {product}
    ccstr FlightRecorderOptions                     =                                     {product}
     bool ForceNUMA                                 = false                               {product}
     bool ForceTimeHighResolution                   = false                               {product}
     intx FreqInlineSize                            = 325                                 {pd product}
   double G1ConcMarkStepDurationMillis              = 10.000000                           {product}
    uintx G1ConcRSHotCardLimit                      = 4                                   {product}
    uintx G1ConcRSLogCacheSize                      = 10                                  {product}
     intx G1ConcRefinementGreenZone                 = 0                                   {product}
     intx G1ConcRefinementRedZone                   = 0                                   {product}
     intx G1ConcRefinementServiceIntervalMillis     = 300                                 {product}
    uintx G1ConcRefinementThreads                   = 0                                   {product}
     intx G1ConcRefinementThresholdStep             = 0                                   {product}
     intx G1ConcRefinementYellowZone                = 0                                   {product}
    uintx G1ConfidencePercent                       = 50                                  {product}
    uintx G1HeapRegionSize                          = 0                                   {product}
    uintx G1HeapWastePercent                        = 5                                   {product}
    uintx G1MixedGCCountTarget                      = 8                                   {product}
     intx G1RSetRegionEntries                       = 0                                   {product}
    uintx G1RSetScanBlockSize                       = 64                                  {product}
     intx G1RSetSparseRegionEntries                 = 0                                   {product}
     intx G1RSetUpdatingPauseTimePercent            = 10                                  {product}
     intx G1RefProcDrainInterval                    = 10                                  {product}
    uintx G1ReservePercent                          = 10                                  {product}
    uintx G1SATBBufferEnqueueingThresholdPercent    = 60                                  {product}
     intx G1SATBBufferSize                          = 1024                                {product}
     intx G1UpdateBufferSize                        = 256                                 {product}
     bool G1UseAdaptiveConcRefinement               = true                                {product}
    uintx GCDrainStackTargetSize                    = 64                                  {product}
    uintx GCHeapFreeLimit                           = 2                                   {product}
    uintx GCLockerEdenExpansionPercent              = 5                                   {product}
     bool GCLockerInvokesConcurrent                 = false                               {product}
    uintx GCLogFileSize                             = 8192                                {product}
    uintx GCPauseIntervalMillis                     = 0                                   {product}
    uintx GCTaskTimeStampEntries                    = 200                                 {product}
    uintx GCTimeLimit                               = 98                                  {product}
    uintx GCTimeRatio                               = 99                                  {product}
    uintx HeapBaseMinAddress                        = 2147483648                          {pd product}
     bool HeapDumpAfterFullGC                       = false                               {manageable}
     bool HeapDumpBeforeFullGC                      = false                               {manageable}
     bool HeapDumpOnOutOfMemoryError                = false                               {manageable}
    ccstr HeapDumpPath                              =                                     {manageable}
    uintx HeapFirstMaximumCompactionCount           = 3                                   {product}
    uintx HeapMaximumCompactionInterval             = 20                                  {product}
    uintx HeapSizePerGCThread                       = 87241520                            {product}
     bool IgnoreEmptyClassPaths                     = false                               {product}
     bool IgnoreUnrecognizedVMOptions               = false                               {product}
    uintx IncreaseFirstTierCompileThresholdAt       = 50                                  {product}
     bool IncrementalInline                         = true                                {C2 product}
    uintx InitialBootClassLoaderMetaspaceSize       = 4194304                             {product}
    uintx InitialCodeCacheSize                      = 2555904                             {pd product}
    uintx InitialHeapSize                          := 134217728                           {product}
    uintx InitialRAMFraction                        = 64                                  {product}
    uintx InitialSurvivorRatio                      = 8                                   {product}
    uintx InitialTenuringThreshold                  = 7                                   {product}
    uintx InitiatingHeapOccupancyPercent            = 45                                  {product}
     bool Inline                                    = true                                {product}
    ccstr InlineDataFile                            =                                     {product}
     intx InlineSmallCode                           = 2000                                {pd product}
     bool InlineSynchronizedMethods                 = true                                {C1 product}
     bool InsertMemBarAfterArraycopy                = true                                {C2 product}
     intx InteriorEntryAlignment                    = 16                                  {C2 pd product}
     intx InterpreterProfilePercentage              = 33                                  {product}
     bool JNIDetachReleasesMonitors                 = true                                {product}
     bool JavaMonitorsInStackTrace                  = true                                {product}
     intx JavaPriority10_To_OSPriority              = -1                                  {product}
     intx JavaPriority1_To_OSPriority               = -1                                  {product}
     intx JavaPriority2_To_OSPriority               = -1                                  {product}
     intx JavaPriority3_To_OSPriority               = -1                                  {product}
     intx JavaPriority4_To_OSPriority               = -1                                  {product}
     intx JavaPriority5_To_OSPriority               = -1                                  {product}
     intx JavaPriority6_To_OSPriority               = -1                                  {product}
     intx JavaPriority7_To_OSPriority               = -1                                  {product}
     intx JavaPriority8_To_OSPriority               = -1                                  {product}
     intx JavaPriority9_To_OSPriority               = -1                                  {product}
     bool LIRFillDelaySlots                         = false                               {C1 pd product}
    uintx LargePageHeapSizeThreshold                = 134217728                           {product}
    uintx LargePageSizeInBytes                      = 0                                   {product}
     bool LazyBootClassLoader                       = true                                {product}
     intx LiveNodeCountInliningCutoff               = 40000                               {C2 product}
     bool LogCommercialFeatures                     = false                               {product}
     intx LoopMaxUnroll                             = 16                                  {C2 product}
     intx LoopOptsCount                             = 43                                  {C2 product}
     intx LoopUnrollLimit                           = 60                                  {C2 pd product}
     intx LoopUnrollMin                             = 4                                   {C2 product}
     bool LoopUnswitching                           = true                                {C2 product}
     bool ManagementServer                          = false                               {product}
    uintx MarkStackSize                             = 4194304                             {product}
    uintx MarkStackSizeMax                          = 536870912                           {product}
    uintx MarkSweepAlwaysCompactCount               = 4                                   {product}
    uintx MarkSweepDeadRatio                        = 1                                   {product}
     intx MaxBCEAEstimateLevel                      = 5                                   {product}
     intx MaxBCEAEstimateSize                       = 150                                 {product}
    uintx MaxDirectMemorySize                       = 0                                   {product}
     bool MaxFDLimit                                = true                                {product}
    uintx MaxGCMinorPauseMillis                     = 18446744073709551615                    {product}
    uintx MaxGCPauseMillis                          = 18446744073709551615                    {product}
    uintx MaxHeapFreeRatio                          = 100                                 {manageable}
    uintx MaxHeapSize                              := 2147483648                          {product}
     intx MaxInlineLevel                            = 9                                   {product}
     intx MaxInlineSize                             = 35                                  {product}
     intx MaxJNILocalCapacity                       = 65536                               {product}
     intx MaxJavaStackTraceDepth                    = 1024                                {product}
     intx MaxJumpTableSize                          = 65000                               {C2 product}
     intx MaxJumpTableSparseness                    = 5                                   {C2 product}
     intx MaxLabelRootDepth                         = 1100                                {C2 product}
     intx MaxLoopPad                                = 11                                  {C2 product}
    uintx MaxMetaspaceExpansion                     = 5451776                             {product}
    uintx MaxMetaspaceFreeRatio                     = 70                                  {product}
    uintx MaxMetaspaceSize                          = 18446744073709547520                    {product}
    uintx MaxNewSize                               := 715653120                           {product}
     intx MaxNodeLimit                              = 75000                               {C2 product}
 uint64_t MaxRAM                                    = 137438953472                        {pd product}
    uintx MaxRAMFraction                            = 4                                   {product}
     intx MaxRecursiveInlineLevel                   = 1                                   {product}
    uintx MaxTenuringThreshold                      = 15                                  {product}
     intx MaxTrivialSize                            = 6                                   {product}
     intx MaxVectorSize                             = 32                                  {C2 product}
    uintx MetaspaceSize                             = 21807104                            {pd product}
     bool MethodFlushing                            = true                                {product}
    uintx MinHeapDeltaBytes                        := 524288                              {product}
    uintx MinHeapFreeRatio                          = 0                                   {manageable}
     intx MinInliningThreshold                      = 250                                 {product}
     intx MinJumpTableSize                          = 10                                  {C2 pd product}
    uintx MinMetaspaceExpansion                     = 339968                              {product}
    uintx MinMetaspaceFreeRatio                     = 40                                  {product}
    uintx MinRAMFraction                            = 2                                   {product}
    uintx MinSurvivorRatio                          = 3                                   {product}
    uintx MinTLABSize                               = 2048                                {product}
     intx MonitorBound                              = 0                                   {product}
     bool MonitorInUseLists                         = false                               {product}
     intx MultiArrayExpandLimit                     = 6                                   {C2 product}
     bool MustCallLoadClassInternal                 = false                               {product}
    uintx NUMAChunkResizeWeight                     = 20                                  {product}
    uintx NUMAInterleaveGranularity                 = 2097152                             {product}
    uintx NUMAPageScanRate                          = 256                                 {product}
    uintx NUMASpaceResizeRate                       = 1073741824                          {product}
     bool NUMAStats                                 = false                               {product}
    ccstr NativeMemoryTracking                      = off                                 {product}
     bool NeedsDeoptSuspend                         = false                               {pd product}
     bool NeverActAsServerClassMachine              = false                               {pd product}
     bool NeverTenure                               = false                               {product}
    uintx NewRatio                                  = 2                                   {product}
    uintx NewSize                                  := 44564480                            {product}
    uintx NewSizeThreadIncrease                     = 5320                                {pd product}
     intx NmethodSweepActivity                      = 10                                  {product}
     intx NmethodSweepCheckInterval                 = 5                                   {product}
     intx NmethodSweepFraction                      = 16                                  {product}
     intx NodeLimitFudgeFactor                      = 2000                                {C2 product}
    uintx NumberOfGCLogFiles                        = 0                                   {product}
     intx NumberOfLoopInstrToAlign                  = 4                                   {C2 product}
     intx ObjectAlignmentInBytes                    = 8                                   {lp64_product}
    uintx OldPLABSize                               = 1024                                {product}
    uintx OldPLABWeight                             = 50                                  {product}
    uintx OldSize                                  := 89653248                            {product}
     bool OmitStackTraceInFastThrow                 = true                                {product}
ccstrlist OnError                                   =                                     {product}
ccstrlist OnOutOfMemoryError                        =                                     {product}
     intx OnStackReplacePercentage                  = 140                                 {pd product}
     bool OptimizeFill                              = true                                {C2 product}
     bool OptimizePtrCompare                        = true                                {C2 product}
     bool OptimizeStringConcat                      = true                                {C2 product}
     bool OptoBundling                              = false                               {C2 pd product}
     intx OptoLoopAlignment                         = 16                                  {pd product}
     bool OptoScheduling                            = false                               {C2 pd product}
    uintx PLABWeight                                = 75                                  {product}
     bool PSChunkLargeArrays                        = true                                {product}
     intx ParGCArrayScanChunk                       = 50                                  {product}
    uintx ParGCDesiredObjsFromOverflowList          = 20                                  {product}
     bool ParGCTrimOverflow                         = true                                {product}
     bool ParGCUseLocalOverflow                     = false                               {product}
    uintx ParallelGCBufferWastePct                  = 10                                  {product}
    uintx ParallelGCThreads                         = 4                                   {product}
     bool ParallelGCVerbose                         = false                               {product}
    uintx ParallelOldDeadWoodLimiterMean            = 50                                  {product}
    uintx ParallelOldDeadWoodLimiterStdDev          = 80                                  {product}
     bool ParallelRefProcBalancingEnabled           = true                                {product}
     bool ParallelRefProcEnabled                    = false                               {product}
     bool PartialPeelAtUnsignedTests                = true                                {C2 product}
     bool PartialPeelLoop                           = true                                {C2 product}
     intx PartialPeelNewPhiDelta                    = 0                                   {C2 product}
    uintx PausePadding                              = 1                                   {product}
     intx PerBytecodeRecompilationCutoff            = 200                                 {product}
     intx PerBytecodeTrapLimit                      = 4                                   {product}
     intx PerMethodRecompilationCutoff              = 400                                 {product}
     intx PerMethodTrapLimit                        = 100                                 {product}
     bool PerfAllowAtExitRegistration               = false                               {product}
     bool PerfBypassFileSystemCheck                 = false                               {product}
     intx PerfDataMemorySize                        = 32768                               {product}
     intx PerfDataSamplingInterval                  = 50                                  {product}
    ccstr PerfDataSaveFile                          =                                     {product}
     bool PerfDataSaveToFile                        = false                               {product}
     bool PerfDisableSharedMem                      = false                               {product}
     intx PerfMaxStringConstLength                  = 1024                                {product}
     intx PreInflateSpin                            = 10                                  {pd product}
     bool PreferInterpreterNativeStubs              = false                               {pd product}
     intx PrefetchCopyIntervalInBytes               = 576                                 {product}
     intx PrefetchFieldsAhead                       = 1                                   {product}
     intx PrefetchScanIntervalInBytes               = 576                                 {product}
     bool PreserveAllAnnotations                    = false                               {product}
     bool PreserveFramePointer                      = false                               {pd product}
    uintx PretenureSizeThreshold                    = 0                                   {product}
     bool PrintAdaptiveSizePolicy                   = false                               {product}
     bool PrintCMSInitiationStatistics              = false                               {product}
     intx PrintCMSStatistics                        = 0                                   {product}
     bool PrintClassHistogram                       = false                               {manageable}
     bool PrintClassHistogramAfterFullGC            = false                               {manageable}
     bool PrintClassHistogramBeforeFullGC           = false                               {manageable}
     bool PrintCodeCache                            = false                               {product}
     bool PrintCodeCacheOnCompilation               = false                               {product}
     bool PrintCommandLineFlags                     = false                               {product}
     bool PrintCompilation                          = false                               {product}
     bool PrintConcurrentLocks                      = false                               {manageable}
     intx PrintFLSCensus                            = 0                                   {product}
     intx PrintFLSStatistics                        = 0                                   {product}
     bool PrintFlagsFinal                          := true                                {product}
     bool PrintFlagsInitial                         = false                               {product}
     bool PrintGC                                   = false                               {manageable}
     bool PrintGCApplicationConcurrentTime          = false                               {product}
     bool PrintGCApplicationStoppedTime             = false                               {product}
     bool PrintGCCause                              = true                                {product}
     bool PrintGCDateStamps                         = false                               {manageable}
     bool PrintGCDetails                            = false                               {manageable}
     bool PrintGCID                                 = false                               {manageable}
     bool PrintGCTaskTimeStamps                     = false                               {product}
     bool PrintGCTimeStamps                         = false                               {manageable}
     bool PrintHeapAtGC                             = false                               {product rw}
     bool PrintHeapAtGCExtended                     = false                               {product rw}
     bool PrintHeapAtSIGBREAK                       = true                                {product}
     bool PrintJNIGCStalls                          = false                               {product}
     bool PrintJNIResolving                         = false                               {product}
     bool PrintOldPLAB                              = false                               {product}
     bool PrintOopAddress                           = false                               {product}
     bool PrintPLAB                                 = false                               {product}
     bool PrintParallelOldGCPhaseTimes              = false                               {product}
     bool PrintPromotionFailure                     = false                               {product}
     bool PrintReferenceGC                          = false                               {product}
     bool PrintSafepointStatistics                  = false                               {product}
     intx PrintSafepointStatisticsCount             = 300                                 {product}
     intx PrintSafepointStatisticsTimeout           = -1                                  {product}
     bool PrintSharedArchiveAndExit                 = false                               {product}
     bool PrintSharedDictionary                     = false                               {product}
     bool PrintSharedSpaces                         = false                               {product}
     bool PrintStringDeduplicationStatistics        = false                               {product}
     bool PrintStringTableStatistics                = false                               {product}
     bool PrintTLAB                                 = false                               {product}
     bool PrintTenuringDistribution                 = false                               {product}
     bool PrintTieredEvents                         = false                               {product}
     bool PrintVMOptions                            = false                               {product}
     bool PrintVMQWaitTime                          = false                               {product}
     bool PrintWarnings                             = true                                {product}
    uintx ProcessDistributionStride                 = 4                                   {product}
     bool ProfileInterpreter                        = true                                {pd product}
     bool ProfileIntervals                          = false                               {product}
     intx ProfileIntervalsTicks                     = 100                                 {product}
     intx ProfileMaturityPercentage                 = 20                                  {product}
     bool ProfileVM                                 = false                               {product}
     bool ProfilerPrintByteCodeStatistics           = false                               {product}
     bool ProfilerRecordPC                          = false                               {product}
    uintx PromotedPadding                           = 3                                   {product}
    uintx QueuedAllocationWarningCount              = 0                                   {product}
    uintx RTMRetryCount                             = 5                                   {ARCH product}
     bool RangeCheckElimination                     = true                                {product}
     intx ReadPrefetchInstr                         = 0                                   {ARCH product}
     bool ReassociateInvariants                     = true                                {C2 product}
     bool ReduceBulkZeroing                         = true                                {C2 product}
     bool ReduceFieldZeroing                        = true                                {C2 product}
     bool ReduceInitialCardMarks                    = true                                {C2 product}
     bool ReduceSignalUsage                         = false                               {product}
     intx RefDiscoveryPolicy                        = 0                                   {product}
     bool ReflectionWrapResolutionErrors            = true                                {product}
     bool RegisterFinalizersAtInit                  = true                                {product}
     bool RelaxAccessControlCheck                   = false                               {product}
    ccstr ReplayDataFile                            =                                     {product}
     bool RequireSharedSpaces                       = false                               {product}
    uintx ReservedCodeCacheSize                     = 251658240                           {pd product}
     bool ResizeOldPLAB                             = true                                {product}
     bool ResizePLAB                                = true                                {product}
     bool ResizeTLAB                                = true                                {pd product}
     bool RestoreMXCSROnJNICalls                    = false                               {product}
     bool RestrictContended                         = true                                {product}
     bool RewriteBytecodes                          = true                                {pd product}
     bool RewriteFrequentPairs                      = true                                {pd product}
     intx SafepointPollOffset                       = 256                                 {C1 pd product}
     intx SafepointSpinBeforeYield                  = 2000                                {product}
     bool SafepointTimeout                          = false                               {product}
     intx SafepointTimeoutDelay                     = 10000                               {product}
     bool ScavengeBeforeFullGC                      = true                                {product}
     intx SelfDestructTimer                         = 0                                   {product}
    uintx SharedBaseAddress                         = 34359738368                         {product}
    ccstr SharedClassListFile                       =                                     {product}
    uintx SharedMiscCodeSize                        = 122880                              {product}
    uintx SharedMiscDataSize                        = 4194304                             {product}
    uintx SharedReadOnlySize                        = 16777216                            {product}
    uintx SharedReadWriteSize                       = 16777216                            {product}
     bool ShowMessageBoxOnError                     = false                               {product}
     intx SoftRefLRUPolicyMSPerMB                   = 1000                                {product}
     bool SpecialEncodeISOArray                     = true                                {C2 product}
     bool SplitIfBlocks                             = true                                {C2 product}
     intx StackRedPages                             = 1                                   {pd product}
     intx StackShadowPages                          = 20                                  {pd product}
     bool StackTraceInThrowable                     = true                                {product}
     intx StackYellowPages                          = 2                                   {pd product}
     bool StartAttachListener                       = false                               {product}
     intx StarvationMonitorInterval                 = 200                                 {product}
     bool StressLdcRewrite                          = false                               {product}
    uintx StringDeduplicationAgeThreshold           = 3                                   {product}
    uintx StringTableSize                           = 60013                               {product}
     bool SuppressFatalErrorMessage                 = false                               {product}
    uintx SurvivorPadding                           = 3                                   {product}
    uintx SurvivorRatio                             = 8                                   {product}
     intx SuspendRetryCount                         = 50                                  {product}
     intx SuspendRetryDelay                         = 5                                   {product}
     intx SyncFlags                                 = 0                                   {product}
    ccstr SyncKnobs                                 =                                     {product}
     intx SyncVerbose                               = 0                                   {product}
    uintx TLABAllocationWeight                      = 35                                  {product}
    uintx TLABRefillWasteFraction                   = 64                                  {product}
    uintx TLABSize                                  = 0                                   {product}
     bool TLABStats                                 = true                                {product}
    uintx TLABWasteIncrement                        = 4                                   {product}
    uintx TLABWasteTargetPercent                    = 1                                   {product}
    uintx TargetPLABWastePct                        = 10                                  {product}
    uintx TargetSurvivorRatio                       = 50                                  {product}
    uintx TenuredGenerationSizeIncrement            = 20                                  {product}
    uintx TenuredGenerationSizeSupplement           = 80                                  {product}
    uintx TenuredGenerationSizeSupplementDecay      = 2                                   {product}
     intx ThreadPriorityPolicy                      = 0                                   {product}
     bool ThreadPriorityVerbose                     = false                               {product}
    uintx ThreadSafetyMargin                        = 52428800                            {product}
     intx ThreadStackSize                           = 1024                                {pd product}
    uintx ThresholdTolerance                        = 10                                  {product}
     intx Tier0BackedgeNotifyFreqLog                = 10                                  {product}
     intx Tier0InvokeNotifyFreqLog                  = 7                                   {product}
     intx Tier0ProfilingStartPercentage             = 200                                 {product}
     intx Tier23InlineeNotifyFreqLog                = 20                                  {product}
     intx Tier2BackEdgeThreshold                    = 0                                   {product}
     intx Tier2BackedgeNotifyFreqLog                = 14                                  {product}
     intx Tier2CompileThreshold                     = 0                                   {product}
     intx Tier2InvokeNotifyFreqLog                  = 11                                  {product}
     intx Tier3BackEdgeThreshold                    = 60000                               {product}
     intx Tier3BackedgeNotifyFreqLog                = 13                                  {product}
     intx Tier3CompileThreshold                     = 2000                                {product}
     intx Tier3DelayOff                             = 2                                   {product}
     intx Tier3DelayOn                              = 5                                   {product}
     intx Tier3InvocationThreshold                  = 200                                 {product}
     intx Tier3InvokeNotifyFreqLog                  = 10                                  {product}
     intx Tier3LoadFeedback                         = 5                                   {product}
     intx Tier3MinInvocationThreshold               = 100                                 {product}
     intx Tier4BackEdgeThreshold                    = 40000                               {product}
     intx Tier4CompileThreshold                     = 15000                               {product}
     intx Tier4InvocationThreshold                  = 5000                                {product}
     intx Tier4LoadFeedback                         = 3                                   {product}
     intx Tier4MinInvocationThreshold               = 600                                 {product}
     bool TieredCompilation                         = true                                {pd product}
     intx TieredCompileTaskTimeout                  = 50                                  {product}
     intx TieredRateUpdateMaxTime                   = 25                                  {product}
     intx TieredRateUpdateMinTime                   = 1                                   {product}
     intx TieredStopAtLevel                         = 4                                   {product}
     bool TimeLinearScan                            = false                               {C1 product}
     bool TraceBiasedLocking                        = false                               {product}
     bool TraceClassLoading                         = false                               {product rw}
     bool TraceClassLoadingPreorder                 = false                               {product}
     bool TraceClassPaths                           = false                               {product}
     bool TraceClassResolution                      = false                               {product}
     bool TraceClassUnloading                       = false                               {product rw}
     bool TraceDynamicGCThreads                     = false                               {product}
     bool TraceGen0Time                             = false                               {product}
     bool TraceGen1Time                             = false                               {product}
    ccstr TraceJVMTI                                =                                     {product}
     bool TraceLoaderConstraints                    = false                               {product rw}
     bool TraceMetadataHumongousAllocation          = false                               {product}
     bool TraceMonitorInflation                     = false                               {product}
     bool TraceParallelOldGCTasks                   = false                               {product}
     intx TraceRedefineClasses                      = 0                                   {product}
     bool TraceSafepointCleanupTime                 = false                               {product}
     bool TraceSharedLookupCache                    = false                               {product}
     bool TraceSuspendWaitFailures                  = false                               {product}
     intx TrackedInitializationLimit                = 50                                  {C2 product}
     bool TransmitErrorReport                       = false                               {product}
     bool TrapBasedNullChecks                       = false                               {pd product}
     bool TrapBasedRangeChecks                      = false                               {C2 pd product}
     intx TypeProfileArgsLimit                      = 2                                   {product}
    uintx TypeProfileLevel                          = 111                                 {pd product}
     intx TypeProfileMajorReceiverPercent           = 90                                  {C2 product}
     intx TypeProfileParmsLimit                     = 2                                   {product}
     intx TypeProfileWidth                          = 2                                   {product}
     intx UnguardOnExecutionViolation               = 0                                   {product}
     bool UnlinkSymbolsALot                         = false                               {product}
     bool Use486InstrsOnly                          = false                               {ARCH product}
     bool UseAES                                    = true                                {product}
     bool UseAESIntrinsics                          = true                                {product}
     intx UseAVX                                    = 2                                   {ARCH product}
     bool UseAdaptiveGCBoundary                     = false                               {product}
     bool UseAdaptiveGenerationSizePolicyAtMajorCollection  = true                                {product}
     bool UseAdaptiveGenerationSizePolicyAtMinorCollection  = true                                {product}
     bool UseAdaptiveNUMAChunkSizing                = true                                {product}
     bool UseAdaptiveSizeDecayMajorGCCost           = true                                {product}
     bool UseAdaptiveSizePolicy                     = true                                {product}
     bool UseAdaptiveSizePolicyFootprintGoal        = true                                {product}
     bool UseAdaptiveSizePolicyWithSystemGC         = false                               {product}
     bool UseAddressNop                             = true                                {ARCH product}
     bool UseAltSigs                                = false                               {product}
     bool UseAutoGCSelectPolicy                     = false                               {product}
     bool UseBMI1Instructions                       = true                                {ARCH product}
     bool UseBMI2Instructions                       = true                                {ARCH product}
     bool UseBiasedLocking                          = true                                {product}
     bool UseBimorphicInlining                      = true                                {C2 product}
     bool UseBoundThreads                           = true                                {product}
     bool UseBsdPosixThreadCPUClocks                = true                                {product}
     bool UseCLMUL                                  = true                                {ARCH product}
     bool UseCMSBestFit                             = true                                {product}
     bool UseCMSCollectionPassing                   = true                                {product}
     bool UseCMSCompactAtFullCollection             = true                                {product}
     bool UseCMSInitiatingOccupancyOnly             = false                               {product}
     bool UseCRC32Intrinsics                        = true                                {product}
     bool UseCodeCacheFlushing                      = true                                {product}
     bool UseCompiler                               = true                                {product}
     bool UseCompilerSafepoints                     = true                                {product}
     bool UseCompressedClassPointers               := true                                {lp64_product}
     bool UseCompressedOops                        := true                                {lp64_product}
     bool UseConcMarkSweepGC                        = false                               {product}
     bool UseCondCardMark                           = false                               {C2 product}
     bool UseCountLeadingZerosInstruction           = true                                {ARCH product}
     bool UseCountTrailingZerosInstruction          = true                                {ARCH product}
     bool UseCountedLoopSafepoints                  = false                               {C2 product}
     bool UseCounterDecay                           = true                                {product}
     bool UseDivMod                                 = true                                {C2 product}
     bool UseDynamicNumberOfGCThreads               = false                               {product}
     bool UseFPUForSpilling                         = true                                {C2 product}
     bool UseFastAccessorMethods                    = false                               {product}
     bool UseFastEmptyMethods                       = false                               {product}
     bool UseFastJNIAccessors                       = true                                {product}
     bool UseFastStosb                              = true                                {ARCH product}
     bool UseG1GC                                   = false                               {product}
     bool UseGCLogFileRotation                      = false                               {product}
     bool UseGCOverheadLimit                        = true                                {product}
     bool UseGCTaskAffinity                         = false                               {product}
     bool UseHeavyMonitors                          = false                               {product}
     bool UseHugeTLBFS                              = false                               {product}
     bool UseInlineCaches                           = true                                {product}
     bool UseInterpreter                            = true                                {product}
     bool UseJumpTables                             = true                                {C2 product}
     bool UseLWPSynchronization                     = true                                {product}
     bool UseLargePages                             = false                               {pd product}
     bool UseLargePagesInMetaspace                  = false                               {product}
     bool UseLargePagesIndividualAllocation         = false                               {pd product}
     bool UseLockedTracing                          = false                               {product}
     bool UseLoopCounter                            = true                                {product}
     bool UseLoopInvariantCodeMotion                = true                                {C1 product}
     bool UseLoopPredicate                          = true                                {C2 product}
     bool UseMathExactIntrinsics                    = true                                {C2 product}
     bool UseMaximumCompactionOnSystemGC            = true                                {product}
     bool UseMembar                                 = true                                {pd product}
     bool UseMontgomeryMultiplyIntrinsic            = true                                {C2 product}
     bool UseMontgomerySquareIntrinsic              = true                                {C2 product}
     bool UseMulAddIntrinsic                        = true                                {C2 product}
     bool UseMultiplyToLenIntrinsic                 = true                                {C2 product}
     bool UseNUMA                                   = false                               {product}
     bool UseNUMAInterleaving                       = false                               {product}
     bool UseNewLongLShift                          = false                               {ARCH product}
     bool UseOSErrorReporting                       = false                               {pd product}
     bool UseOldInlining                            = true                                {C2 product}
     bool UseOnStackReplacement                     = true                                {pd product}
     bool UseOnlyInlinedBimorphic                   = true                                {C2 product}
     bool UseOprofile                               = false                               {product}
     bool UseOptoBiasInlining                       = true                                {C2 product}
     bool UsePSAdaptiveSurvivorSizePolicy           = true                                {product}
     bool UseParNewGC                               = false                               {product}
     bool UseParallelGC                            := true                                {product}
     bool UseParallelOldGC                          = true                                {product}
     bool UsePerfData                               = true                                {product}
     bool UsePopCountInstruction                    = true                                {product}
     bool UseRDPCForConstantTableBase               = false                               {C2 product}
     bool UseRTMDeopt                               = false                               {ARCH product}
     bool UseRTMLocking                             = false                               {ARCH product}
     bool UseSHA                                    = false                               {product}
     bool UseSHA1Intrinsics                         = false                               {product}
     bool UseSHA256Intrinsics                       = false                               {product}
     bool UseSHA512Intrinsics                       = false                               {product}
     bool UseSHM                                    = false                               {product}
     intx UseSSE                                    = 4                                   {product}
     bool UseSSE42Intrinsics                        = true                                {product}
     bool UseSerialGC                               = false                               {product}
     bool UseSharedSpaces                           = false                               {product}
     bool UseSignalChaining                         = true                                {product}
     bool UseSquareToLenIntrinsic                   = true                                {C2 product}
     bool UseStoreImmI16                            = false                               {ARCH product}
     bool UseStringDeduplication                    = false                               {product}
     bool UseSuperWord                              = true                                {C2 product}
     bool UseTLAB                                   = true                                {pd product}
     bool UseThreadPriorities                       = true                                {pd product}
     bool UseTypeProfile                            = true                                {product}
     bool UseTypeSpeculation                        = true                                {C2 product}
     bool UseUnalignedLoadStores                    = true                                {ARCH product}
     bool UseVMInterruptibleIO                      = false                               {product}
     bool UseXMMForArrayCopy                        = true                                {product}
     bool UseXmmI2D                                 = false                               {ARCH product}
     bool UseXmmI2F                                 = false                               {ARCH product}
     bool UseXmmLoadAndClearUpper                   = true                                {ARCH product}
     bool UseXmmRegToRegMoveAll                     = true                                {ARCH product}
     bool VMThreadHintNoPreempt                     = false                               {product}
     intx VMThreadPriority                          = -1                                  {product}
     intx VMThreadStackSize                         = 1024                                {pd product}
     intx ValueMapInitialSize                       = 11                                  {C1 product}
     intx ValueMapMaxLoopSize                       = 8                                   {C1 product}
     intx ValueSearchLimit                          = 1000                                {C2 product}
     bool VerifyMergedCPBytecodes                   = true                                {product}
     bool VerifySharedSpaces                        = false                               {product}
     intx WorkAroundNPTLTimedWaitHang               = 1                                   {product}
    uintx YoungGenerationSizeIncrement              = 20                                  {product}
    uintx YoungGenerationSizeSupplement             = 80                                  {product}
    uintx YoungGenerationSizeSupplementDecay        = 8                                   {product}
    uintx YoungPLABSize                             = 4096                                {product}
     bool ZeroTLAB                                  = false                               {product}
     intx hashCode                                  = 5                                   {product}
PS Scavenge
PS MarkSweep
zhenglubiaodeMacBook-Pro:six zlb$
```
- Parallel Scavenge收集器架构中本身有PS MarkSweep收集器来进行老年代收集，并非直接使用了Serial Old收集器，但是这个PS MarkSweep收集器与Serial Old的实现非常接近，所以在官方的许多资料中都是直接以Serial Old代替PS MarkSweep进行讲解，这里笔者也采用这种方式。
-  -XX:+PrintCommandLineFlags。这个参数让JVM打印出那些已经被用户或者JVM设置过的详细的XX参数的名称和值。换句话说，它列举出 -XX:+PrintFlagsFinal的结果中第三列有":="的参数。以这种方式，我们可以用-XX:+PrintCommandLineFlags作为快捷方式来查看修改过的参数
  