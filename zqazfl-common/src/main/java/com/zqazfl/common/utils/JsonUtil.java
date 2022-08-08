package com.zqazfl.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.InputStream;

public class JsonUtil {

    public JSONObject getJson(String fileName) throws Exception{
        InputStream config = getClass().getResourceAsStream(fileName);
        JSONObject json = new JSONObject();
        if (config == null) {
            throw new RuntimeException("读取文件失败");
        } else {
            json = JSON.parseObject(config, JSONObject.class);
            System.out.println(json);
        }
        return json;
    }
}
