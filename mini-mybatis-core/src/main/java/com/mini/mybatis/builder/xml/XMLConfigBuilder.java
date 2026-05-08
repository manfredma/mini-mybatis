package com.mini.mybatis.builder.xml;

import com.mini.mybatis.datasource.UnpooledDataSource;
import com.mini.mybatis.session.Configuration;
import com.mini.mybatis.session.Environment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Parses mybatis-config.xml and builds a Configuration object.
 */
public class XMLConfigBuilder {

    private final Configuration configuration;
    private final Document document;

    public XMLConfigBuilder(InputStream inputStream) {
        this.configuration = new Configuration();
        this.document = parseDocument(inputStream);
    }

    public XMLConfigBuilder(Reader reader) {
        this.configuration = new Configuration();
        this.document = parseDocument(readerToStream(reader));
    }

    public Configuration parse() {
        Element root = document.getDocumentElement();
        parseEnvironments(root);
        parseMappers(root);
        return configuration;
    }

    private void parseEnvironments(Element root) {
        NodeList envsList = root.getElementsByTagName("environments");
        if (envsList.getLength() == 0) return;

        Element environments = (Element) envsList.item(0);
        String defaultEnv = environments.getAttribute("default");

        NodeList envList = environments.getElementsByTagName("environment");
        for (int i = 0; i < envList.getLength(); i++) {
            Element env = (Element) envList.item(i);
            String id = env.getAttribute("id");
            if (!id.equals(defaultEnv) && !defaultEnv.isEmpty()) continue;

            DataSource dataSource = parseDataSource(env);
            configuration.setEnvironment(new Environment(id, dataSource));
            break;
        }
    }

    private DataSource parseDataSource(Element environment) {
        NodeList dsList = environment.getElementsByTagName("dataSource");
        if (dsList.getLength() == 0) {
            throw new RuntimeException("No dataSource configured in environment");
        }
        Element dsElement = (Element) dsList.item(0);
        NodeList properties = dsElement.getElementsByTagName("property");

        UnpooledDataSource dataSource = new UnpooledDataSource();
        for (int i = 0; i < properties.getLength(); i++) {
            Element prop = (Element) properties.item(i);
            String name = prop.getAttribute("name");
            String value = prop.getAttribute("value");
            switch (name) {
                case "driver":   dataSource.setDriver(value);   break;
                case "url":      dataSource.setUrl(value);      break;
                case "username": dataSource.setUsername(value); break;
                case "password": dataSource.setPassword(value); break;
            }
        }
        return dataSource;
    }

    private void parseMappers(Element root) {
        NodeList mappersList = root.getElementsByTagName("mappers");
        if (mappersList.getLength() == 0) return;

        Element mappers = (Element) mappersList.item(0);
        NodeList mapperList = mappers.getElementsByTagName("mapper");
        for (int i = 0; i < mapperList.getLength(); i++) {
            Element mapper = (Element) mapperList.item(i);
            String resource = mapper.getAttribute("resource");
            String clazz = mapper.getAttribute("class");

            if (!resource.isEmpty()) {
                InputStream is = getClass().getClassLoader().getResourceAsStream(resource);
                if (is == null) {
                    throw new RuntimeException("Cannot find mapper resource: " + resource);
                }
                new XMLMapperBuilder(is, configuration).parse();
            } else if (!clazz.isEmpty()) {
                try {
                    Class<?> mapperClass = Class.forName(clazz);
                    configuration.addMapper(mapperClass);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Cannot find mapper class: " + clazz, e);
                }
            }
        }
    }

    private Document parseDocument(InputStream inputStream) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(inputStream);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException("Error parsing XML configuration", e);
        }
    }

    private InputStream readerToStream(Reader reader) {
        try {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[1024];
            int n;
            while ((n = reader.read(buf)) != -1) {
                sb.append(buf, 0, n);
            }
            return new java.io.ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Error reading configuration", e);
        }
    }
}
