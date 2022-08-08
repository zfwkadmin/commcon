package com.zqazfl.common.permission;

import java.util.Map;

public interface Permission_Config {
    Map<String,Object> getPermissionConfig(String subAreaType_id) throws Exception;
    String getNowUserId();
    String getNowPartitionType();
    String getNowPartitionId();
}
