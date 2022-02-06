package cn.langpy.test;

import cn.langpy.core.DataHandler;
import cn.langpy.core.ListFrame;
import cn.langpy.core.MapFrame;

import java.util.Map;

/**
 * read data from txt/csv
 */
public class HandleMap {
    public static void main(String[] args) {
        String path = "src/main/java/cn/langpy/test/test.txt";
        /*read easily*/
//        ListFrame<Map<String, Object>> lines = ListFrame.readMap(path);
        /*read by split symbol*/
//        ListFrame<Map<String, Object>> lines = ListFrame.readMap(path,",");
        /*define data types*/
        ListFrame<Map<String, Object>> lines = ListFrame.readMap(path,",",new Class[]{Integer.class,String.class,Integer.class,Double.class});
        lines = lines
                .handle("收入=收入*0.8")
                .handle("序号='0'+序号;姓名=序号+姓名")//add "0" at the front of 序号;rename 姓名 by 序号+姓名
                .handle(new MapHandler());//add a key named "newKey" whose value is 1

        for (Map<String, Object> line : lines) {
            System.out.println(line);
        }
        /*obtain data by column name*/
        ListFrame<Double> indexs = lines.get("收入");
        double maxIncome = indexs.max();
        double minIncome = indexs.min();
        double avgIncome = indexs.avg();

        MapFrame<Object, ListFrame> agesGroup = lines.groupBy("年龄");
        Map<Object, Integer> count = agesGroup.count();
        Map<Object, Double> incomeAvg = agesGroup.avg("收入");
        Map<Object, Double> incomeSum = agesGroup.sum("收入");
        Map<Object, ListFrame> incomeConcat = agesGroup.concat("收入");

        /*save to file*/
        lines.toFile("src/main/java/cn/langpy/test/save.txt");

    }
}

class MapHandler implements DataHandler<Map<String, Object>> {
    @Override
    public Map<String, Object> handle(Map<String, Object> line) {
        line.put("newKey",1);
        return line;
    }
}
