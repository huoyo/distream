package cn.langpy.core;

public class DataBaseConfig {
    private String host;
    private Integer port;
    private String database;
    private String userName;
    private String password;
    private DataBaseType databaseType = DataBaseType.MYSQL;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public DataBaseType getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(DataBaseType databaseType) {
        this.databaseType = databaseType;
    }
}
