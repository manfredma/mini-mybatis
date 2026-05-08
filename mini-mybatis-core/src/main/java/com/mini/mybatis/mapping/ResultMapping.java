package com.mini.mybatis.mapping;

/**
 * Describes a single column-to-property mapping within a &lt;resultMap&gt;.
 */
public class ResultMapping {

    private String property;
    private String column;
    private Class<?> javaType;

    private ResultMapping() {}

    public String getProperty() { return property; }
    public String getColumn() { return column; }
    public Class<?> getJavaType() { return javaType; }

    public static class Builder {
        private final ResultMapping resultMapping = new ResultMapping();

        public Builder(String property, String column) {
            resultMapping.property = property;
            resultMapping.column = column;
        }

        public Builder javaType(Class<?> javaType) {
            resultMapping.javaType = javaType;
            return this;
        }

        public ResultMapping build() {
            return resultMapping;
        }
    }
}
