package cn.langpy.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressUtil {
    public static Logger log = Logger.getLogger(ExpressUtil.class.toString());

    private static Map<String, Field> fieldMap = new ConcurrentHashMap<>();

    private static Pattern operatePattern = Pattern.compile("[-+*/]+");

    private static Pattern stringPattern = Pattern.compile("^'.*'$");

    private static Pattern intPattern = Pattern.compile("^[0-9]+$");

    private static Pattern doublePattern = Pattern.compile("^[0-9]+\\.[0-9]+$");

    private static Pattern sumPattern = Pattern.compile("^sum\\(.+\\)$");

    private static Pattern avgPattern = Pattern.compile("^avg\\(.+\\)$");

    private static Pattern formatPattern = Pattern.compile("^format\\(.+,\\s*[0-9]+\\s*\\)$");

    private static Pattern replacePattern = Pattern.compile("^replace\\(.+,\\s*'.*',\\s*'.*'\\s*\\)$");

    private static Pattern substringPattern = Pattern.compile("^substring\\(.+,\\s*[0-9]+,\\s*[0-9]+\\s*\\)$");

    public static List<Map<String, String>> getOperates(String expresses) {
        List<Map<String, String>> ops = new ArrayList<>();
        String[] expressesSplit = expresses.split(";");
        for (String express : expressesSplit) {
            String[] expressSplit = express.split("=");
            String leftKey = expressSplit[0].trim();
            String rights = expressSplit[1].trim();
            String[] rightsSplit = rights.split("[-+*/]+");
            String operateKey1 = rightsSplit[0].trim();
            String operateKey2 = null;
            if (rightsSplit.length > 1) {
                operateKey2 = rightsSplit[1].trim();
            }
            Matcher m = operatePattern.matcher(express);//
            String operater = "AAAA";
            if (m.find()) {
                operater = m.group();
            }

            Map<String, String> map = new HashMap<>();
            map.put("leftKey", leftKey);
            map.put("operateKey1", operateKey1);
            map.put("operateKey2", operateKey2);
            map.put("operater", operater);
            ops.add(map);
        }
        return ops;
    }

    public static <E> E operate(E datum, Map<String, String> op) {
        String leftKey = op.get("leftKey");
        String operateKey1 = op.get("operateKey1");
        String operateKey2 = op.get("operateKey2");
        String operater = op.get("operater");

        Object value1 = getValue(operateKey1, datum);
        Object value2 = getValue(operateKey2, datum);

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


    public static Object getValue(String key, Object param) {
        if (key == null) {
            return null;
        }
        if (intPattern.matcher(key).find()) {
            return Integer.valueOf(key);
        }
        if (doublePattern.matcher(key).find()) {
            return Double.valueOf(key);
        }
        if (stringPattern.matcher(key).find()) {
            return key.replace("'", "");
        }
        String d = null;
        if (formatPattern.matcher(key).find()) {
            String newKey = key.substring(7, key.indexOf(","));
            d = key.substring(key.indexOf(",")+1, key.indexOf(",") + 2);
            key = newKey;
        }
        String src = null;
        String tar = null;
        if (replacePattern.matcher(key).find()) {
            String newKey = key.substring(8, key.indexOf(",")).trim();
            String[] split = key.split(",");
            src = split[1].trim().replace("'","");
            tar = (split[2].substring(0,split[2].indexOf(")"))).replace("'","");
            key = newKey;
        }

        int subStart = 0;
        int subEnd = 0;
        if (substringPattern.matcher(key).find()) {
            String newKey = key.substring(10, key.indexOf(",")).trim();
            String[] split = key.split(",");
            subStart = Integer.valueOf(split[1].trim());
            subEnd = Integer.valueOf((split[2].substring(0,split[2].indexOf(")"))).trim());
            key = newKey;
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
        if (d != null && keyValue instanceof Double) {
            BigDecimal bigDecimal = new BigDecimal((double)keyValue);
            double doubleValue = bigDecimal.setScale(Integer.valueOf(d), BigDecimal.ROUND_HALF_UP).doubleValue();
            return doubleValue;
        }
        if (src!=null) {
            return keyValue.toString().replace(src,tar);
        }
        if (subEnd!=0) {
            return keyValue.toString().substring(subStart,subEnd);
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
