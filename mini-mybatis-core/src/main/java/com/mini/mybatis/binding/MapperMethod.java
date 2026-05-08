package com.mini.mybatis.binding;

import com.mini.mybatis.mapping.MappedStatement;
import com.mini.mybatis.mapping.SqlCommandType;
import com.mini.mybatis.session.Configuration;
import com.mini.mybatis.session.SqlSession;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Resolves and executes the mapped statement for a single Mapper method.
 */
public class MapperMethod {

    private final SqlCommand command;
    private final MethodSignature method;

    public MapperMethod(Class<?> mapperInterface, Method method, Configuration config) {
        this.command = new SqlCommand(config, mapperInterface, method);
        this.method = new MethodSignature(method);
    }

    public Object execute(SqlSession sqlSession, Object[] args) {
        Object result;
        switch (command.getType()) {
            case INSERT:
                result = sqlSession.insert(command.getName(), method.convertArgsToSqlCommandParam(args));
                break;
            case UPDATE:
                result = sqlSession.update(command.getName(), method.convertArgsToSqlCommandParam(args));
                break;
            case DELETE:
                result = sqlSession.delete(command.getName(), method.convertArgsToSqlCommandParam(args));
                break;
            case SELECT:
                if (method.returnsMany()) {
                    result = sqlSession.selectList(command.getName(), method.convertArgsToSqlCommandParam(args));
                } else {
                    result = sqlSession.selectOne(command.getName(), method.convertArgsToSqlCommandParam(args));
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown execution method for: " + command.getName());
        }
        return result;
    }

    /**
     * Resolves the SQL command type and statement ID for a mapper method.
     */
    public static class SqlCommand {
        private final String name;
        private final SqlCommandType type;

        public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
            String statementId = mapperInterface.getName() + "." + method.getName();
            MappedStatement ms = configuration.getMappedStatement(statementId);
            this.name = ms.getId();
            this.type = ms.getSqlCommandType();
        }

        public String getName() { return name; }
        public SqlCommandType getType() { return type; }
    }

    /**
     * Reflects on the method signature to determine return type and parameter handling.
     */
    public static class MethodSignature {
        private final boolean returnsMany;
        private final Class<?> returnType;

        public MethodSignature(Method method) {
            this.returnType = method.getReturnType();
            this.returnsMany = List.class.isAssignableFrom(returnType);
        }

        public boolean returnsMany() { return returnsMany; }
        public Class<?> getReturnType() { return returnType; }

        public Object convertArgsToSqlCommandParam(Object[] args) {
            if (args == null || args.length == 0) {
                return null;
            }
            if (args.length == 1) {
                return args[0];
            }
            // Multiple params: return as array; full @Param support can be added later
            return args;
        }
    }
}
