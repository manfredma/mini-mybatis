package com.mini.mybatis.datasource;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Factory interface for creating DataSource instances from configuration properties.
 */
public interface DataSourceFactory {

    void setProperties(Properties properties);

    DataSource getDataSource();
}
