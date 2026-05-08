package com.mini.mybatis.session;

import javax.sql.DataSource;

/**
 * Groups the DataSource and transaction factory under an environment id.
 */
public class Environment {

    private final String id;
    private final DataSource dataSource;

    public Environment(String id, DataSource dataSource) {
        this.id = id;
        this.dataSource = dataSource;
    }

    public String getId() { return id; }
    public DataSource getDataSource() { return dataSource; }
}
