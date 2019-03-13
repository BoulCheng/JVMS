package oom;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yuanming Tao
 * Created on 2019/3/13
 * Description
 *
 * VM Args：(jdk7 -XX:PermSize=10M -XX:MaxPermSize=10M)  jdk8 -Xms10m -Xmx10m
 */
public class RuntimeConstantPoolOOM {

    /**
     * Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
     * 	at java.lang.Integer.toString(Integer.java:403)
     * 	at java.lang.String.valueOf(String.java:3099)
     * 	at oom.RuntimeConstantPoolOOM.main(RuntimeConstantPoolOOM.java:21)
     * @param args
     */
    public static void main(String[] args) {
        // 使用List保持着常量池引用，避免Full GC回收常量池行为
        List<String> list = new ArrayList<String>();
        // 10MB的PermSize在integer范围内足够产生OOM了
        int i = 0;
        while (true) {
            list.add(String.valueOf(i++).intern());
        }
    }
}

