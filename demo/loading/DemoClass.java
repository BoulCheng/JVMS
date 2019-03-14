package loading;

/**
 * @author Yuanming Tao
 * Created on 2019/3/9
 * Description
 */
public class DemoClass {

    public static void main(String[] args) {



        //1 Determines if the class or interface represented by this Class object is either the same as,
        // or is a superclass or superinterface of, the class or interface represented by the specified Class parameter. It returns true if so;
        Father father = new Father();
        boolean b;
        b = father.getClass().isAssignableFrom(GrandFather.class);
        System.out.println(b);

        b = father.getClass().isAssignableFrom(Son.class);
        System.out.println(b);

        b = father.getClass().isAssignableFrom(Father.class);
        System.out.println(b);


        //只能向上类型转化 而不能向下
        GrandFather grandFather = (GrandFather) father;
        //Exception in thread "main" java.lang.ClassCastException: com.zlb.lang.DemoClass$Father cannot be cast to com.zlb.lang.DemoClass$Son
//        Son son = (Son) father;


        //2 If this Class object represents a primitive type, this method returns true if the specified Class parameter is exactly this Class object;
        System.out.println("===");
        Long i = 1L;
        boolean bb = i.getClass().isAssignableFrom(Integer.class);
        System.out.println(bb);
        bb = i.getClass().isAssignableFrom(Long.class);
        System.out.println(bb);



        //3 This method is the dynamic equivalent of the Java language instanceof operator.
        // The method returns true if the specified Object argument is non-null and can be cast to the reference type
        // represented by this Class object without raising a ClassCastException. It returns false otherwise.
        System.out.println("===");

        b = father.getClass().isInstance(new GrandFather());
        System.out.println(b);

        b = father.getClass().isInstance(new Son());
        System.out.println(b);

        b = father.getClass().isInstance(new Father());
        System.out.println(b);



    }


    private static class GrandFather {
    }

    private static class Father extends GrandFather {
    }


    private static class Son extends Father {
    }
}


