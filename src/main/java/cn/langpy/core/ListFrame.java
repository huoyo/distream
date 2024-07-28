package cn.langpy.core;


import cn.langpy.model.DataBaseConfig;
import cn.langpy.model.ExpressionMap;
import cn.langpy.util.DataBaseUtil;
import cn.langpy.util.ExpressUtil;

import javax.sql.DataSource;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * zhangchang
 */
public class ListFrame<E> extends ArrayList<E> {
    private static Pattern doublePattern = Pattern.compile("^[0-9]+\\.[0-9]+$");

    DataSource dataSource = null;
    private ListFrame<E> data = null;
    private Map<String, ListFrame<Object>> columnData = new LinkedHashMap<>();

    private Map<String, ListFrame<Object>> getColumnData() {
        return columnData;
    }

    private void setColumnData(Map<String, ListFrame<Object>> columnData) {
        this.columnData = columnData;
    }

    private List<String> columns = new ArrayList<>();
    private static List<DataHandlerInterface> dataHandlers = new ArrayList<>();
    private static List<String> expHandlers = new ArrayList<>();

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }


    public ListFrame() {
        data = this;
    }

    public ListFrame(Collection<E> c) {
        super(c);
        if (c instanceof ListFrame) {
            data = (ListFrame<E>) c;
        } else {
            data = new ListFrame();
            Iterator<E> iterator = c.iterator();
            while (iterator.hasNext()) {
                data.add(iterator.next());
            }
        }
    }

    @Override
    public boolean add(E e) {
        if (e!=null && !isBaseType(e)) {
            addInnerColumnData(e);
        }
        return super.add(e);
    }

    private boolean isBaseType(E e){
        if (e instanceof Number ||
                e instanceof String ||
                e instanceof Character ||
                e instanceof LocalDateTime ||
                e instanceof Date ||
                e instanceof Time ||
                e instanceof LocalDate ||
                e instanceof Boolean) {
            return true;
        }
        return false;
    }

    private void addInnerColumnData(E e) {
        if (e instanceof Map) {
            Map map = (Map) e;
            for (Object o : map.keySet()) {
                if (!columns.contains((String) o)) {
                    columns.add((String) o);
                }
                ListFrame<Object> columnList = columnData.get(o);
                if (columnList == null) {
                    columnList = new ListFrame();
                    columnList.add(map.get(o));
                    columnData.put((String) o, columnList);
                } else {
                    columnList.add(map.get(o));
                }
            }
        } else {
            Field[] fields = e.getClass().getDeclaredFields();

            for (Field field : fields) {
                int mod = field.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                    continue;
                }
                if (!columns.contains(field.getName())) {
                    columns.add(field.getName());
                }
                field.setAccessible(true);
                Object value = null;
                try {
                    value = field.get(e);
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                }
                ListFrame<Object> columnList = columnData.get(field.getName());
                if (columnList == null) {
                    columnList = new ListFrame();
                    columnList.add(value);
                    columnData.put(field.getName(), columnList);
                } else {
                    columnList.add(value);
                }
                field.setAccessible(false);
            }
        }
    }

    public static <E> ListFrame<E> asList(E... elements) {
        ListFrame<E> listFrame = new ListFrame();
        for (E e : elements) {
            listFrame.add(e);
        }
        return listFrame;
    }

    public static <E> ListFrame<E> fromList(List<E> list) {
        if (list instanceof ListFrame) {
            return (ListFrame<E>) list;
        }
        ListFrame<E> listFrame = new ListFrame();
        Iterator<E> iterator = list.iterator();
        while (iterator.hasNext()) {
            listFrame.add(iterator.next());
        }
//        for (E e : list) {
//            listFrame.add(e);
//        }
        return listFrame;
    }

    public void initDataSource(DataSource dataSource) {
        if (this.dataSource == null) {
            this.dataSource = dataSource;
        }
    }

    public void initDataSource(DataBaseConfig dataBaseConfig) {
        if (this.dataSource == null) {
            this.dataSource = DataBaseUtil.getDataSource(dataBaseConfig);
        }
    }

    public ListFrame<Map<String, Object>> readSql(String sql) {
        if (this.dataSource == null) {
            throw new RuntimeException("please initilize datasource first!");
        }
        return DataBaseUtil.readSql(sql, this.dataSource);
    }

    public static ListFrame<Map<String, Object>> readSql(String sql, DataSource dataSource) {
        return DataBaseUtil.readSql(sql, dataSource);
    }

    public static ListFrame<Map<String, Object>> readSql(String sql, DataBaseConfig config) {
        return DataBaseUtil.readSql(sql, config);
    }

    public static ListFrame<String> readString(String path, Charset charset) {
        File file = new File(path);
        ListFrame<String> listFrame = new ListFrame();
        try (
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader streamReader = new InputStreamReader(fileInputStream, charset);
                BufferedReader br = new BufferedReader(streamReader)
        ) {
            String line = "";
            while ((line = br.readLine()) != null) {
                listFrame.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listFrame;
    }

    public static ListFrame<String> readString(String path) {
        return readString(path, Charset.defaultCharset());
    }

    public static ListFrame<Map<String, Object>> readMap(String path) {
        return readMap(path, ",");
    }

    public static ListFrame<Map<String, Object>> readMap(String path, Charset charset) {
        return readMap(path, ",", charset);
    }

    public <T> ListFrame<T> toObjectList(Class<T> beanClass) {
        ListFrame<T> listFrame = new ListFrame<>();
        if (this.data == null) {
            return null;
        }
        if (this.data.size() == 0) {
            return listFrame;
        }
        E e1 = data.get(0);
        if (e1 instanceof Map) {
            try {
                for (E datum : data) {
                    Map map = (Map) datum;
                    T object = mapToObject(map, beanClass);
                    listFrame.add(object);
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            try {
                for (E datum : data) {
                    T object = objectToObject(datum, beanClass);
                    listFrame.add(object);
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return listFrame;
    }

    public ListFrame<Map<String, Object>> toMapList() {
        if (this.data == null) {
            return null;
        }
        ListFrame<Map<String, Object>> listFrame = new ListFrame<>();
        if (this.data.size() == 0) {
            return listFrame;
        }
        E e1 = data.get(0);
        if (e1 instanceof Map) {
            return (ListFrame<Map<String, Object>>) this.data;
        }
        Field[] fields = data.get(0).getClass().getDeclaredFields();
        for (E datum : data) {
            Map<String, Object> map = new HashMap<>();
            for (Field field : fields) {
                int mod = field.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    map.put(field.getName(), field.get(datum));
                    field.setAccessible(false);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            listFrame.add(map);
        }
        return listFrame;
    }

    private static <T> T mapToObject(Map map, Class<T> beanClass) throws IllegalAccessException, InstantiationException {
        T object = beanClass.newInstance();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                continue;
            }
            field.setAccessible(true);
            field.set(object, map.get(field.getName()));
        }
        return object;
    }

    private static <T> T objectToObject(Object map, Class<T> beanClass) throws IllegalAccessException, InstantiationException {
        T object = beanClass.newInstance();
        Field[] fields = object.getClass().getDeclaredFields();

        Field[] targetFields = map.getClass().getDeclaredFields();

        for (Field field : fields) {
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                continue;
            }
            field.setAccessible(true);
            Optional<Field> keyFieldOption = Arrays.stream(targetFields).filter(name -> name.getName().equals(field.getName())).findFirst();
            if (keyFieldOption.isPresent()) {
                Field field1 = keyFieldOption.get();
                field1.setAccessible(true);
                field.set(object, field1.get(map));
                field1.setAccessible(false);
            }
            field.setAccessible(false);
        }
        return object;
    }

    public static ListFrame<Map<String, Object>> readMap(String path, String splitBy, List<Class> columnTypes, Charset charset) {
        File file = new File(path);
        ListFrame<Map<String, Object>> listFrame = new ListFrame();
        try (
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader streamReader = new InputStreamReader(fileInputStream, charset);
                BufferedReader br = new BufferedReader(streamReader)
        ) {

            String line = "";
            String[] titles = null;
            if ((line = br.readLine()) != null) {
                titles = line.split(splitBy);
            }
            while ((line = br.readLine()) != null) {
                String[] split = line.split(splitBy);
                Map map = new LinkedHashMap();
                for (int i = 0; i < split.length; i++) {
                    map.put(titles[i], getTypeValue(split[i], columnTypes.get(i)));
//                    setListColumns(listFrame, titles[i], getTypeValue(split[i], columnTypes.get(i)));
                }
                listFrame.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listFrame;
    }

    public static ListFrame<Map<String, Object>> readMap(String path, String splitBy, Class[] columnTypes, Charset charset) {
        return readMap(path, splitBy, Arrays.stream(columnTypes).collect(Collectors.toList()), charset);
    }

    public static ListFrame<Map<String, Object>> readMap(String path, String splitBy, Class[] columnTypes) {
        return readMap(path, splitBy, Arrays.stream(columnTypes).collect(Collectors.toList()), Charset.defaultCharset());
    }

    public static ListFrame<Map<String, Object>> readMap(String path, Class[] columnTypes, Charset charset) {
        return readMap(path, ",", Arrays.stream(columnTypes).collect(Collectors.toList()), charset);
    }

    public static ListFrame<Map<String, Object>> readMap(String path, Class[] columnTypes) {
        return readMap(path, ",", Arrays.stream(columnTypes).collect(Collectors.toList()), Charset.defaultCharset());
    }

    private static Object getTypeValue(Object v, Class<?> c) {
        if (c == Integer.class) {
            return Integer.valueOf(v.toString());
        }
        if (c == Double.class) {
            return Double.valueOf(v.toString());
        }
        if (c == Float.class) {
            return Float.valueOf(v.toString());
        }
        return v.toString();
    }


    public static ListFrame<Map<String, Object>> readMap(String path, String splitBy) {
        return readMap(path, splitBy, Charset.defaultCharset());
    }

    public static ListFrame<Map<String, Object>> readMap(String path, String splitBy, Charset charset) {
        File file = new File(path);
        ListFrame<Map<String, Object>> listFrame = new ListFrame();
        try (
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader streamReader = new InputStreamReader(fileInputStream, charset);
                BufferedReader br = new BufferedReader(streamReader)
        ) {

            String line = "";
            String[] titles = null;
            if ((line = br.readLine()) != null) {
                titles = line.split(splitBy);
//                listFrame.setColumns(Arrays.stream(titles).collect(Collectors.toList()));
            }
            while ((line = br.readLine()) != null) {
                String[] split = line.split(splitBy);
                Map map = new LinkedHashMap();
                for (int i = 0; i < split.length; i++) {
                    map.put(titles[i], split[i]);
//                    setListColumns(listFrame, titles[i], split[i]);
                }
                listFrame.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listFrame;
    }

    private static void setListColumns(ListFrame<Map<String, Object>> listFrame, String title, Object value) {
        if (listFrame.columnData.containsKey(title)) {
            listFrame.columnData.get(title).add(value);
        } else {
            ListFrame columnsData = new ListFrame();
            columnsData.add(value);
            listFrame.columnData.put(title, columnsData);
        }
    }


    public <T> ListFrame<T> get(Function<E, T> fun) {
        ListFrame<T> listFrame = new ListFrame<>();
        if (this.data == null) {
            return new ListFrame<>();
        }
        if (this.data.size() == 0) {
            return listFrame;
        }
        Object o = data.get(0);
        if (o instanceof String || o instanceof Map) {
            throw new RuntimeException("unsupported operate!");
        } else {
            for (E datum : data) {
                listFrame.add(fun.apply(datum));
            }
            return listFrame;
        }
    }

    public <T> ListFrame<T> get(String columnName) {
        ListFrame<Object> objects = columnData.get(columnName);
        if (null != objects) {
            return (ListFrame<T>) objects;
        }
        ListFrame<Object> listFrame = new ListFrame<>();
        if (this.data == null) {
            return new ListFrame<>();
        }
        if (this.data.size() == 0) {
            return new ListFrame<T>();
        }
        Object o = data.get(0);
        if (o instanceof String) {
            throw new RuntimeException("unsupported operate!");
        } else if (o instanceof Map) {
            for (E datum : data) {
                Map<String, Object> e = (Map<String, Object>) datum;
                listFrame.add(e.get(columnName));
            }
            columnData.put(columnName, listFrame);
            return (ListFrame<T>) listFrame;
        } else {
            for (E datum : data) {
                listFrame.add(ExpressUtil.getValue(columnName, datum));
            }
            columnData.put(columnName, listFrame);
            return (ListFrame<T>) listFrame;
        }
    }

    public <T> ListFrame<T> distinct() {
        ListFrame<T> listFrame = new ListFrame<>();
        for (E datum : data) {
            if (!listFrame.contains(datum)) {
                listFrame.add((T) datum);
            }
        }
        return listFrame;
    }

    public <T> ListFrame<T> sortAsc() {
        data.sort(new Comparator<E>() {
            @Override
            public int compare(E o1, E o2) {
                if (o1 instanceof Number) {
                    double v = ((Number) o1).doubleValue() - ((Number) o2).doubleValue();
                    return (int) v;
                } else if (o1 instanceof String) {
                    double o1v = Double.valueOf(o1.toString()).doubleValue();
                    double o2v = Double.valueOf(o2.toString()).doubleValue();
                    double v = o1v - o2v;
                    return (int) v;
                }
                return 0;
            }
        });
        return (ListFrame<T>) data;
    }

    public <T> ListFrame<T> sortAsc(String columnName) {
        data.sort(new Comparator<E>() {
            @Override
            public int compare(E o1, E o2) {
                Object value1 = ExpressUtil.getValue(columnName, o1);
                Object value2 = ExpressUtil.getValue(columnName, o2);
                double o1v = Double.valueOf(value1.toString()).doubleValue();
                double o2v = Double.valueOf(value2.toString()).doubleValue();
                double v = o1v - o2v;
                return (int) v;
            }
        });
        return (ListFrame<T>) data;
    }

    public <T> ListFrame<T> sortDesc(String columnName) {
        data.sort(new Comparator<E>() {
            @Override
            public int compare(E o1, E o2) {
                Object value1 = ExpressUtil.getValue(columnName, o1);
                Object value2 = ExpressUtil.getValue(columnName, o2);
                double o1v = Double.valueOf(value1.toString()).doubleValue();
                double o2v = Double.valueOf(value2.toString()).doubleValue();
                double v = o2v - o1v;
                return (int) v;
            }
        });
        return (ListFrame<T>) data;
    }

    public <T> ListFrame<T> sortDesc() {
        data.sort(new Comparator<E>() {
            @Override
            public int compare(E o1, E o2) {
                if (o1 instanceof Number) {
                    double v = ((Number) o2).doubleValue() - ((Number) o1).doubleValue();
                    return (int) v;
                } else if (o1 instanceof String) {
                    double o1v = Double.valueOf(o1.toString()).doubleValue();
                    double o2v = Double.valueOf(o2.toString()).doubleValue();
                    double v = o2v - o1v;
                    return (int) v;
                }
                return 0;
            }
        });
        return (ListFrame<T>) data;
    }

    public <T> ListFrame<T> reverse() {
        ListFrame<T> frame = new ListFrame<T>();
        for (int i = data.size() - 1; i >= 0; i--) {
            frame.add((T) data.get(i));
        }
        return frame;
    }

    public ListFrame<E> replace(String src, String tar) {
        if (data == null || data.size() == 0) {
            return new ListFrame<E>();
        }
        Object o = data.get(0);
        if (o instanceof String) {
            ListFrame<String> numFrame = new ListFrame<String>();
            for (E datum : data) {
                String map = (String) datum;
                numFrame.add(map.replaceAll(src, tar));
            }
            return (ListFrame<E>) numFrame;
        } else if (o instanceof Map) {
            ListFrame<Map> numFrame = new ListFrame<Map>();
            for (E datum : data) {
                Map map = (Map) datum;
                for (Object o1 : map.keySet()) {
                    map.put(o1, map.get(o1).toString().replaceAll(src, tar));
                }
                numFrame.add(map);
            }
            return (ListFrame<E>) numFrame;
        } else {
            throw new RuntimeException("please define a property that you want to replace!");
        }
    }

    public ListFrame<E> replace(String column, String src, String tar) {
        if (data == null || data.size() == 0) {
            return new ListFrame<E>();
        }
        Object o = data.get(0);
        if (o instanceof String) {
            ListFrame<String> numFrame = new ListFrame<String>();
            for (E datum : data) {
                String map = (String) datum;
                numFrame.add(map.replaceAll(src, tar));
            }
            return (ListFrame<E>) numFrame;
        } else if (o instanceof Map) {
            for (E datum : data) {
                Map map = (Map) datum;
                Object columnValue = map.get(column).toString().replaceAll(src, tar);
                map.put(column, columnValue);
            }
            return data;
        } else {
            for (E datum : data) {
                Object columnValue = ExpressUtil.getValue(column, datum);
                datum = ExpressUtil.setValue(column, columnValue.toString().replaceAll(src, tar), datum);
            }
            return data;
        }
    }

    public ListFrame<E> addHandler(DataHandlerInterface<E> dataProcess) {
        dataHandlers.add(dataProcess);
        return this;
    }

    public ListFrame<E> addHandler(String expressions) {
        expHandlers.add(expressions);
        return this;
    }

    public synchronized ListFrame<E> execute() {
        if (data == null || data.size() == 0) {
            return new ListFrame<E>();
        }
        ListFrame<E> numFrame = new ListFrame<E>();
        Iterator<E> iterator = data.iterator();
        while (iterator.hasNext()) {
            E datum = iterator.next();
            for (DataHandlerInterface<E> eDataHandlerInterface : dataHandlers) {
                datum = eDataHandlerInterface.handle(datum);
            }
            for (String expHandler : expHandlers) {
                List<ExpressionMap> ops = ExpressUtil.getOperates(expHandler);
                for (ExpressionMap op : ops) {
                    datum = ExpressUtil.operate(datum, op);
                }
            }
            numFrame.add(datum);

        }
        dataHandlers.clear();
        expHandlers.clear();
        return numFrame;
    }

    public ListFrame<E> handle(Predicate<E> condition, DataHandlerInterface<E>... dataProcessArray) {
        if (data == null || data.size() == 0) {
            return new ListFrame<E>();
        }
        ListFrame<E> numFrame = new ListFrame<E>();
        for (E datum : data) {
            if (condition.test(datum)) {
                for (DataHandlerInterface<E> eDataHandlerInterface : dataProcessArray) {
                    datum = eDataHandlerInterface.handle(datum);
                }
                numFrame.add(datum);
            } else {
                numFrame.add(datum);
            }
        }
        return numFrame;
    }

    public ListFrame<E> handle(DataHandlerInterface<E> dataProcess) {
        return handle(a -> true, dataProcess);
    }

    public ListFrame<E> handle(DataHandlerInterface<E>... dataProcess) {
        return handle(a -> true, dataProcess);
    }

    public ListFrame<E> handle(Predicate<E> condition, String ifExpressions, String elseExpressions) {
        if (data == null || data.size() == 0) {
            return new ListFrame<E>();
        }
        List<ExpressionMap> ifOps = ExpressUtil.getOperates(ifExpressions);
        List<ExpressionMap> elseOps = ExpressUtil.getOperates(elseExpressions);
        ListFrame<E> numFrame = new ListFrame<E>();
        for (E datum : data) {
            for (ExpressionMap op : ifOps) {
                if (condition.test(datum)) {
                    datum = ExpressUtil.operate(datum, op);
                }
            }
            for (ExpressionMap op : elseOps) {
                if (!condition.test(datum)) {
                    datum = ExpressUtil.operate(datum, op);
                }
            }
            numFrame.add(datum);
        }
        return numFrame;
    }

    public ListFrame<E> handle(Predicate<E> condition, String... expressions) {
        if (data == null || data.size() == 0) {
            return new ListFrame<E>();
        }
        List<ExpressionMap> ops = ExpressUtil.getOperates(expressions);
        ListFrame<E> numFrame = new ListFrame<E>();
        Iterator<E> iterator = data.iterator();
        while (iterator.hasNext()) {
            E datum = iterator.next();
            if (condition.test(datum)) {
                for (ExpressionMap op : ops) {
                    datum = ExpressUtil.operate(datum, op);
                }
            }
            numFrame.add(datum);
        }

        return numFrame;
    }


    public ListFrame<E> handle(String expressions) {
        return handle(a -> true, expressions);
    }

    public ListFrame<E> handle(String... expressions) {
        return handle(a -> true, expressions);
    }

    public <T> ListFrame<T> handle(Predicate<E> condition, Function<E, T> fun) {
        if (data == null || data.size() == 0) {
            return new ListFrame<T>();
        }
        ListFrame<T> numFrame = new ListFrame<T>();
        Iterator<E> iterator = data.iterator();
        while (iterator.hasNext()) {
            E datum = iterator.next();
            if (condition.test(datum)) {
                numFrame.add(fun.apply(datum));
            } else {
                numFrame.add((T) datum);
            }
        }
//        for (E datum : data) {
//            if (condition.test(datum)) {
//                numFrame.add(fun.apply(datum));
//            } else {
//                numFrame.add((T) datum);
//            }
//        }
        return numFrame;
    }

    public <T> ListFrame<T> handle(Function<E, T> fun) {
        return handle(a -> true, fun);
    }

    public MapFrame<Object, ListFrame> groupBy(String columnName) {
        if (data == null || data.size() == 0) {
            return new MapFrame<>();
        }
        MapFrame<Object, ListFrame> groupMap = new MapFrame<>();

        Object o = data.get(0);
        if (o instanceof String) {
            throw new RuntimeException("unsupported operate for String!");
        } else if (o instanceof Map) {
            for (E datum : data) {
                Map map = (Map) datum;
                Object columnValue = map.get(columnName);
                if (groupMap.containsKey(columnValue)) {
                    groupMap.get(columnValue).add(datum);
                } else {
                    ListFrame listFrame = new ListFrame();
                    listFrame.add(datum);
                    groupMap.put(columnValue, listFrame);
                }
            }
        } else {
            for (E datum : data) {
                Object columnValue = ExpressUtil.getValue(columnName, datum);
                if (groupMap.containsKey(columnValue)) {
                    groupMap.get(columnValue).add(datum);
                } else {
                    ListFrame listFrame = new ListFrame();
                    listFrame.add(datum);
                    groupMap.put(columnValue, listFrame);
                }
            }
        }

        return groupMap;
    }

    public int argmax() {
        if (data == null || data.size() == 0) {
            return 0;
        }
        if (doublePattern.matcher(data.get(0).toString()).find()) {
            Double max = 0.0;
            int index = 0;
            int n = 0;
            for (E datum : data) {
                if (datum == null) {
                    continue;
                }
                double a = Double.valueOf(datum + "");
                if (a > max) {
                    max = a;
                    index = n;
                }
                n++;
            }
            return index;
        } else {
            Integer max = 0;
            int index = 0;
            int n = 0;
            for (E datum : data) {
                if (datum == null) {
                    continue;
                }
                int a = Integer.valueOf(datum + "");
                if (a > max) {
                    max = a;
                    index = n;
                }
                n++;
            }
            return index;
        }
    }

    public <T> T max() {
        if (data == null || data.size() == 0) {
            return null;
        }
        if (doublePattern.matcher(data.get(0).toString()).find()) {
            Double max = Double.MIN_VALUE;
            for (E datum : data) {
                if (datum == null) {
                    continue;
                }
                double a = Double.valueOf(datum + "");
                if (a > max) {
                    max = a;
                }
            }
            return (T) max;
        } else {
            Integer max = 0;
            for (E datum : data) {
                if (datum == null) {
                    continue;
                }
                int a = Integer.valueOf(datum + "");
                if (a > max) {
                    max = a;
                }
            }
            return (T) max;
        }
    }


    public int argmin() {
        if (data == null || data.size() == 0) {
            return 0;
        }
        if (doublePattern.matcher(data.get(0).toString()).find()) {
            Double max = Double.MAX_VALUE;
            int index = 0;
            int n = 0;
            for (E datum : data) {
                if (datum == null) {
                    continue;
                }
                double a = Double.valueOf(datum + "");
                if (a < max) {
                    max = a;
                    index = n;
                }
                n++;
            }
            return index;
        } else {
            Integer max = Integer.MAX_VALUE;
            int index = 0;
            int n = 0;
            for (E datum : data) {
                if (datum == null) {
                    continue;
                }
                int a = Integer.valueOf(datum + "");
                if (a < max) {
                    max = a;
                    index = n;
                }
                n++;
            }
            return index;
        }
    }

    public <T> T min() {
        if (data == null || data.size() == 0) {
            return null;
        }
        if (doublePattern.matcher(data.get(0).toString()).find()) {
            Double max = Double.MAX_VALUE;
            for (E datum : data) {
                if (datum == null) {
                    continue;
                }
                double a = Double.valueOf(datum + "");
                if (a < max) {
                    max = a;
                }
            }
            return (T) max;
        } else {
            Integer max = Integer.MAX_VALUE;
            for (E datum : data) {
                if (datum == null) {
                    continue;
                }
                int a = Integer.valueOf(datum + "");
                if (a < max) {
                    max = a;
                }
            }
            return (T) max;
        }
    }

    public double avg() {
        if (data == null || data.size() == 0) {
            return 0.0;
        }
        double sum = 0.0;
        for (E datum : data) {
            if (datum == null) {
                continue;
            }
            double a = Double.valueOf(datum + "");
            sum += a;
        }
        return sum / data.size();
    }

    public double sum() {
        if (data == null || data.size() == 0) {
            return 0.0;
        }
        double sum = 0.0;
        for (E datum : data) {
            if (datum == null) {
                continue;
            }
            double a = Double.valueOf(datum + "");
            sum += a;
        }
        return sum;
    }


    public ListFrame<Double> asDouble() {
        ListFrame<Double> listFrame = new ListFrame<Double>();
        for (E datum : data) {
            if (datum instanceof Double) {
                listFrame.add((Double) datum);
            } else {
                listFrame.add(Double.valueOf(datum + ""));
            }
        }
        return listFrame;
    }

    public ListFrame<Float> asFloat() {
        ListFrame<Float> listFrame = new ListFrame<Float>();
        for (E datum : data) {
            if (datum instanceof Float) {
                listFrame.add((Float) datum);
            } else {
                listFrame.add(Float.valueOf(datum + ""));
            }
        }
        return listFrame;
    }

    public ListFrame<Integer> asInteger() {
        ListFrame<Integer> listFrame = new ListFrame<Integer>();
        for (E datum : data) {
            if (datum instanceof Integer) {
                listFrame.add((Integer) datum);
            } else {
                listFrame.add(Integer.valueOf(datum + ""));
            }
        }
        return listFrame;
    }

    public ListFrame<String> asString() {
        ListFrame<String> listFrame = new ListFrame<String>();
        for (E datum : data) {
            if (datum instanceof String) {
                listFrame.add((String) datum);
            } else {
                listFrame.add(datum + "");
            }
        }
        return listFrame;
    }


    public List<E> toList() {
        return new CopyOnWriteArrayList<>(data);
    }


    public void toFile(String path) {
        try (
                FileWriter writer = new FileWriter(path);
                BufferedWriter bw = new BufferedWriter(writer)
        ) {
            List<String> columns = getColumns();
            bw.write(String.join(",", columns) + "\n");
            for (E datum : data) {
                if (datum instanceof String) {
                    bw.write(datum.toString() + "\n");
                } else if (datum instanceof Map) {
                    Map map = (Map) datum;
                    StringBuffer line = new StringBuffer();

                    int columnsSize = columns.size();
                    for (int i = 0; i < columnsSize; i++) {
                        String column = columns.get(i);
                        if (i < columnsSize - 1) {
                            line.append(map.get(column) + ",");
                        } else {
                            line.append(map.get(column));
                        }
                    }
                    line.append("\n");
                    bw.write(line.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double variance() {
        double avg = this.avg();
        double sum = 0.0;
        for (E datum : data) {
            double a = Double.valueOf(datum + "");
            sum += Math.pow(a - avg, 2);
        }
        return sum / data.size();
    }

    public double standardDeviation() {
        return Math.sqrt(variance());
    }

    public MapFrame<E, Integer> frequency() {
        MapFrame<E, Integer> mapFrame = new MapFrame<>();
        for (E datum : data) {
            if (mapFrame.containsKey(datum)) {
                mapFrame.put(datum, mapFrame.get(datum) + 1);
            } else {
                mapFrame.put(datum, 1);
            }
        }
        return mapFrame;
    }

    public <T> ListFrame<T> dropNull() {
        ListFrame<T> listFrame = new ListFrame<>();
        for (E datum : data) {
            if (datum != null) {
                listFrame.add((T) datum);
            }
        }
        return listFrame;
    }

    public E sample() {
        int i = ThreadLocalRandom.current().nextInt(this.size());
        return this.get(i);
    }

    public ListFrame<E> sample(int n) {
        if (n<=0) {
            throw new RuntimeException("the number you want to sample can not <= 0!");
        }
        int size = this.size();
        ListFrame<E> listFrame = new ListFrame<>();
        for (int i = 0; i < n; i++) {
            int index = ThreadLocalRandom.current().nextInt(size);
            listFrame.add(this.get(index));
        }
        return listFrame;
    }

    public ListFrame<E> sample(int n,boolean duplicate) {
        if (n<=0) {
            throw new RuntimeException("the number you want to sample can not <= 0!");
        }
        if (duplicate) {
            return sample(n);
        }
        int size = this.size();
        if (n>size) {
            throw new RuntimeException("the number you want to sample can not > the size of the list!");
        }
        List<Integer> list = new ArrayList<>();
        ListFrame<E> listFrame = new ListFrame<>();
        while (listFrame.size()<n){
            int index = ThreadLocalRandom.current().nextInt(size);
            if (list.contains(index)) {
                continue;
            }
            listFrame.add(this.get(index));
            list.add(index);
        }
        return listFrame;
    }


}
