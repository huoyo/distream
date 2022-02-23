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
        planConsumer.put(Functions.INT, (value, operateMap) -> toInt(value, operateMap));
        planConsumer.put(Functions.DOUBLE, (value, operateMap) -> toDouble(value, operateMap));
        planConsumer.put(Functions.STRING, (value, operateMap) -> toString(value, operateMap));
        planConsumer.put(Functions.INDEX, (value, operateMap) -> index(value, operateMap));
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
        if (value==null) {
            return null;
        }

        double doubleValue = (double) value;
        if (Double.isNaN(doubleValue)) {
            return 0.0;
        }
        if (doubleValue==0.0) {
            return doubleValue;
        }
        int d = (int) operateMap.getParams().get(1);
        BigDecimal bigDecimal = new BigDecimal(doubleValue);
        doubleValue = bigDecimal.setScale(d, BigDecimal.ROUND_HALF_UP).doubleValue();
        return doubleValue;
    }


    private static Object toInt(Object value, OperateMap operateMap) {
        if (value==null) {
            throw new RuntimeException("can not convert null to int!");
        }
        if (value.toString().matches("[0-9]+\\.[0-9]+")) {
            return Integer.valueOf(value.toString().split("\\.")[0]);
        }
        return Integer.valueOf(value.toString());
    }
    private static Object toDouble(Object value, OperateMap operateMap) {
        if (value==null) {
            throw new RuntimeException("can not convert null to double!");
        }
        return Double.valueOf(value.toString());
    }
    private static Object toString(Object value, OperateMap operateMap) {
        if (value==null) {
           return null;
        }
        return value.toString();
    }

    private static Object index(Object value, OperateMap operateMap) {
        if (value==null) {
            return 0;
        }
        return value.toString().indexOf(operateMap.getParams().get(1).toString());
    }
}
