package com.zqazfl.common.handler;

import com.zqazfl.common.entity.ResultEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.Logger;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public ResultEntity handleException(Exception e){

        ResultEntity resultentity=new ResultEntity();
        resultentity.setErrorCode("500");
        resultentity.setStatus(-1);
        resultentity.setErrorMsg(e.getMessage());
        //将错误堆栈信息一并记录日志
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(baos));
        String exception = baos.toString();
        Logger.getGlobal().info("当前异常错误:" +exception);

        return resultentity;
    }
}
