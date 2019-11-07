package com.baqi;

import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BaqiApplication {
    public static void run(Class clazz, Object[] args) {
        Properties properties = getProperties();
        String port = properties.getProperty("server.port");
        if (port == null) {
            port = "8080";
        }
        String contextPath = properties.getProperty("server.contextPath");
        if (contextPath == null) {
            contextPath = "";
        } else if (!contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }
        //创建Tomcat实例
        Tomcat tomcat = new Tomcat();
        tomcat.setHostname("localhost");
        tomcat.setPort(Integer.valueOf(port));
        //设置 Context
        //server.xml - <Context docBase="" path="/" /></Host>
        String webapp = System.getProperty("user.dir") + File.separator
                + "src" + File.separator + "main" + File.separator + "resources";
        //设置项目名
//        String contextPath = "/mySpring";
        try {
            tomcat.addWebapp(contextPath, webapp);
            // 添加Servlet到tomcat 容器
            DispatchServlet servlet = new DispatchServlet();
            servlet.setStartClass(clazz);
            servlet.init();
            Wrapper wrapper = tomcat.addServlet(contextPath, "dispatchServlet", servlet);
            wrapper.addMapping("/");
            //启动 tomcat 服务器
            tomcat.start();
            //强制 Tomcat Server 等待，避免main线程执行结束关闭
            tomcat.getServer().await();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("tomcat启动失败");
        }
    }

    public static Properties getProperties() {
        InputStream is = BaqiApplication.class.getResourceAsStream("/application.properties");
        Properties properties = new Properties();
        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
