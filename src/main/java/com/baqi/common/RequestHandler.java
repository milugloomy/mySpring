package com.baqi.common;

import java.lang.reflect.Method;
import java.util.List;

public class RequestHandler {
    private Object clazz;
    private Method method;
    private List<ParamWrapper> paramWrapperList;

    public RequestHandler(Object clazz, Method method, List<ParamWrapper> paramWrapperList) {
        this.clazz = clazz;
        this.method = method;
        this.paramWrapperList = paramWrapperList;
    }

    public Object getClazz() {
        return clazz;
    }

    public void setClazz(Object clazz) {
        this.clazz = clazz;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public List<ParamWrapper> getParamWrapperList() {
        return paramWrapperList;
    }

    public void setParamWrapperList(List<ParamWrapper> paramWrapperList) {
        this.paramWrapperList = paramWrapperList;
    }
}
