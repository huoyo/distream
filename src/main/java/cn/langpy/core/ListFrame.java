package cn.langpy.core;


import cn.langpy.model.DataBaseConfig;
import cn.langpy.model.ExpressionMap;
import cn.langpy.util.DataBaseUtil;
import cn.langpy.util.ExpressUtil;

import javax.sql.DataSource;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class ListFrame<E> extends ArrayList<E> {
    private static Pattern doublePattern = Pattern.compile("^[0-9]+\\.[0-9]+$");

    DataSource dataSource = null;
    ListFrame<E> data = null;
    Map<String, ListFrame<Object>> columnData = new LinkedHashMap<>();

    private Map<String, ListFrame<Object>> getColumnData() {
        return columnData;
    }

    private void setColumnData(Map<String, ListFrame<Object>> columnData) {
        this.columnData = columnData;
    }

    private List<String> columns;

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }


    public ListFrame(int initialCapacity) {
        super(initialCapacity);
    }

    public ListFrame() {
        data = this;
    }

    public ListFrame(Collection<E> c) {
        super(c);
        data = (ListFrame<E>) c;
    }

    public static <E> ListFrame<E> fromList(List<E> list) {
        if (list instanceof ListFrame) {
            return (ListFrame<E>) list;
        }
        ListFrame<E> listFrame = new ListFrame();
        for (E e : list) {
            listFrame.add(e);
        }
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


    /**
     * read data from txt/csv
     *
     * @param path
     * @return
     */
    public static ListFrame<String> readString(String path) {
        File file = new File(path);
        ListFrame<String> listFrame = new ListFrame();
        try (
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader streamReader = new InputStreamReader(fileInputStream);
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

    /**
     * read data from txt/csv
     *
     * @param path
     * @return
     */
    public static ListFrame<Map<String, Object>> readMap(String path) {
        return readMap(path, ",");
    }

    /**
     * convert a map to a obejct
     *
     * @param beanClass
     * @param <T>
     * @return
     */
    public <T> ListFrame<T> toObject(Class<T> beanClass) {
        ListFrame<T> listFrame = new ListFrame<>();
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

    /**
     * read data from txt/csv
     *
     * @param path
     * @param splitBy
     * @param columnTypes
     * @return
     */
    public static ListFrame<Map<String, Object>> readMap(String path, String splitBy, List<Class> columnTypes) {
        File file = new File(path);
        ListFrame<Map<String, Object>> listFrame = new ListFrame();
        try (
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader streamReader = new InputStreamReader(fileInputStream);
                BufferedReader br = new BufferedReader(streamReader)
        ) {

            String line = "";
            String[] titles = null;
            if ((line = br.readLine()) != null) {
                titles = line.split(splitBy);
                listFrame.setColumns(Arrays.stream(titles).collect(Collectors.toList()));
            }
            while ((line = br.readLine()) != null) {
                String[] split = line.split(splitBy);
                Map map = new LinkedHashMap();
                for (int i = 0; i < split.length; i++) {
                    map.put(titles[i], getTypeValue(split[i], columnTypes.get(i)));
                    setListColumns(listFrame, titles[i], getTypeValue(split[i], columnTypes.get(i)));
                }
                listFrame.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listFrame;
    }

    /**
     * read data from txt/csv
     *
     * @param path
     * @param splitBy
     * @param columnTypes
     * @return
     */
    public static ListFrame<Map<String, Object>> readMap(String path, String splitBy, Class[] columnTypes) {
        return readMap(path, splitBy, Arrays.stream(columnTypes).collect(Collectors.toList()));
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


    /**
     * read data from txt/csv
     *
     * @param path
     * @param splitBy
     * @return
     */
    public static ListFrame<Map<String, Object>> readMap(String path, String splitBy) {
        File file = new File(path);
        ListFrame<Map<String, Object>> listFrame = new ListFrame();
        try (
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader streamReader = new InputStreamReader(fileInputStream);
                BufferedReader br = new BufferedReader(streamReader)
        ) {

            String line = "";
            String[] titles = null;
            if ((line = br.readLine()) != null) {
                titles = line.split(splitBy);
                listFrame.setColumns(Arrays.stream(titles).collect(Collectors.toList()));
            }
            while ((line = br.readLine()) != null) {
                String[] split = line.split(splitBy);
                Map map = new LinkedHashMap();
                for (int i = 0; i < split.length; i++) {
                    map.put(titles[i], split[i]);
                    setListColumns(listFrame, titles[i], split[i]);
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

    /**
     * obtain data list by a property
     *
     * @param fun such as UserInfo::getName
     * @param <T>
     * @return
     */
    public <T> ListFrame<T> get(Function<E, T> fun) {
        ListFrame<T> listFrame = new ListFrame<>();
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

    /**
     * obtain data list by a column name
     *
     * @param columnName
     * @param <T>
     * @return
     */
    public <T> ListFrame<T> get(String columnName) {
        ListFrame<Object> objects = columnData.get(columnName);
        if (null != objects) {
            return (ListFrame<T>) objects;
        }
        ListFrame<Object> listFrame = new ListFrame<>();

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

    public ListFrame<E> replace(String src, String tar) {
        if (data.size() == 0) {
            return data;
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
            numFrame.setColumns(data.getColumns());
            return (ListFrame<E>) numFrame;
        } else {
            throw new RuntimeException("please define a property that you want to replace!");
        }
    }

    public ListFrame<E> replace(String column, String src, String tar) {
        if (data.size() == 0) {
            return data;
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

    /**
     * process data by a implement from DataHandler<E>
     *
     * @param dataProcess
     * @return ListFrame
     */
    public ListFrame<E> handle(DataHandlerInterface<E> dataProcess) {
        ListFrame<E> numFrame = new ListFrame<E>();
        for (E datum : data) {
            numFrame.add(dataProcess.handle(datum));
        }
        numFrame.setColumns(data.getColumns());
        return numFrame;
    }

    /**
     * process data by string expression
     *
     * @param expressions
     * @return ListFrame
     */
    public ListFrame<E> handle(String expressions) {
        List<ExpressionMap> ops = ExpressUtil.getOperates(expressions);
        ListFrame<E> numFrame = new ListFrame<E>();
        for (E datum : data) {
            for (ExpressionMap op : ops) {
                datum = ExpressUtil.operate(datum, op);
            }
            numFrame.add(datum);
        }
        numFrame.setColumns(data.getColumns());
        return numFrame;
    }

    /**
     * process data by lambda expression
     *
     * @param fun
     * @return ListFrame
     */
    public <T> ListFrame<T> handle(Function<E, T> fun) {
        ListFrame<T> numFrame = new ListFrame<T>();
        for (E datum : data) {
            numFrame.add(fun.apply(datum));
        }
        numFrame.setColumns(data.getColumns());
        return numFrame;
    }

    public MapFrame<Object, ListFrame> groupBy(String columnName) {
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
        if (doublePattern.matcher(data.get(0).toString()).find()) {
            Double max = 0.0;
            int index = 0;
            int n = 0;
            for (E datum : data) {
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
        if (doublePattern.matcher(data.get(0).toString()).find()) {
            Double max = 0.0;
            for (E datum : data) {
                double a = Double.valueOf(datum + "");
                if (a > max) {
                    max = a;
                }
            }
            return (T) max;
        } else {
            Integer max = 0;
            for (E datum : data) {
                int a = Integer.valueOf(datum + "");
                if (a > max) {
                    max = a;
                }
            }
            return (T) max;
        }
    }


    public int argmin() {
        if (doublePattern.matcher(data.get(0).toString()).find()) {
            Double max = Double.MAX_VALUE;
            int index = 0;
            int n = 0;
            for (E datum : data) {
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
        if (doublePattern.matcher(data.get(0).toString()).find()) {
            Double max = Double.MAX_VALUE;
            for (E datum : data) {
                double a = Double.valueOf(datum + "");
                if (a < max) {
                    max = a;
                }
            }
            return (T) max;
        } else {
            Integer max = Integer.MAX_VALUE;
            for (E datum : data) {
                int a = Integer.valueOf(datum + "");
                if (a < max) {
                    max = a;
                }
            }
            return (T) max;
        }
    }

    public double avg() {
        double sum = 0.0;
        for (E datum : data) {
            double a = Double.valueOf(datum + "");
            sum += a;
        }
        return sum / data.size();
    }

    public double sum() {
        double sum = 0.0;
        for (E datum : data) {
            double a = Double.valueOf(datum + "");
            sum += a;
        }
        return sum;
    }


    /**
     * convert ListFrame to List
     *
     * @return List
     */
    public List<E> toList() {
        return data;
    }


    /**
     * generate a txt file from ListFrame
     *
     * @param path file path such as "c:/demo.txt"
     */
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

}
