package cn.langpy.core;

import cn.langpy.constant.Functions;
import cn.langpy.model.OperateMap;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class FunctionCenter {

    public static Map<Functions, BiFunction<Object, OperateMap, Object>> planConsumer;

    static {
        planConsumer = new HashMap<>();
        planConsumer.put(Functions.REPLACE, (value, operateMap) -> replace(value, operateMap));
        planConsumer.put(Functions.SUBSTRING, (value, operateMap) -> substring(value, operateMap));
        planConsumer.put(Functions.FORMAT, (value, operateMap) -> format(value, operateMap));
    }


    private static Object replace(Object value, OperateMap operateMap) {
        String src = operateMap.getParams().get(1).toString();
        String tar = operateMap.getParams().get(2).toString();
        return value.toString().replace(src, tar);
    }

    private static Object substring(Object value, OperateMap operateMap) {
        int subStart = (int) operateMap.getParams().get(1);
        int subEnd = (int) operateMap.getParams().get(2);
        return value.toString().substring(subStart, subEnd);
    }

    private static Object format(Object value, OperateMap operateMap) {
        int d = (int) operateMap.getParams().get(1);
        BigDecimal bigDecimal = new BigDecimal((double) value);
        double doubleValue = bigDecimal.setScale(d, BigDecimal.ROUND_HALF_UP).doubleValue();
        return doubleValue;
    }
}
