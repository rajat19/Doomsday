package com.paradox.geeks.doomsday.management;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesManager {
    private static Properties properties = null;
    private static final Boolean flag = true;
    private static PropertiesManager propertiesManager;

    private PropertiesManager() throws IOException {
        if (properties == null) {
            InputStream inputStream = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("application.properties");
            properties = new Properties();
            properties.load(inputStream);
        }
    }

    public static PropertiesManager getInstance() {
        if (propertiesManager == null) {
            synchronized (flag) {
                if (propertiesManager == null) {
                    try {
                        propertiesManager = new PropertiesManager();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return propertiesManager;
    }

    public String getProperty(String propertyName) {
        return propertiesManager.getProperty(propertyName);
    }
}
