package cn.langpy.util;

import cn.langpy.core.FunctionCenter;
import cn.langpy.model.ExpressionMap;
import cn.langpy.constant.Functions;
import cn.langpy.model.OperateMap;
import cn.langpy.constant.ParamType;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressUtil {
    public static Logger log = Logger.getLogger(ExpressUtil.class.toString());

    private static Pattern subOperateSymbol = Pattern.compile(".*'.*-+.*'.*");

    private static Map<String, Field> fieldMap = new ConcurrentHashMap<>();
    private static Map<String, List<ExpressionMap>> expressesMap = new ConcurrentHashMap<>();

    private static Pattern operatePattern = Pattern.compile("[-+*/]+");

    private static Pattern operatePattern_ = Pattern.compile("[+*/]+");

    private static Pattern stringPattern = Pattern.compile("^'.*'$");

    private static Pattern intPattern = Pattern.compile("^[0-9]+$");

    private static Pattern doublePattern = Pattern.compile("^[0-9]+\\.[0-9]+$");

    private static Pattern indexPattern = Pattern.compile("^index\\(.+\\s*,'.*'\\s*\\)$");

    private static Pattern toIntPattern = Pattern.compile("^int\\(.+\\s*\\)$");

    private static Pattern toDoublePattern = Pattern.compile("^double\\(.+\\s*\\)$");

    private static Pattern toStringPattern = Pattern.compile("^string\\(.+\\s*\\)$");

    private static Pattern formatPattern = Pattern.compile("^format\\(.+,\\s*[0-9]+\\s*\\)$");

    private static Pattern replacePattern = Pattern.compile("^replace\\(.+,\\s*'.*',\\s*'.*'\\s*\\)$");

    private static Pattern substringPattern = Pattern.compile("^substring\\(.+,\\s*[0-9]+,\\s*[0-9]+\\s*\\)$");

    public static boolean canFormatNumber(Object value) {
        if (value == null) {
            return false;
        }
        if (canFormatInt(value)) {
            return true;
        }
        if (canFormatDouble(value)) {
            return true;
        }
        return false;
    }

    public static boolean canFormatInt(Object value) {
        if (value == null) {
            return false;
        }
        if (intPattern.matcher(value.toString()).find()) {
            return true;
        }
        return false;
    }

    public static boolean canFormatDouble(Object value) {
        if (value == null) {
            return false;
        }
        if (intPattern.matcher(value.toString()).find()) {
            return true;
        }
        return false;
    }

    public static List<ExpressionMap> getOperates(String... expressesArray) {
        List<ExpressionMap> ops = new ArrayList<>();
        for (String expresses : expressesArray) {
            if (expressesMap.containsKey(expresses)) {
                ops.addAll(expressesMap.get(expresses));
                continue;
            }
            List<ExpressionMap> ops_ = new ArrayList<>();
            String[] expressesSplit = expresses.split(";");
            for (String express : expressesSplit) {
                String[] expressSplit = express.split("=");
                String leftKey = expressSplit[0].trim();
                String rights = expressSplit[1].trim();
                String[] rightsSplit = rights.split("[-+*/]+");
                if (subOperateSymbol.matcher(rights).find()) {
                    rightsSplit = rights.split("[+*/]+");
                }else {
                    rightsSplit = rights.split("[-+*/]+");
                }

                String operateKey1 = rightsSplit[0].trim();
                String operateKey2 = null;
                if (rightsSplit.length > 1) {
                    operateKey2 = rightsSplit[1].trim();
                }
                Matcher m = null;
                if (subOperateSymbol.matcher(express).find()) {
                    m = operatePattern_.matcher(express);
                }else {
                    m = operatePattern.matcher(express);
                }
                String operater = "AAAA";
                if (m.find()) {
                    operater = m.group();
                }

                ExpressionMap expressionMap = new ExpressionMap();
                expressionMap.setAssignKey(leftKey);
                expressionMap.setOperateSymbol(operater);
                OperateMap leftOperateMap = getOperateInfo(operateKey1);
                OperateMap rightOperateMap = getOperateInfo(operateKey2);
                expressionMap.setOperate1(leftOperateMap);
                expressionMap.setOperate2(rightOperateMap);
                ops_.add(expressionMap);
            }
            ops.addAll(ops_);
            expressesMap.put(expresses,ops_);
        }

        return ops;
    }

    public static OperateMap getOperateInfo(String key) {
        if (key == null) {
            return null;
        }
        OperateMap leftOperateMap = new OperateMap();
        if (intPattern.matcher(key).find()) {
            leftOperateMap.setParamType(ParamType.INT);
            List<Object> params = new ArrayList<>();
            params.add(Integer.valueOf(key));
            leftOperateMap.setParams(params);
        } else if (doublePattern.matcher(key).find()) {
            leftOperateMap.setParamType(ParamType.DOUBLE);
            List<Object> params = new ArrayList<>();
            params.add(Double.valueOf(key));
            leftOperateMap.setParams(params);
        } else if (stringPattern.matcher(key).find()) {
            leftOperateMap.setParamType(ParamType.STRING);
            List<Object> params = new ArrayList<>();
            params.add(key.replace("'", ""));
            leftOperateMap.setParams(params);
        } else if (formatPattern.matcher(key).find()) {
            String newKey = key.substring(7, key.indexOf(","));
            String d = key.substring(key.indexOf(",") + 1, key.indexOf(",") + 2);
            leftOperateMap.setParamType(ParamType.FUNCTION);
            leftOperateMap.setFunc(Functions.FORMAT);
            List<Object> params = new ArrayList<>();
            params.add(newKey);
            params.add(Integer.valueOf(d));
            leftOperateMap.setParams(params);
        } else if (replacePattern.matcher(key).find()) {
            String newKey = key.substring(8, key.indexOf(",")).trim();
            String[] split = key.split(",");
            String src = split[1].trim().replace("'", "");
            String tar = (split[2].substring(0, split[2].indexOf(")"))).replace("'", "");
            leftOperateMap.setParamType(ParamType.FUNCTION);
            leftOperateMap.setFunc(Functions.REPLACE);
            List<Object> params = new ArrayList<>();
            params.add(newKey);
            params.add(src);
            params.add(tar);
            leftOperateMap.setParams(params);

        } else if (substringPattern.matcher(key).find()) {
            String newKey = key.substring(10, key.indexOf(",")).trim();
            String[] split = key.split(",");
            int subStart = Integer.valueOf(split[1].trim());
            int subEnd = Integer.valueOf((split[2].substring(0, split[2].indexOf(")"))).trim());
            leftOperateMap.setParamType(ParamType.FUNCTION);
            leftOperateMap.setFunc(Functions.SUBSTRING);
            List<Object> params = new ArrayList<>();
            params.add(newKey);
            params.add(subStart);
            params.add(subEnd);
            leftOperateMap.setParams(params);
        } else if (toIntPattern.matcher(key).find()) {
            String newKey = key.substring(4, key.indexOf(")"));
            leftOperateMap.setParamType(ParamType.FUNCTION);
            leftOperateMap.setFunc(Functions.INT);
            List<Object> params = new ArrayList<>();
            params.add(newKey);
            leftOperateMap.setParams(params);
        } else if (toDoublePattern.matcher(key).find()) {
            String newKey = key.substring(7, key.indexOf(")"));
            leftOperateMap.setParamType(ParamType.FUNCTION);
            leftOperateMap.setFunc(Functions.DOUBLE);
            List<Object> params = new ArrayList<>();
            params.add(newKey);
            leftOperateMap.setParams(params);
        } else if (toStringPattern.matcher(key).find()) {
            String newKey = key.substring(7, key.indexOf(")"));
            leftOperateMap.setParamType(ParamType.FUNCTION);
            leftOperateMap.setFunc(Functions.STRING);
            List<Object> params = new ArrayList<>();
            params.add(newKey);
            leftOperateMap.setParams(params);
        } else if (indexPattern.matcher(key).find()) {
            String newKey = key.substring(6, key.indexOf(","));
            String src = key.substring(key.indexOf(",") + 1, key.indexOf(")"));
            leftOperateMap.setParamType(ParamType.FUNCTION);
            leftOperateMap.setFunc(Functions.INDEX);
            List<Object> params = new ArrayList<>();
            params.add(newKey.trim());
            params.add(src.trim().replace("'", ""));
            leftOperateMap.setParams(params);
        } else {
            leftOperateMap.setParamType(ParamType.VARIABLE);
            List<Object> params = new ArrayList<>();
            params.add(key);
            leftOperateMap.setParams(params);
        }
        return leftOperateMap;
    }

    public static <E> E operate(E datum, ExpressionMap op) {
        String leftKey = op.getAssignKey();
        String operater = op.getOperateSymbol();

        Object value1 = getValue(op.getOperate1(), datum);
        Object value2 = getValue(op.getOperate2(), datum);

        if (operater.equals("+")) {
            datum = ComputeUtil.add(leftKey, value1, value2, datum);
        } else if (operater.equals("-")) {
            datum = ComputeUtil.sub(leftKey, value1, value2, datum);
        } else if (operater.equals("*")) {
            datum = ComputeUtil.mul(leftKey, value1, value2, datum);
        } else if (operater.equals("/")) {
            datum = ComputeUtil.div(leftKey, value1, value2, datum);
        } else if (operater.equals("AAAA")) {
            datum = ComputeUtil.eq(leftKey, value1, datum);
        }
        return datum;
    }


    public static Object getValue(OperateMap operateMap, Object param) {
        if (operateMap == null) {
            return null;
        }
        if (operateMap.getParamType() == ParamType.INT || operateMap.getParamType() == ParamType.DOUBLE || operateMap.getParamType() == ParamType.STRING) {
            return operateMap.getParams().get(0);
        }
        String key = null;
        if (operateMap.getParamType() == ParamType.VARIABLE || operateMap.getParamType() == ParamType.FUNCTION) {
            key = operateMap.getParams().get(0).toString();
        }
        Object keyValue = getValue(key, param);
        BiFunction<Object, OperateMap, Object> biFunction = FunctionCenter.planConsumer.get(operateMap.getFunc());
        if (biFunction != null) {
            return biFunction.apply(keyValue, operateMap);
        }
        return keyValue;
    }


    public static Object getValue(String key, Object param) {
        if (key == null) {
            return null;
        }
        Object keyValue = null;
        if (param instanceof Map) {
            Map map = (Map) param;
            keyValue = map.get(key);
        } else {
            Field keyField = null;
            if (fieldMap.containsKey(key + param.getClass().getName())) {
                keyField = fieldMap.get(key + param.getClass().getName());
            } else {
                Field[] fields = param.getClass().getDeclaredFields();
                String keyf = key;
                Optional<Field> keyFieldOption = Arrays.stream(fields).filter(name -> name.getName().equals(keyf)).findFirst();
                keyFieldOption.orElseThrow(() -> new RuntimeException("unkown columnName!"));
                keyField = keyFieldOption.get();
                fieldMap.put(key + param.getClass().getName(), keyField);
            }
            keyField.setAccessible(true);
            try {
                keyValue = keyField.get(param);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } finally {
                keyField.setAccessible(false);
            }
        }
        return keyValue;
    }

    public static <T> T setValue(String key, Object value, T param) {
        if (param instanceof Map) {
            Map map = (Map) param;
            map.put(key, value);
            return (T) map;
        } else {
            Field keyField = null;
            if (fieldMap.containsKey(key + param.getClass().getName())) {
                keyField = fieldMap.get(key + param.getClass().getName());
            } else {
                Field[] fields = param.getClass().getDeclaredFields();
                Optional<Field> keyFieldOption = Arrays.stream(fields).filter(name -> name.getName().equals(key)).findFirst();
                keyFieldOption.orElseThrow(() -> new RuntimeException());
                keyField = keyFieldOption.get();
                fieldMap.put(key + param.getClass().getName(), keyField);
            }
            keyField.setAccessible(true);
            try {
                if ("Double".equals(keyField.getType().getSimpleName())) {
                    keyField.set(param, Double.valueOf(value.toString()));
                } else if ("Integer".equals(keyField.getType().getSimpleName())) {
                    keyField.set(param, Integer.valueOf(value.toString()));
                } else if ("Float".equals(keyField.getType().getSimpleName())) {
                    keyField.set(param, Float.valueOf(value.toString()));
                } else if ("String".equals(keyField.getType().getSimpleName())) {
                    keyField.set(param, value.toString());
                } else {
                    keyField.set(param, value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } finally {
                keyField.setAccessible(false);
            }
        }
        return param;
    }
}
