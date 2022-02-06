package cn.langpy.test;

import cn.langpy.core.ListFrame;
import cn.langpy.core.MapFrame;

import java.util.Map;

/**
 * read data from txt/csv
 */
public class ReadString {
    public static void main(String[] args) {
        String path = "src/main/java/cn/langpy/test/test.txt";
        ListFrame<String> lines = ListFrame.readString(path);
        for (String line : lines) {
            System.out.println(line);
        }

    }
}
