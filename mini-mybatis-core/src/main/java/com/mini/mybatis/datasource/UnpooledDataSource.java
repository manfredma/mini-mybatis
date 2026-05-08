package com.mini.mybatis.datasource;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Non-pooled DataSource that opens a new JDBC connection on every call.
 */
public class UnpooledDataSource implements DataSource {

    private ClassLoader driverClassLoader;
    private String driver;
    private String url;
    private String username;
    private String password;

    @Override
    public Connection getConnection() throws SQLException {
        return doGetConnection(username, password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return doGetConnection(username, password);
    }

    private Connection doGetConnection(String username, String password) throws SQLException {
        try {
            Class.forName(driver, true, driverClassLoader != null ? driverClassLoader : getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new SQLException("Could not load JDBC driver: " + driver, e);
        }
        Properties props = new Properties();
        if (username != null) props.setProperty("user", username);
        if (password != null) props.setProperty("password", password);
        return DriverManager.getConnection(url, props);
    }

    @Override
    public PrintWriter getLogWriter() { return null; }
    @Override
    public void setLogWriter(PrintWriter out) {}
    @Override
    public void setLoginTimeout(int seconds) {}
    @Override
    public int getLoginTimeout() { return 0; }
    @Override
    public Logger getParentLogger() { return null; }
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException { throw new SQLException("Not a wrapper."); }
    @Override
    public boolean isWrapperFor(Class<?> iface) { return false; }

    public void setDriver(String driver) { this.driver = driver; }
    public void setUrl(String url) { this.url = url; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setDriverClassLoader(ClassLoader driverClassLoader) { this.driverClassLoader = driverClassLoader; }

    public String getDriver() { return driver; }
    public String getUrl() { return url; }
    public String getUsername() { return username; }
}
