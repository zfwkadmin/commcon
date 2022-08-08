package com.zqazfl.common.view;

import com.zqazfl.common.utils.CollectionUtil;
import com.zqazfl.common.utils.JdbcUtilUse;
import com.zqazfl.common.utils.StringUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ViewUtils {
    @Resource(name = "JdbcUtilImpl")
    JdbcUtilUse jdbcUtil;
    /**
     * @param sql
     * @param params
     * @return
     */
    public Map<String, Object> getData(String sql, Map<String, Object> params) throws Exception {
        Map<String, Object> mapList = new HashMap<>();

        // 是否包含动态列
        if (params.containsKey("dynamicColumn")) {
            mapList.put("dynamicColumn", params.get("dynamicColumn"));
        }
        if ("export".equals(StringUtil.getString(params.get("queryWay")))) { // 导出
            List<Map<String, Object>> exportData = jdbcUtil.getNPJdbcTemplate().queryForList(sql, params);
            mapList.put("exportData", exportData);
        } else {
            // 查多
            List<Map<String, Object>> selectMore = jdbcUtil.getNPJdbcTemplate().queryForList(jdbcUtil.getPageSql(sql,
                    StringUtil.getString(params.get("current")),
                    StringUtil.getString(params.get("pageSize")),
                    StringUtil.getString(params.get("orders"))), params);
            mapList.put("selectMore", CollectionUtil.objectToString(selectMore));

            // 查总
            String selectCount = jdbcUtil.getNPJdbcTemplate().queryForObject("SELECT COUNT(1) FROM (" + sql + ") a", params, String.class);
            mapList.put("selectCount", selectCount);
        }
        return mapList;
    }
}
