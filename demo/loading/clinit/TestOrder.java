package loading.clinit;

/**
 * @author Yuanming Tao
 * Created on 2019/3/17
 * Description
 */
public class TestOrder {


    static class Father {

        Father() {
            System.out.println(System.currentTimeMillis() + " ------ 父类构造函数");
        }

        static {
            System.out.println(System.currentTimeMillis() + " ------ 父类静态代码块");
        }

        long x = getTime(" ------ 父类实例变量");

        {
            long time = System.currentTimeMillis();
            System.out.println(time + " ------ 父类代码块");
        }

        static long y = getTime(" ------ 父类静态变量");

        static long getTime(String who) {
            long time = System.currentTimeMillis();
            System.out.println(time + who);
            return time;
        }
    }

    static class Child extends Father {

        Child() {
            System.out.println(System.currentTimeMillis() + " ------ 子类构造函数");
        }

        static long y = getTime(" ------ 子类静态变量");

        static {
            System.out.println(System.currentTimeMillis() + " ------ 子类静态代码块");
        }

        {
            long time = System.currentTimeMillis();
            System.out.println(time + " ------ 子类代码块");
        }

        long x = getTime(" ------ 子类实例变量");

        static long getTime(String who) {
            long time = System.currentTimeMillis();
            System.out.println(time + who);
            return time;
        }
    }


    public static void main(String[] args) {
        new Child();
        System.out.println("分隔符 ------ 分隔符");
        new Child();
    }

}




