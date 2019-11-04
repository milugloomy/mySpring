package com.baqi.util;

public class ResEntity {
    private String code;
    private String errMsg;
    private Object body;

    public ResEntity(){
        this.code = "0000";
        this.errMsg = "操作成功";
    }

    public ResEntity(Object body){
        this.code = "0000";
        this.body = body;
        this.errMsg = "操作成功";
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}

