package cn.langpy.util;

import cn.langpy.core.DataBaseConfig;
import cn.langpy.core.DataBaseType;
import cn.langpy.core.ListFrame;
import cn.langpy.core.TableInfo;
import com.alibaba.druid.pool.DruidDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataBaseUtil {

    static Map<String, DataSource> dataSourceMap = null;

    static {
        dataSourceMap = new ConcurrentHashMap<>();
    }

    public static ListFrame<Map<String, Object>> readSql(String sql, DataBaseConfig dataBaseConfig) {
        DataSource dataSource = getDataSource(dataBaseConfig);
        return readSql(sql,dataSource);
    }

    public static DataSource getDataSource(DataBaseConfig dataBaseConfig) {
        String key = dataBaseConfig.getHost() + dataBaseConfig.getPort() + dataBaseConfig.getDatabase() + dataBaseConfig.getUserName();
        if (dataSourceMap.containsKey(key)) {
            return dataSourceMap.get(key);
        }
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUsername(dataBaseConfig.getUserName());
        dataSource.setPassword(dataBaseConfig.getPassword());
        if (dataBaseConfig.getDatabaseType() == DataBaseType.MYSQL) {
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            dataSource.setUrl(String.format("jdbc:mysql://%s:%s/%s", dataBaseConfig.getHost(), dataBaseConfig.getPort(), dataBaseConfig.getDatabase()));
        } else if (dataBaseConfig.getDatabaseType() == DataBaseType.ORACLE) {
            throw new RuntimeException("unsupported operate!");
        }else {
            throw new RuntimeException("please define DataBaseType!");
        }
        dataSourceMap.put(key, dataSource);
        return dataSource;
    }

    public static ListFrame<Map<String, Object>> readSql(String sql, DataSource dataSource) {
        ListFrame<Map<String, Object>> listFrame = new ListFrame<>();
        PreparedStatement statement;
        try (Connection connection = dataSource.getConnection()) {
            statement = connection.prepareStatement(sql);
            final ResultSetMetaData metaData = statement.getMetaData();
            ResultSet resultSet = statement.executeQuery(sql);
            List<TableInfo> columns = getColumns(metaData);
            while (resultSet.next()) {
                Map<String, Object> map = new HashMap<>();
                for (TableInfo column : columns) {
                    map.put(column.getName(), getColumnValue(resultSet, column));
                }
                listFrame.add(map);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return listFrame;
    }

    private static Object getColumnValue(ResultSet resultSet, TableInfo column) throws SQLException {
        if ("VARCHAR".equals(column.getDataType()) || "TEXT".equals(column.getDataType())) {
            return resultSet.getString(column.getName());
        } else if ("DATETIME".equals(column.getDataType())) {
            return resultSet.getTimestamp(column.getName());
        } else if ("INT".equals(column.getDataType())) {
            return resultSet.getInt(column.getName());
        } else {
            return resultSet.getObject(column.getName());
        }
    }

    private static List<TableInfo> getColumns(ResultSetMetaData metaData) throws SQLException {
        List<TableInfo> colnames = new ArrayList<TableInfo>();
        for (int i = 0; i < metaData.getColumnCount(); i++) {
            String colname = metaData.getColumnName(i + 1);
            String colType = metaData.getColumnTypeName(i + 1);
            TableInfo tableInfo = new TableInfo();
            tableInfo.setName(colname);
            tableInfo.setDataType(colType);
            colnames.add(tableInfo);
        }
        return colnames;
    }
}
