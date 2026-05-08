package com.mini.mybatis.session;

import com.mini.mybatis.datasource.UnpooledDataSource;
import com.mini.mybatis.session.defaults.DefaultSqlSessionFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.Assert.*;

public class SqlSessionTest {

    @Rule
    public TestName testName = new TestName();

    private SqlSessionFactory sqlSessionFactory;

    @Before
    public void setUp() throws Exception {
        String dbName = "testdb_" + testName.getMethodName();
        UnpooledDataSource dataSource = new UnpooledDataSource();
        dataSource.setDriver("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(true);
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS t_user");
        stmt.execute("CREATE TABLE t_user (id BIGINT PRIMARY KEY, name VARCHAR(50), age INT)");
        stmt.execute("INSERT INTO t_user VALUES (1, 'Alice', 30)");
        stmt.execute("INSERT INTO t_user VALUES (2, 'Bob', 25)");
        conn.close();

        Configuration configuration = new Configuration();
        configuration.setEnvironment(new Environment("test", dataSource));

        configuration.addMapper(UserMapper.class);
        addMappedStatements(configuration);

        sqlSessionFactory = new DefaultSqlSessionFactory(configuration);
    }

    private void addMappedStatements(Configuration config) {
        com.mini.mybatis.builder.xml.StaticSqlSource selectById =
                new com.mini.mybatis.builder.xml.StaticSqlSource(
                        "SELECT id, name, age FROM t_user WHERE id = ?",
                        java.util.Arrays.asList(
                                new com.mini.mybatis.mapping.ParameterMapping.Builder("id").build()
                        )
                );
        config.addMappedStatement(
                com.mini.mybatis.mapping.MappedStatement.builder(config,
                        "com.mini.mybatis.session.SqlSessionTest$UserMapper.selectById",
                        selectById,
                        com.mini.mybatis.mapping.SqlCommandType.SELECT)
                        .resultType(User.class)
                        .build()
        );

        com.mini.mybatis.builder.xml.StaticSqlSource selectAll =
                new com.mini.mybatis.builder.xml.StaticSqlSource(
                        "SELECT id, name, age FROM t_user",
                        java.util.Collections.emptyList()
                );
        config.addMappedStatement(
                com.mini.mybatis.mapping.MappedStatement.builder(config,
                        "com.mini.mybatis.session.SqlSessionTest$UserMapper.selectAll",
                        selectAll,
                        com.mini.mybatis.mapping.SqlCommandType.SELECT)
                        .resultType(User.class)
                        .build()
        );

        com.mini.mybatis.builder.xml.StaticSqlSource insertUser =
                new com.mini.mybatis.builder.xml.StaticSqlSource(
                        "INSERT INTO t_user (id, name, age) VALUES (?, ?, ?)",
                        java.util.Arrays.asList(
                                new com.mini.mybatis.mapping.ParameterMapping.Builder("id").build(),
                                new com.mini.mybatis.mapping.ParameterMapping.Builder("name").build(),
                                new com.mini.mybatis.mapping.ParameterMapping.Builder("age").build()
                        )
                );
        config.addMappedStatement(
                com.mini.mybatis.mapping.MappedStatement.builder(config,
                        "com.mini.mybatis.session.SqlSessionTest$UserMapper.insertUser",
                        insertUser,
                        com.mini.mybatis.mapping.SqlCommandType.INSERT)
                        .build()
        );
    }

    @Test
    public void testSelectOne() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            User user = session.selectOne(
                    "com.mini.mybatis.session.SqlSessionTest$UserMapper.selectById", 1L);
            assertNotNull(user);
            assertEquals("Alice", user.getName());
            assertEquals(30, user.getAge());
        }
    }

    @Test
    public void testSelectList() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            java.util.List<User> users = session.selectList(
                    "com.mini.mybatis.session.SqlSessionTest$UserMapper.selectAll");
            assertEquals(2, users.size());
        }
    }

    @Test
    public void testInsert() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            User newUser = new User(3L, "Charlie", 20);
            int rows = session.insert(
                    "com.mini.mybatis.session.SqlSessionTest$UserMapper.insertUser", newUser);
            session.commit();
            assertEquals(1, rows);
        }
        try (SqlSession session = sqlSessionFactory.openSession()) {
            User user = session.selectOne(
                    "com.mini.mybatis.session.SqlSessionTest$UserMapper.selectById", 3L);
            assertNotNull(user);
            assertEquals("Charlie", user.getName());
        }
    }

    @Test
    public void testGetMapper() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            assertNotNull(mapper);
            User user = mapper.selectById(1L);
            assertNotNull(user);
            assertEquals("Alice", user.getName());
        }
    }

    public interface UserMapper {
        User selectById(Long id);
        java.util.List<User> selectAll();
        int insertUser(User user);
    }

    public static class User {
        private Long id;
        private String name;
        private int age;

        public User() {}

        public User(Long id, String name, int age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public int getAge() { return age; }
    }
}
