package com.baqi;

import com.baqi.annotation.BQAutowired;
import com.baqi.annotation.BQComponentScan;
import com.baqi.annotation.BQController;
import com.baqi.annotation.BQService;

import java.io.File;
import java.lang.reflect.Field;
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
    public static void main(String[] args) {
        BaqiStarter baqiStarter = new BaqiStarter();
        baqiStarter.init();
    }

    private List<String> classNameList = new ArrayList<>();
    private Map<String, Object> instanceMap = new HashMap<>();

    private void init() {
        //获取配置
        String path = getScanPackage();
        //扫描包
        scanClass(path);
        System.out.println(classNameList);
        //实例化
        instance();
        System.out.println(instanceMap);
        //依赖注入
        autowired();
        //url和method映射关系
        handlerMapping();
        System.out.println("Spring 初始化完成");
    }

    private String getScanPackage() {
        String path;
        BQComponentScan annotation = this.getClass().getAnnotation(BQComponentScan.class);
        if (annotation != null) {
            path = annotation.value();
        } else {
            path = this.getClass().getPackage().getName();
        }
        return path;
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
                    // service上有value
                    if (!"".equals(service.value())) {
                        String beanName = service.value();
                        instanceMap.put(beanName, object);
                    }
                    // service上没有value
                    else {
                        Class<?>[] interfaces = clazz.getInterfaces();
                        for (Class<?> i : interfaces) {
                            String beanName = Util.lowerFirst(i.getSimpleName());
                            instanceMap.put(beanName, object);
                        }
                    }
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
                if (field.isAnnotationPresent(BQAutowired.class)) {

                }
            }
        });
    }

    private void handlerMapping() {
    }


    static class Util {
        public static String lowerFirst(String str) {
            char[] cs = str.toCharArray();
            cs[0] += 32;
            return String.valueOf(cs);
        }
    }
}
