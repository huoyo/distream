package cn.langpy.test;

import cn.langpy.core.ListFrame;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HandleObject {
    public static List<BaseInfo> getBaseInfo() {
        List<BaseInfo> list = new ArrayList<BaseInfo>(){
            {
                add(new BaseInfo(1,"李四","语文",90.0));
                add(new BaseInfo(2,"李四","语文==",80.0));
                add(new BaseInfo(3,"王五","语文--",70.4599999));
            }
        };
        return list;
    }
    public static void main(String[] args) {
        ListFrame<BaseInfo> baseInfos = ListFrame.fromList(getBaseInfo());
//        baseInfos.replace("name","张三","9999");
//        ListFrame<Double> scores = baseInfos.get(BaseInfo::getScore);
//        baseInfos.handle("score=format(score,2)+10");
//        baseInfos.handle("name=replace(name,'李四','8888')");
        baseInfos.handle(a->true,"name='修改后'");
        System.out.println(baseInfos);
    }
}

