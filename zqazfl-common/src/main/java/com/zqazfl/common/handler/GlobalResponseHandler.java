package com.zqazfl.common.handler;

import com.alibaba.fastjson.JSON;
import com.zqazfl.common.entity.CustomResult;
import com.zqazfl.common.entity.ResultEntity;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        //获取当前处理请求的controller的方法
        //String methodName=methodParameter.getMethod().getName();
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {

//        serverHttpResponse.getHeaders().set("Access-Control-Allow-Origin", "*");
//        serverHttpResponse.getHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
//        serverHttpResponse.getHeaders().set("Access-Control-Max-Age", "1800");
//        serverHttpResponse.getHeaders().set("Access-Control-Allow-Headers", "x-requested-with");

        if (o instanceof ResultEntity){
            return o;
        }
        if (o instanceof String){
            return JSON.toJSONString(new ResultEntity<Object>(o));
        }
        if(o instanceof Resource){
            return o;
        }
        if (o instanceof CustomResult) {
            return ((CustomResult<?>) o).getData();
        }
        return new ResultEntity<Object>(o);
    }
}
