package com.mini.mybatis.mapping;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds result mappings from a &lt;resultMap&gt; XML element.
 */
public class ResultMap {

    private String id;
    private Class<?> type;
    private List<ResultMapping> resultMappings;

    private ResultMap() {}

    public String getId() { return id; }
    public Class<?> getType() { return type; }
    public List<ResultMapping> getResultMappings() { return resultMappings; }

    public static class Builder {
        private final ResultMap resultMap = new ResultMap();

        public Builder(String id, Class<?> type) {
            resultMap.id = id;
            resultMap.type = type;
            resultMap.resultMappings = new ArrayList<>();
        }

        public Builder resultMappings(List<ResultMapping> resultMappings) {
            resultMap.resultMappings = resultMappings;
            return this;
        }

        public ResultMap build() {
            return resultMap;
        }
    }
}
