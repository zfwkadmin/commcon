package com.zqazfl.common.entity;

import java.io.Serializable;

/**
 * @Description 公共返回数据格式
 * @Author lvming
 * @Date 2019/9/2 13:33
 * @Version 1.0
 */
public final class ResultEntity<T> implements Serializable {
    private int status = 1;
    private String errorCode = "";
    private String errorMsg = "";
    private T data;
    private static final long serialVersionUID = 1L;

    public ResultEntity(){

    }

    public ResultEntity(T data){
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
