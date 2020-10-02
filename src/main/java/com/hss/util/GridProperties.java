package com.hss.util;

import java.io.IOException;
import java.util.Properties;

public class GridProperties {

    private static Properties properties = null;

    protected static Properties getGridProperties() {
        if (properties == null) {
            properties = new Properties();
            try {
                properties.load(GridProperties.class.getClassLoader()
                        .getResourceAsStream("grid.properties"));
            } catch (IOException e) {
                System.out.println("加载配置文件出错");
            }
        }
        return properties;
    }

    public static final String SCAN_PACKAGE = getGridProperties()
            .getProperty("scanPackage","");
}
