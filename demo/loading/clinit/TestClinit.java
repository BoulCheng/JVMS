package loading.clinit;

/**
 * @author Yuanming Tao
 * Created on 2019/3/17
 * Description
 */
public class TestClinit {

    static int j;
    static {
        i = 0;  //  给变量复制可以正常编译通过
        //System.out.print(i);  // 这句编译器会提示“非法向前引用”

        //System.out.println(j);

    }
    static int i = 1;


    public static void main(String[] args) {
        System.out.println(Sub.B);
    }



    static class Parent {
        public static int A = 1;
        static {
            A = 2;
        }
    }

    static class Sub extends Parent {

        public static int B = A;

        static {
            A = 3;
        }
    }

}


