package com.zqazfl.common.entity;

import java.io.Serializable;

public class CustomResult<T> implements Serializable {

    private static final long serialVersionUID = 4866226341970708302L;

    private T data;

    public CustomResult(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
