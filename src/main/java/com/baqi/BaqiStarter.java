package com.baqi;

import com.baqi.annotation.*;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.ServletException;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 *      ┌─┐       ┌─┐
 *   ┌──┘ ┴───────┘ ┴──┐
 *   │                 │
 *   │       ───       │
 *   │  ─┬┘       └┬─  │
 *   │                 │
 *   │       ─┴─       │
 *   │                 │
 *   └───┐         ┌───┘
 *       │         │
 *       │         │
 *       │         │
 *       │         └──────────────┐
 *       │                        │
 *       │                        ├─┐
 *       │                        ┌─┘
 *       │                        │
 *       └─┐  ┐  ┌───────┬──┐  ┌──┘
 *         │ ─┤ ─┤       │ ─┤ ─┤
 *         └──┴──┘       └──┴──┘
 *                神兽保佑
 *               代码无BUG!
 */
@BQComponentScan("com.baqi")
public class BaqiStarter {
    public static void main(String[] args) throws ServletException, LifecycleException {
        //确定classes 目录绝对路径
        String calssesPath = System.getProperty("user.dir") +
                File.separator + "target" + File.separator + "classes";
        System.out.println(calssesPath);
        //创建Tomcat实例
        Tomcat tomcat = new Tomcat();
        tomcat.setHostname("localhost");
        tomcat.setPort(9527);
        //设置 Context
        //server.xml - <Context docBase="" path="/" /></Host>
        String webapp = System.getProperty("user.dir") + File.separator
                + "src" + File.separator + "main" + File.separator + "resources";
        //设置项目名
        String contextPath = "/mySpring";
        tomcat.addWebapp(contextPath, webapp);
        // 添加Servlet到tomcat 容器
        DispatchServlet servlet = new DispatchServlet();
        servlet.init();
        Wrapper wrapper = tomcat.addServlet(contextPath, "dispatchServlet", servlet);
        wrapper.addMapping("/");
        //启动 tomcat 服务器
        tomcat.start();
        //强制 Tomcat Server 等待，避免main线程执行结束关闭
        tomcat.getServer().await();
    }
}
