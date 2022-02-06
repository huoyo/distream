package cn.langpy.test;

import cn.langpy.core.ListFrame;

import java.util.Map;

/**
 * read data from txt/csv
 */
public class MapToObject {
    public static void main(String[] args) {
        String path = "src/main/java/cn/langpy/test/test2.txt";
        ListFrame<Map<String, Object>> lines = ListFrame.readMap(path,",",new Class[]{Integer.class,String.class,Integer.class,Double.class});
        ListFrame<TestObject> a = lines.toObject(TestObject.class);
        a.handle("name=substring(name,0,2);age=substring(age,0,2)");
        System.out.println(a);
    }
}
