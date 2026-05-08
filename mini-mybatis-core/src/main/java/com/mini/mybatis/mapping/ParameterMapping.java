package com.mini.mybatis.mapping;

/**
 * Describes a single '#{property}' placeholder in a SQL string.
 */
public class ParameterMapping {

    private String property;
    private Class<?> javaType;

    private ParameterMapping() {}

    public String getProperty() { return property; }
    public Class<?> getJavaType() { return javaType; }

    public static class Builder {
        private final ParameterMapping parameterMapping = new ParameterMapping();

        public Builder(String property) {
            parameterMapping.property = property;
        }

        public Builder javaType(Class<?> javaType) {
            parameterMapping.javaType = javaType;
            return this;
        }

        public ParameterMapping build() {
            return parameterMapping;
        }
    }
}
