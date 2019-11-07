package com.baqi;

import com.alibaba.fastjson.JSONObject;
import com.baqi.annotation.*;
import com.baqi.common.ParamResolver;
import com.baqi.common.ParamWrapper;
import com.baqi.common.RequestHandler;
import com.baqi.util.ResEntity;
import com.baqi.util.Util;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DispatchServlet extends HttpServlet {

    // 方法参数列表的包装
    private ParamResolver paramResolver = new ParamResolver();
    // 存储已扫描到的class
    private List<String> classNameList = new ArrayList<>();
    // 存储实例
    private Map<String, Object> instanceMap = new HashMap<>();
    // 存储映射
    private Map<String, RequestHandler> handlerMap = new HashMap<>();

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
        //初始化
        postConstruct();
        //初始化完成
        System.out.println("Spring 初始化完成");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        Map<String, String[]> parameterMap = req.getParameterMap();

        RequestHandler handler = handlerMap.get(url);
        if(handler != null) {
            Method method = handler.getMethod();
            // 封装参数
            Object[] params = new Object[method.getParameterCount()];
            List<ParamWrapper> paramWrapperList = handler.getParamWrapperList();
            for (int i = 0; i < paramWrapperList.size(); i++) {
                ParamWrapper paramWrapper = paramWrapperList.get(i);
                if (paramWrapper.getType() == HttpServletRequest.class) {
                    params[i] = req;
                } else if (paramWrapper.getType() == HttpServletResponse.class) {
                    params[i] = resp;
                } else if (paramWrapper.getType() == HttpSession.class) {
                    params[i] = req.getSession();
                } else {
                    params[i] = paramResolver.packParam(paramWrapper, parameterMap);
                }
            }
            // 执行方法
            try {
                final Object ret = method.invoke(handler.getClazz(), params);
                resp.getWriter().print(JSONObject.toJSONString(ret));
            } catch (Exception e) {
                e.printStackTrace();
                ResEntity resEntity = new ResEntity();
                resEntity.setCode("500");
                resEntity.setErrMsg(e.getMessage());
                resp.getWriter().print(JSONObject.toJSONString(resEntity));
            }
        }
        // 返回404
        else {
            resp.setStatus(404);
            ResEntity resEntity = new ResEntity();
            resEntity.setCode("404");
            resEntity.setErrMsg("url未找到");
            resp.getWriter().print(JSONObject.toJSONString(resEntity));
        }
    }


    private String getScanPackage() {
        // 获取带main方法的启动类
        for (Entry<Thread, StackTraceElement[]> stack : Thread.getAllStackTraces().entrySet()) {
            if ("main".equals(stack.getKey().getName())) {
                StackTraceElement[] stackTraceElements = stack.getValue();
                StackTraceElement stackTraceElement = stackTraceElements[stackTraceElements.length - 1];
                String className = stackTraceElement.getClassName();
                try {
                    Class<?> clazz = Class.forName(className);
                    BQComponentScan bqComponentScan = clazz.getAnnotation(BQComponentScan.class);
                    String value;
                    if (bqComponentScan == null) {
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
        throw new RuntimeException("获取包扫描路径失败");
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
                    requestUrl = requestUrl.replaceAll("/+", "/");

                    List<ParamWrapper> paramWrapperList = paramResolver.resolve(method);
                    handlerMap.put(requestUrl, new RequestHandler(entry.getValue(), method, paramWrapperList));
                }
            }
        });
    }

    private void postConstruct() {
        instanceMap.entrySet().forEach(entry -> {
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if(method.isAnnotationPresent(BQPostConstruct.class)) {
                    try {
                        method.invoke(instance, new Object[]{});
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
