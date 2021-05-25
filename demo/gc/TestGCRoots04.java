package gc;


/**
 *
 * 这个日志的含义是整个GC过程中STW的时间，配置了 -XX:+PrintGCApplicationStoppedTime 这个参数才会打印这个信息
 *
 * -Xms1024m -Xmx1024m -Xmn512m -XX:+PrintGCDetails -XX:+PrintGCApplicationStoppedTime -XX:+PrintCommandLineFlags
 */
/**
 * appledeiMac:demo apple$ java -Xms1024m -Xmx1024m -Xmn512m -XX:+PrintGCDetails -XX:+PrintGCApplicationStoppedTime -XX:+PrintCommandLineFlags gc/TestGCRoots04
 * -XX:InitialHeapSize=1073741824 -XX:MaxHeapSize=1073741824 -XX:MaxNewSize=536870912 -XX:NewSize=536870912 -XX:+PrintCommandLineFlags -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCDetails -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseParallelGC
 * [GC (System.gc()) [PSYoungGen: 138608K->400K(458752K)] 138608K->408K(983040K), 0.0021046 secs] [Times: user=0.01 sys=0.00, real=0.00 secs]
 * [Full GC (System.gc()) [PSYoungGen: 400K->0K(458752K)] [ParOldGen: 8K->277K(524288K)] 408K->277K(983040K), [Metaspace: 2666K->2666K(1056768K)], 0.0052298 secs] [Times: user=0.00 sys=0.01, real=0.01 secs]
 * Total time for which application threads were stopped: 0.0075703 seconds, Stopping threads took: 0.0000177 seconds
 * Heap
 *  PSYoungGen      total 458752K, used 3932K [0x00000007a0000000, 0x00000007c0000000, 0x00000007c0000000)
 *   eden space 393216K, 1% used [0x00000007a0000000,0x00000007a03d7218,0x00000007b8000000)
 *   from space 65536K, 0% used [0x00000007b8000000,0x00000007b8000000,0x00000007bc000000)
 *   to   space 65536K, 0% used [0x00000007bc000000,0x00000007bc000000,0x00000007c0000000)
 *  ParOldGen       total 524288K, used 277K [0x0000000780000000, 0x00000007a0000000, 0x00000007a0000000)
 *   object space 524288K, 0% used [0x0000000780000000,0x0000000780045720,0x00000007a0000000)
 *  Metaspace       used 2673K, capacity 4486K, committed 4864K, reserved 1056768K
 *   class space    used 288K, capacity 386K, committed 512K, reserved 1048576K
 * appledeiMac:demo apple$
 */
public class TestGCRoots04 {
    private static int _10MB = 10 * 1024 * 1024;
    private TestGCRoots04 t;
    private byte[] memory;

    public TestGCRoots04(int size) {
        memory = new byte[size];
    }

    public static void main(String[] args) {
        TestGCRoots04 t4 = new TestGCRoots04(4 * _10MB);
        t4.t = new TestGCRoots04(8 * _10MB);
        t4 = null;
        System.gc();
    }

}

