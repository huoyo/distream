package cn.langpy.util;

import static cn.langpy.util.ExpressUtil.setValue;

public class ComputeUtil {

    public static <E> E add(String leftKey, Object value1, Object value2, E datum) {
        if (value1 instanceof Integer && value2 instanceof Integer) {
            datum = setValue(leftKey, Integer.valueOf(value1.toString()) + Integer.valueOf(value2.toString()), datum);
        } else if (value1 instanceof Integer && value2 instanceof Double) {
            datum = setValue(leftKey, Integer.valueOf(value1.toString()) + Double.valueOf(value2.toString()), datum);
        } else if (value1 instanceof Double && value2 instanceof Integer) {
            datum = setValue(leftKey, Double.valueOf(value1.toString()) + Integer.valueOf(value2.toString()), datum);
        } else if (value1 instanceof Double && value2 instanceof Double) {
            datum = setValue(leftKey, Double.valueOf(value1.toString()) + Double.valueOf(value2.toString()), datum);
        } else {
            datum = setValue(leftKey, value1.toString() + value2.toString(), datum);
        }
        return datum;
    }

    public static <E> E sub(String leftKey, Object value1, Object value2, E datum) {
        if (value1 instanceof Integer && value2 instanceof Integer) {
            datum = setValue(leftKey, Integer.valueOf(value1.toString()) - Integer.valueOf(value2.toString()), datum);
        } else if (value1 instanceof Integer && value2 instanceof Double) {
            datum = setValue(leftKey, Integer.valueOf(value1.toString()) - Double.valueOf(value2.toString()), datum);
        } else if (value1 instanceof Double && value2 instanceof Integer) {
            datum = setValue(leftKey, Double.valueOf(value1.toString()) - Integer.valueOf(value2.toString()), datum);
        } else if (value1 instanceof Double && value2 instanceof Double) {
            datum = setValue(leftKey, Double.valueOf(value1.toString()) - Double.valueOf(value2.toString()), datum);
        } else if (value1 instanceof String) {
            datum = setValue(leftKey, ((String) value1).replace(value2.toString(),""), datum);
        } else {
            throw new RuntimeException("unknown data type:"+value1.getClass()+" and "+value2.getClass());
        }
        return datum;
    }

    public static <E> E mul(String leftKey, Object value1, Object value2, E datum) {
        if (value1 instanceof Integer && value2 instanceof Integer) {
            datum = setValue(leftKey, Integer.valueOf(value1.toString()) * Integer.valueOf(value2.toString()), datum);
        } else if (value1 instanceof Integer && value2 instanceof Double) {
            datum = setValue(leftKey, Integer.valueOf(value1.toString()) * Double.valueOf(value2.toString()), datum);
        } else if (value1 instanceof Double && value2 instanceof Integer) {
            datum = setValue(leftKey, Double.valueOf(value1.toString()) * Integer.valueOf(value2.toString()), datum);
        } else if (value1 instanceof Double && value2 instanceof Double) {
            datum = setValue(leftKey, Double.valueOf(value1.toString()) * Double.valueOf(value2.toString()), datum);
        } else if (ExpressUtil.canFormatInt(value1) && ExpressUtil.canFormatInt(value2)) {
            datum = setValue(leftKey, Integer.valueOf(value1.toString()) * Integer.valueOf(value2.toString()), datum);
        }else if (ExpressUtil.canFormatDouble(value1) && ExpressUtil.canFormatDouble(value2)) {
            datum = setValue(leftKey, Double.valueOf(value1.toString()) * Double.valueOf(value2.toString()), datum);
        }else {
            throw new RuntimeException("unknown data type:"+value1.getClass()+" and "+value2.getClass());
        }
        return datum;
    }


    public static <E> E div(String leftKey, Object value1, Object value2, E datum) {
        if (value1 instanceof Integer && value2 instanceof Integer) {
            double v = Double.valueOf(value1.toString()) / Double.valueOf(value2.toString());
            if (Double.isNaN(v)) {
                v = 0.0;
            }
            datum = setValue(leftKey,v , datum);
        } else if (value1 instanceof Integer && value2 instanceof Double) {
            double v =Double.valueOf(value1.toString()) / Double.valueOf(value2.toString());
            if (Double.isNaN(v)) {
                v = 0.0;
            }
            datum = setValue(leftKey, v, datum);
        } else if (value1 instanceof Double && value2 instanceof Integer) {
            double v = Double.valueOf(value1.toString()) / Double.valueOf(value2.toString());
            if (Double.isNaN(v)) {
                v = 0.0;
            }
            datum = setValue(leftKey, v, datum);
        } else if (value1 instanceof Double && value2 instanceof Double) {
            double v=Double.valueOf(value1.toString()) / Double.valueOf(value2.toString());
            if (Double.isNaN(v)) {
                v = 0.0;
            }
            datum = setValue(leftKey, v, datum);
        } else if (ExpressUtil.canFormatNumber(value1) && ExpressUtil.canFormatNumber(value2)) {
            double v = Double.valueOf(value1.toString()) / Double.valueOf(value2.toString());
            if (Double.isNaN(v)) {
                v = 0.0;
            }
            datum = setValue(leftKey, v, datum);
        } else {
            throw new RuntimeException("unknown data type:" + value1.getClass() + " and " + value2.getClass());
        }
        return datum;
    }

    public static <E> E eq(String leftKey, Object value1, E datum) {
        if (value1 instanceof Integer) {
            datum = setValue(leftKey, Integer.valueOf(value1.toString()), datum);
        } else if (value1 instanceof Double) {
            datum = setValue(leftKey, Double.valueOf(value1.toString()), datum);
        } else if (value1 instanceof Float) {
            datum = setValue(leftKey, Float.valueOf(value1.toString()), datum);
        } else {
            datum = setValue(leftKey, String.valueOf(value1.toString()), datum);
        }
        return datum;
    }

}
