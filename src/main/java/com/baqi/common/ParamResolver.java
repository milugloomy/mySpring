package com.baqi.common;

import com.baqi.annotation.BQRequestParam;
import com.sun.xml.internal.ws.org.objectweb.asm.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ParamResolver {

    public List<ParamWrapper> resolve(Method method) {
        Parameter[] parameters = method.getParameters();
        // 先判断是否全部有RequestParam注解
        Boolean allAnnotated = true;
        for (Parameter param : parameters) {
            if (param.getType() == HttpServletRequest.class
                    || param.getType() == HttpServletResponse.class
                    || param.getType() == HttpSession.class) {
                continue;
            }
            if (param.getAnnotation(BQRequestParam.class) == null) {
                allAnnotated = false;
                break;
            }
        }

        List<ParamWrapper> list = new ArrayList<>();
        if (allAnnotated) {
            for (Parameter param : parameters) {
                BQRequestParam annotation = param.getAnnotation(BQRequestParam.class);
                if (annotation != null){
                    list.add(new ParamWrapper(annotation.value(), param.getType()));
                } else {
                    list.add(new ParamWrapper(param.getName(), param.getType()));
                }
            }
        }
        // 若不是全部参数有注解，则使用ASM解析字节码
        else {
            String[] methodParams = getMethodParams(method);
            for (int i = 0; i < methodParams.length; i++) {
                list.add(new ParamWrapper(methodParams[i], parameters[i].getType()));
            }
        }
        return list;
    }

    // ASM读字节码方式获取方法参数名
    private String[] getMethodParams(Method method) {
        String[] methodParams = new String[method.getParameterCount()];

        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        boolean isStatic = Modifier.isStatic(method.getModifiers());

        ClassReader classReader = null;
        try {
            classReader = new ClassReader(className);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        classReader.accept(new ClassAdapter(classWriter) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
                Type[] argTypes = Type.getArgumentTypes(desc);
                // 名字和参数类型都一样表示同一个方法
                if (methodName.equals(name) && matchParamTypes(argTypes, parameterTypes)) {
                    return new MethodAdapter(methodVisitor) {
                        @Override
                        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                            //这个name就是参数名
                            // 静态方法
                            if (isStatic) {
                                methodParams[index] = name;
                            }
                            // 非静态方法，第一个参数是this，要过滤掉
                            else if (index > 0) {
                                methodParams[index - 1] = name;
                            }
                            super.visitLocalVariable(name, desc, signature, start, end, index);
                        }
                    };
                }
                return methodVisitor;
            }
        }, 0);

        return methodParams;
    }

    private boolean matchParamTypes(Type[] types, Class<?>[] parameterTypes) {
        if (types.length != parameterTypes.length) {
            return false;
        }
        for (int i = 0; i < types.length; i++) {
            if (!Type.getType(parameterTypes[i]).equals(types[i])) {
                return false;
            }
        }
        return true;
    }


    public Object packParam(ParamWrapper paramWrapper, Map<String,String[]> parameterMap) {
        String param = parameterMap.get(paramWrapper.getName())[0];
        // Integer类型
        if(paramWrapper.getType() == Integer.class || paramWrapper.getType() == int.class) {
            return Integer.valueOf(param);
        }
        // Date类型
        if(paramWrapper.getType() == Date.class) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                return sdf.parse(param);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }
        // 其他很多类型
        //...
        //...
        return param;
    }
}
