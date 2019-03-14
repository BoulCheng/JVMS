package loading.NotInitialization;

/**
 * @author Yuanming Tao
 * Created on 2019/3/14
 * Description
 */
public class SubClass extends SuperClass {

    static {
        System.out.println("SubClass init!");
    }
}