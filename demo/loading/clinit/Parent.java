package loading.clinit;

/**
 * @author Yuanming Tao
 * Created on 2019/3/17
 * Description
 */
public class Parent {
    public static int A = 1;
    static {
        A = 2;
    }
}
