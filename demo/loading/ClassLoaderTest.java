package loading;

import java.io.IOException;
import java.io.InputStream;
import java.security.ProtectionDomain;

/**
 * @author Yuanming Tao
 * Created on 2019/3/9
 * Description
 */
public class ClassLoaderTest {

//    todo  1. classpath 2. rt.jar同全限定名类  4.不同类加载器加载不同jar包 5 jar包


    /**
     * 虚拟机中存在了两个ClassLoaderTest类，一个是由系统应用程序类加载器加载的，另外一个是由我们自定义的类加载器加载的，虽然都来自同一个Class文件，但依然是两个独立的类，做对象所属类型检查时结果自然为false。
     * @param args
     * @throws Exception
     * {@link ClassLoader#preDefineClass(String, ProtectionDomain)}
     */
    public static void main(String[] args) throws Exception {

        ClassLoader myLoader = new ClassLoader() {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                try {
                    String fileName = name.substring(name.lastIndexOf(".") + 1) + ".class";
                    InputStream is = getClass().getResourceAsStream(fileName);
                    if (is == null) {
                        throw new ClassNotFoundException();
//                        return super.loadClass(name);
                    }
                    byte[] b = new byte[is.available()];
                    is.read(b);
                    return defineClass(name, b, 0, b.length);
                } catch (IOException e) {
                    throw new ClassNotFoundException(name);
                }
            }
        };


        Class stringClass1 = myLoader.loadClass("java.lang.String");
        System.out.println(stringClass1.newInstance() instanceof String);


        ClassLoader myLoader2 = new ClassLoader() {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                return super.loadClass(name);
            }
        };
        Class stringClass = myLoader2.loadClass("java.lang.String");
        System.out.println(stringClass.newInstance() instanceof String);

        Object obj = myLoader.loadClass("loading.ClassLoaderTest").newInstance();

        System.out.println(obj.getClass());
        //两个ClassLoaderTest类由不同的类加载器加载 虽然是同一个Class文件，但是不同的两个类
        System.out.println(obj instanceof loading.ClassLoaderTest);

        ClassLoaderTest classLoaderTest = new ClassLoaderTest();
        System.out.println(classLoaderTest instanceof loading.ClassLoaderTest);


        Object short2 = myLoader.loadClass("loading.Short").newInstance();
        System.out.println(short2 instanceof loading.Short);
        Short short3 = new Short();
        System.out.println(short3 instanceof loading.Short);

    }
}


