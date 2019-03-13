package oom;

/**
 * @author Yuanming Tao
 * Created on 2019/3/13
 * Description 通过不断地建立线程的方式倒是可以产生内存溢出异常
 * VM Args：-Xss2M （这时候不妨设大些）
 */
public class JavaVMStackOOM {

    private void dontStop() {
        while (true) {
        }
    }

    /**
     * 但是这样产生的内存溢出异常与栈空间是否足够大并不存在任何联系，或者准确地说，在这种情况下，为每个线程的栈分配的内存越大，反而越容易产生内存溢出异常。
     * 每个线程分配到的栈容量越大，可以建立的线程数量自然就越少，建立线程时就越容易把剩下的内存耗尽。
     *
     * 如果使用虚拟机默认参数，栈深度在大多数情况下（因为每个方法压入栈的帧大小并不是一样的，所以只能说在大多数情况下）达到1000~2000完全没有问题，对于正常的方法调用（包括递归），这个深度应该完全够用了
     * 但是，如果是建立过多线程导致的内存溢出，在不能减少线程数或者更换64位虚拟机的情况下，就只能通过减少最大堆和减少栈容量来换取更多的线程。如果没有这方面的处理经验，这种通过“减少内存”的手段来解决内存溢出的方式会比较难以想到。
     *
     * 代码执行时有较大的风险，可能会导致操作系统假死。
     */
    public void stackLeakByThread() {
//        while (true) {
//            Thread thread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    dontStop();
//                }
//            });
//            thread.start();
//        }
    }

    public static void main(String[] args) throws Throwable {
        JavaVMStackOOM oom = new JavaVMStackOOM();
        oom.stackLeakByThread();
    }
}

