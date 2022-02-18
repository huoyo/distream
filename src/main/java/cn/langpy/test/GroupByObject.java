package cn.langpy.test;

import cn.langpy.core.ListFrame;
import cn.langpy.core.MapFrame;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupByObject {
    public static List<BaseInfo> getBaseInfo() {
        List<BaseInfo> list = new ArrayList<BaseInfo>(){
            {
                add(new BaseInfo(2,"李四","语文",90.0));
                add(new BaseInfo(2,"李四","数学",80.0));
                add(new BaseInfo(2,"李四","数学",80.0));
                add(new BaseInfo(3,"王五","语文--",70.4599999));
            }
        };
        return list;
    }
    public static void main(String[] args) {
        ListFrame<BaseInfo> baseInfos = ListFrame.fromList(getBaseInfo());
        MapFrame<Object, ListFrame> ids = baseInfos.groupBy("name");
        Map<Object, Integer> count = ids.count();
        System.out.println(count);
        System.out.println(ids.avg("score"));
        MapFrame<Object, MapFrame<Object, ListFrame>> objectMapFrameMapFrame = baseInfos.groupBy("name").groupBy("subject");
        Map<Object, Object> count1 = objectMapFrameMapFrame.count();
        Map<Object, Object> sum = objectMapFrameMapFrame.sum("score");
        Map<Object, Object> avg = objectMapFrameMapFrame.avg("score");
        System.out.println(count1);
        System.out.println(sum);
        System.out.println(avg);
    }
}

