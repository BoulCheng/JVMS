package oom;

/**
 * @author Yuanming Tao
 * Created on 2019/3/13 The stack size specified is too small, Specify at least 160k
 * Description 在单个线程下，无论是由于栈帧太大还是虚拟机栈容量太小，当内存无法分配的时候，虚拟机抛出的都是StackOverflowError异常
 * VM Args：-Xss160k
 */
public class JavaVMStackSOF {

    private int stackLength = 1;

    public void stackLeak() {
        stackLength++;
        stackLeak();
    }

    public void stackLeak2() {
        stackLength++;
        int i = 1;
        int i1 = 1;
        int i2 = 2;
        int i3 = 3;
        int i4 = 4;
        int i5 = 5;
        int i6 = 6;
        int i7 = 7;
        int i8 = 8;
        int i9 = 9;
        int i10 = 10;
        int i111 = 1;
        int i11 = 1;
        int i12 = 2;
        int i13 = 3;
        int i14 = 4;
        int i15 = 5;
        int i16 = 6;
        int i17 = 7;
        int i18 = 8;
        int i19 = 9;
        int i110 = 10;
        stackLeak2();
    }

    /**
     * 在单个线程下，无论是由于栈帧太大还是虚拟机栈容量太小，当内存无法分配的时候，虚拟机抛出的都是StackOverflowError异常
     * @param args
     * @throws Throwable
     */
    public static void main(String[] args) throws Throwable {
        JavaVMStackSOF oom = new JavaVMStackSOF();
        try {
//            1. 使用-Xss参数减少栈内存容量。结果：抛出StackOverflowError异常，异常出现时输出的堆栈深度相应缩小
            oom.stackLeak();


//            2.定义了大量的本地变量，增大此方法帧中本地变量表的长度。结果：抛出StackOverflowError异常时输出的堆栈深度相应缩小
//            oom.stackLeak2();

        } catch (Throwable e) {
            System.out.println("stack length:" + oom.stackLength);
            throw e;
        }
    }
}


