package com.mini.mybatis.builder.xml;

import com.mini.mybatis.mapping.*;
import com.mini.mybatis.session.Configuration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a Mapper XML file and registers MappedStatements into Configuration.
 */
public class XMLMapperBuilder {

    private static final Pattern PARAM_PATTERN = Pattern.compile("#\\{(\\w+)\\}");

    private final InputStream inputStream;
    private final Configuration configuration;

    public XMLMapperBuilder(InputStream inputStream, Configuration configuration) {
        this.inputStream = inputStream;
        this.configuration = configuration;
    }

    public void parse() {
        Document doc = parseDocument();
        Element root = doc.getDocumentElement();
        String namespace = root.getAttribute("namespace");

        // Register mapper interface if available
        tryRegisterMapperInterface(namespace);

        // Parse select | insert | update | delete
        parseSqlStatements(root, namespace, "select",  SqlCommandType.SELECT);
        parseSqlStatements(root, namespace, "insert",  SqlCommandType.INSERT);
        parseSqlStatements(root, namespace, "update",  SqlCommandType.UPDATE);
        parseSqlStatements(root, namespace, "delete",  SqlCommandType.DELETE);
    }

    private void tryRegisterMapperInterface(String namespace) {
        try {
            Class<?> mapperClass = Class.forName(namespace);
            if (!configuration.hasMapper(mapperClass)) {
                configuration.addMapper(mapperClass);
            }
        } catch (ClassNotFoundException e) {
            // namespace is not a class — that's fine
        }
    }

    private void parseSqlStatements(Element root, String namespace, String tag, SqlCommandType commandType) {
        NodeList nodes = root.getElementsByTagName(tag);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            String id = element.getAttribute("id");
            String resultType = element.getAttribute("resultType");
            String sql = element.getTextContent().trim();

            String statementId = namespace + "." + id;
            List<ParameterMapping> parameterMappings = new ArrayList<>();
            String parsedSql = parseSqlParameters(sql, parameterMappings);

            SqlSource sqlSource = new StaticSqlSource(parsedSql, parameterMappings);
            MappedStatement.Builder builder = MappedStatement.builder(configuration, statementId, sqlSource, commandType);

            if (!resultType.isEmpty()) {
                try {
                    builder.resultType(resolveClass(resultType));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Cannot resolve resultType: " + resultType, e);
                }
            }

            configuration.addMappedStatement(builder.build());
        }
    }

    /**
     * Replaces #{property} placeholders with ? and collects ParameterMappings.
     */
    private String parseSqlParameters(String sql, List<ParameterMapping> parameterMappings) {
        Matcher matcher = PARAM_PATTERN.matcher(sql);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String property = matcher.group(1);
            parameterMappings.add(new ParameterMapping.Builder(property).build());
            matcher.appendReplacement(sb, "?");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private Class<?> resolveClass(String alias) throws ClassNotFoundException {
        switch (alias) {
            case "int":     return int.class;
            case "long":    return long.class;
            case "string":
            case "String":  return String.class;
            case "Integer": return Integer.class;
            case "Long":    return Long.class;
            default:        return Class.forName(alias);
        }
    }

    private Document parseDocument() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(inputStream);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException("Error parsing Mapper XML", e);
        }
    }
}
