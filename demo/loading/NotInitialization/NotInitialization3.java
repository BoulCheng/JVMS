package loading.NotInitialization;

/**
 * @author Yuanming Tao
 * Created on 2019/3/14
 * Description
 */
/**
 * 非主动使用类字段演示
 **/
public class NotInitialization3 {

    public static void main(String[] args) {
        System.out.println(ConstClass.HELLOWORLD);
    }
}