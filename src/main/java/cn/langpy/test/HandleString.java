package cn.langpy.test;

import cn.langpy.core.DataHandler;
import cn.langpy.core.ListFrame;

/**
 * read data from txt/csv
 */
public class HandleString {
    public static void main(String[] args) {
        String path = "src/main/java/cn/langpy/test/test.txt";
        ListFrame<String> lines = ListFrame.readString(path);
        lines = lines
                .handle(line -> line + ";")//add ";" at the end of every line
                .handle(new StringHandler());//add "==" at the front of every line by a data handler

        for (String line : lines) {
            System.out.println(line);
        }

    }
}

class StringHandler implements DataHandler<String> {
    @Override
    public String handle(String line) {
        return "==" + line;
    }
}
