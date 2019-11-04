package com.baqi;

import com.baqi.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DispatchServlet extends HttpServlet {

    private List<String> classNameList = new ArrayList<>();
    private Map<String, Object> instanceMap = new HashMap<>();
    private Map<String, Method> handlerMap = new HashMap<>();

    @Override
    public void init() throws ServletException {
        //获取配置
        String path = getScanPackage();
        //扫描包
        scanClass(path);
        //实例化
        instance();
        //依赖注入
        autowired();
        //url和method映射关系
        handlerMapping();
        //初始化完成
        System.out.println("Spring 初始化完成");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "");
        Map<String, String[]> parameterMap = req.getParameterMap();

        for(Entry<String, Method> entry: handlerMap.entrySet()) {
            if(entry.getKey().equals(url)) {
                Method method = entry.getValue();
                Class<?>[] parameterTypes = method.getParameterTypes();
                Object[] params = new Object[parameterTypes.length];


                method.invoke(controller, params);
            }
        }
    }


    private String getScanPackage() {
        // 获取带main方法的启动类
        for (Entry<Thread, StackTraceElement[]> stack: Thread.getAllStackTraces().entrySet()){
            if("main".equals(stack.getKey().getName())) {
                StackTraceElement[] stackTraceElements = stack.getValue();
                StackTraceElement stackTraceElement = stackTraceElements[stackTraceElements.length - 1];
                String className = stackTraceElement.getClassName();
                try {
                    Class<?> clazz = Class.forName(className);
                    BQComponentScan bqComponentScan = clazz.getAnnotation(BQComponentScan.class);
                    String value;
                    if(bqComponentScan == null){
                        value = clazz.getPackage().getName();
                        return value;
                    } else {
                        value = bqComponentScan.value();
                        return value;
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
        throw new RuntimeException("初始化失败");
    }

    private void scanClass(String path) {
        URL url = this.getClass().getClassLoader().getResource(path.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        // 递归查所有class
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                scanClass(path + "." + file.getName());
            } else {
                String className = path + "." + file.getName().replace(".class", "");
                classNameList.add(className);
            }
        }
    }

    private void instance() {
        for (String className : classNameList) {
            try {
                //获取类
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(BQController.class)) {
                    //实例化
                    Object object = clazz.newInstance();
                    //首字母小写
                    String beanName = Util.lowerFirst(clazz.getSimpleName());
                    // 放入ioc
                    instanceMap.put(beanName, object);
                } else if (clazz.isAnnotationPresent(BQService.class)) {
                    //实例化
                    Object object = clazz.newInstance();
                    BQService service = clazz.getAnnotation(BQService.class);
                    String beanName = service.value();
                    // service上有value
                    if ("".equals(beanName)) {
                        beanName = Util.lowerFirst(clazz.getSimpleName());
                    }
                    instanceMap.put(beanName, object);
                } else {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    private void autowired() {
        instanceMap.entrySet().forEach(entry -> {
            Field[] fileds = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fileds) {
                BQAutowired autowired = field.getAnnotation(BQAutowired.class);
                if (autowired != null) {
                    String beanName = autowired.value().trim();
                    // 为空注入类名
                    if ("".equals(beanName)) {
                        beanName = field.getName();
                    }
                    field.setAccessible(true); //如果是私有属性，设置访问权限
                    // 反射设置值
                    try {
                        field.set(entry.getValue(), instanceMap.get(beanName));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void handlerMapping() {
        instanceMap.entrySet().forEach(entry -> {
            Class<?> clazz = entry.getValue().getClass();
            if (clazz.isAnnotationPresent(BQController.class)) {
                String url = "";
                // 若controller有RequestMapping注解
                if (clazz.isAnnotationPresent(BQRequestMapping.class)) {
                    BQRequestMapping requestMapping = clazz.getAnnotation(BQRequestMapping.class);
                    url = requestMapping.value();
                }
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    BQRequestMapping requestMapping = method.getAnnotation(BQRequestMapping.class);
                    String requestUrl = "/" + url + requestMapping.value();
                    requestUrl.replaceAll("/+", "/");
                    handlerMap.put(requestUrl, method);
                }
            }
        });
    }


    static class Util {
        public static String lowerFirst(String str) {
            char[] cs = str.toCharArray();
            cs[0] += 32;
            return String.valueOf(cs);
        }
    }

}
