package com.zqazfl.common.utils;

import java.util.*;

public class CollectionUtil {

    public static List<Map<String, String>> objectToString(List<Map<String, Object>> obj) {
        List<Map<String, String>> list = new ArrayList<>();
        if (obj != null && obj.size() > 0) {
            for (Map<String, Object> map : obj) {
                Map<String, String> data = new HashMap<>();
                Set<String> keySet = map.keySet();
                for (String key : keySet) {
                    data.put(key, StringUtil.getString(map.get(key)));
                }
                list.add(data);
            }
        }
        return list;
    }
}
