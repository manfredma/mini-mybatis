package com.mini.mybatis.datasource;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * DataSource factory that creates a new connection per request (no pooling).
 */
public class UnpooledDataSourceFactory implements DataSourceFactory {

    protected UnpooledDataSource dataSource;

    public UnpooledDataSourceFactory() {
        this.dataSource = new UnpooledDataSource();
    }

    @Override
    public void setProperties(Properties properties) {
        String driver = properties.getProperty("driver");
        String url = properties.getProperty("url");
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");
        dataSource.setDriver(driver);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }
}
