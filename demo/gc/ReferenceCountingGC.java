package gc;

/**
 * @author Yuanming Tao
 * Created on 2019/3/28
 * -XX:+PrintGCDetails -Xloggc:/Users/zlb/IdeaProjects/lb/JVM-S/gcLogs/ReferenceCountingGC.log -XX:+PrintGCDateStamps
 */
/**
 * testGC()方法执行后，objA和objB会不会被GC呢？
 * 会 虚拟机并不是通过引用计数算法来判断对象是否存活的
 */
public class ReferenceCountingGC {

    public Object instance = null;

    private static final int _1MB = 1024 * 1024;

    /**
     * 这个成员属性的唯一意义就是占点内存，以便在能在GC日志中看清楚是否有回收过
     */
    private byte[] bigSize = new byte[2 * _1MB];

    public static void testGC() {
        ReferenceCountingGC objA = new ReferenceCountingGC();
        ReferenceCountingGC objB = new ReferenceCountingGC();
        objA.instance = objB;
        objB.instance = objA;

        objA = null;
        objB = null;

        // 假设在这行发生GC，objA和objB是否能被回收？
        System.gc();
    }

    public static void main(String[] args) {
        testGC();
    }
}

