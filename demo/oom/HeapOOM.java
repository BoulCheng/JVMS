package oom;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yuanming Tao
 * Created on 2019/3/3
 * Description VM Args：-Xms20m -Xmx20m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/Users/zlb/Downloads/mat.app/Contents/MacOS/dump
 */
public class HeapOOM {

    static class OOMObject {
    }

    public static void main(String[] args) {
        List<OOMObject> list = new ArrayList<OOMObject>();

        while (true) {
            list.add(new OOMObject());
        }
    }
}

