package com.mini.mybatis.session;

import com.mini.mybatis.builder.xml.XMLConfigBuilder;
import com.mini.mybatis.session.defaults.DefaultSqlSessionFactory;

import java.io.InputStream;
import java.io.Reader;

/**
 * Builds {@link SqlSessionFactory} from various configuration sources.
 */
public class SqlSessionFactoryBuilder {

    public SqlSessionFactory build(Reader reader) {
        return build(reader, null);
    }

    public SqlSessionFactory build(Reader reader, String environment) {
        XMLConfigBuilder parser = new XMLConfigBuilder(reader);
        return build(parser.parse());
    }

    public SqlSessionFactory build(InputStream inputStream) {
        return build(inputStream, null);
    }

    public SqlSessionFactory build(InputStream inputStream, String environment) {
        XMLConfigBuilder parser = new XMLConfigBuilder(inputStream);
        return build(parser.parse());
    }

    public SqlSessionFactory build(Configuration config) {
        return new DefaultSqlSessionFactory(config);
    }
}
