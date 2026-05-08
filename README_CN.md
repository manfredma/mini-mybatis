# mini-mybatis

用 Java 8 实现的精简版 MyBatis，涵盖 SqlSession 生命周期、一二级缓存、动态代理 Mapper、插件拦截器链等核心设计。模块结构与官方 MyBatis 保持一致。

## 功能特性

- **SqlSession 生命周期** — 开启、执行 SQL、提交/回滚、关闭
- **Executor 执行器链** — `SimpleExecutor`（JDBC 执行）被 `CachingExecutor`（二级缓存）装饰
- **一级缓存** — `BaseExecutor` 内置 `PerpetualCache`，SqlSession 级别隔离
- **二级缓存** — namespace 级别 `TransactionalCache`，`SqlSession.commit()` 后生效
- **动态代理 Mapper** — `MapperProxy` + `MapperRegistry`，无需手写 DAO 实现类
- **XML 解析** — `XMLConfigBuilder`（mybatis-config.xml）+ `XMLMapperBuilder`（Mapper XML）
- **插件拦截器链** — `@Intercepts`/`@Signature` 注解 + `InterceptorChain.pluginAll()`

## 模块结构

```
mini-mybatis
└── mini-mybatis-core
    └── com.mini.mybatis
        ├── binding/        # MapperProxy、MapperProxyFactory、MapperRegistry、MapperMethod
        ├── builder/xml/    # XMLConfigBuilder、XMLMapperBuilder、StaticSqlSource
        ├── cache/          # Cache 接口、CacheKey、PerpetualCache
        │   └── decorators/ # TransactionalCache、TransactionalCacheManager、LruCache、FifoCache
        ├── datasource/     # UnpooledDataSource、UnpooledDataSourceFactory
        ├── executor/       # Executor、BaseExecutor、SimpleExecutor、CachingExecutor
        │   ├── resultset/  # ResultSetHandler、DefaultResultSetHandler
        │   └── statement/  # StatementHandler、PreparedStatementHandler
        ├── mapping/        # MappedStatement、BoundSql、SqlSource、ResultMap、SqlCommandType
        ├── plugin/         # Interceptor、InterceptorChain、Plugin、Invocation、@Intercepts、@Signature
        └── session/        # SqlSession、SqlSessionFactory、SqlSessionFactoryBuilder、Configuration
            └── defaults/   # DefaultSqlSession、DefaultSqlSessionFactory
```

## 快速上手

### 1. 引入依赖

```xml
<dependency>
    <groupId>com.mini</groupId>
    <artifactId>mini-mybatis-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 编写 mybatis-config.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
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

### 3. 定义 Mapper 接口和 XML

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

### 4. 使用 SqlSession

```java
SqlSessionFactory factory = new SqlSessionFactoryBuilder()
        .build(Resources.getResourceAsStream("mybatis-config.xml"));

try (SqlSession session = factory.openSession()) {
    UserMapper mapper = session.getMapper(UserMapper.class);
    User user = mapper.selectById(1);
    session.commit();
}
```

### 5. 注册插件

```java
@Intercepts({
    @Signature(type = Executor.class, method = "query",
               args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class PageInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        System.out.println("查询前拦截: " + invocation.getMethod().getName());
        return invocation.proceed();
    }
}

// 在 Configuration 中注册
configuration.addInterceptor(new PageInterceptor());
```

## 整体架构

```
SqlSessionFactoryBuilder
        │
        ▼
SqlSessionFactory ──── Configuration
        │                    │
        ▼                    ├── MapperRegistry（Mapper 注册表）
   SqlSession                ├── MappedStatements（SQL 映射）
        │                    ├── InterceptorChain（插件链）
        ▼                    └── Cache（二级缓存）
   CachingExecutor  ◄── 二级缓存（TransactionalCache，namespace 级别）
        │
        ▼
   BaseExecutor     ◄── 一级缓存（PerpetualCache，SqlSession 级别）
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

## 缓存设计

### 一级缓存（Local Cache）

- 作用域：SqlSession 级别
- 实现：`BaseExecutor` 内置 `PerpetualCache`（HashMap）
- 失效时机：执行 update/insert/delete、手动调用 `clearLocalCache()`、SqlSession 关闭

### 二级缓存（Second Level Cache）

- 作用域：namespace（Mapper）级别，跨 SqlSession 共享
- 实现：`CachingExecutor` 装饰器 + `TransactionalCache`
- 生效时机：`SqlSession.commit()` 后，暂存数据才真正写入缓存
- 开启方式：在 Mapper XML 中添加 `<cache/>` 标签

## 插件机制

mini-mybatis 支持对以下四个核心对象进行拦截：

| 拦截对象 | 说明 |
|---------|------|
| `Executor` | 拦截 query/update 等执行方法 |
| `StatementHandler` | 拦截 SQL 语句准备阶段 |
| `ParameterHandler` | 拦截参数设置阶段 |
| `ResultSetHandler` | 拦截结果集处理阶段 |

## 构建与测试

```bash
mvn clean test -Dsort.skip=true
```

23 个测试全部通过，覆盖：

| 测试类 | 内容 |
|--------|------|
| `CacheKeyTest` | 缓存键相等性与哈希 |
| `TransactionalCacheTest` | 二级缓存提交/回滚语义 |
| `PerpetualCacheTest` | 一级缓存 CRUD |
| `PluginTest` | 拦截器链包装与执行 |
| `SqlSessionTest` | 基于 H2 内存库的端到端 CRUD |

## 运行环境

- Java 8+
- Maven 3.6+
