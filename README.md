# mini-mybatis

A minimal MyBatis implementation in Java 8, covering the core design of SqlSession lifecycle, first/second-level cache, dynamic proxy Mapper, and plugin interceptor chain. Module structure mirrors the official MyBatis layout.

## Features

- **SqlSession lifecycle** — open, execute SQL, commit/rollback, close
- **Executor chain** — `SimpleExecutor` (JDBC) wrapped by `CachingExecutor` (L2 cache)
- **First-level cache** — per-`SqlSession` `PerpetualCache` in `BaseExecutor`
- **Second-level cache** — per-namespace `TransactionalCache`, committed on `SqlSession.commit()`
- **Dynamic proxy Mapper** — `MapperProxy` + `MapperRegistry`, zero boilerplate DAO implementation
- **XML parsing** — `XMLConfigBuilder` (mybatis-config.xml) + `XMLMapperBuilder` (Mapper XML)
- **Plugin interceptor chain** — `@Intercepts`/`@Signature` annotations, `InterceptorChain.pluginAll()`

## Module Structure

```
mini-mybatis
└── mini-mybatis-core
    └── com.mini.mybatis
        ├── binding/        # MapperProxy, MapperProxyFactory, MapperRegistry, MapperMethod
        ├── builder/xml/    # XMLConfigBuilder, XMLMapperBuilder, StaticSqlSource
        ├── cache/          # Cache, CacheKey, PerpetualCache
        │   └── decorators/ # TransactionalCache, TransactionalCacheManager, LruCache, FifoCache
        ├── datasource/     # UnpooledDataSource, UnpooledDataSourceFactory
        ├── executor/       # Executor, BaseExecutor, SimpleExecutor, CachingExecutor
        │   ├── resultset/  # ResultSetHandler, DefaultResultSetHandler
        │   └── statement/  # StatementHandler, PreparedStatementHandler
        ├── mapping/        # MappedStatement, BoundSql, SqlSource, ResultMap, SqlCommandType
        ├── plugin/         # Interceptor, InterceptorChain, Plugin, Invocation, @Intercepts, @Signature
        └── session/        # SqlSession, SqlSessionFactory, SqlSessionFactoryBuilder, Configuration
            └── defaults/   # DefaultSqlSession, DefaultSqlSessionFactory
```

## Quick Start

### 1. Add dependency

```xml
<dependency>
    <groupId>com.mini</groupId>
    <artifactId>mini-mybatis-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Write mybatis-config.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration PUBLIC "-//mini.mybatis.org//DTD Config 3.0//EN" "">
<configuration>
    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC"/>
            <dataSource type="UNPOOLED">
                <property name="driver" value="org.h2.Driver"/>
                <property name="url" value="jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"/>
                <property name="username" value="sa"/>
                <property name="password" value=""/>
            </dataSource>
        </environment>
    </environments>
    <mappers>
        <mapper resource="mapper/UserMapper.xml"/>
    </mappers>
</configuration>
```

### 3. Define Mapper interface and XML

```java
public interface UserMapper {
    User selectById(int id);
    int insert(User user);
}
```

```xml
<mapper namespace="com.example.UserMapper">
    <select id="selectById" resultType="com.example.User">
        SELECT id, name FROM user WHERE id = #{id}
    </select>
    <insert id="insert">
        INSERT INTO user (name) VALUES (#{name})
    </insert>
</mapper>
```

### 4. Use SqlSession

```java
SqlSessionFactory factory = new SqlSessionFactoryBuilder()
        .build(Resources.getResourceAsStream("mybatis-config.xml"));

try (SqlSession session = factory.openSession()) {
    UserMapper mapper = session.getMapper(UserMapper.class);
    User user = mapper.selectById(1);
    session.commit();
}
```

### 5. Register a Plugin

```java
@Intercepts({
    @Signature(type = Executor.class, method = "query",
               args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class PageInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        System.out.println("Before query: " + invocation.getMethod().getName());
        return invocation.proceed();
    }
}

// Register in Configuration
configuration.addInterceptor(new PageInterceptor());
```

## Architecture

```
SqlSessionFactoryBuilder
        │
        ▼
SqlSessionFactory ──── Configuration
        │                    │
        ▼                    ├── MapperRegistry
   SqlSession                ├── MappedStatements
        │                    ├── InterceptorChain
        ▼                    └── Cache (L2)
   CachingExecutor  ◄── L2 Cache (TransactionalCache)
        │
        ▼
   BaseExecutor     ◄── L1 Cache (PerpetualCache / localCache)
        │
        ▼
   SimpleExecutor
        │
        ▼
PreparedStatementHandler
        │
        ▼
      JDBC
```

## Build & Test

```bash
mvn clean test -Dsort.skip=true
```

All 23 tests pass across:
- `CacheKeyTest` — cache key equality and hashing
- `TransactionalCacheTest` — L2 cache commit/rollback semantics
- `PerpetualCacheTest` — L1 cache CRUD
- `PluginTest` — interceptor chain wrapping and invocation
- `SqlSessionTest` — end-to-end CRUD with H2 in-memory database

## Requirements

- Java 8+
- Maven 3.6+
