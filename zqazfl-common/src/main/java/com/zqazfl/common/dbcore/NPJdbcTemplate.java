package com.zqazfl.common.dbcore;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NPJdbcTemplate {
    private JdbcTemplate jt;

    private String RegExp = "\\{(.*?)\\}";

    NPJdbcTemplate(JdbcTemplate jt) {
        this.jt = jt;
    }

    /**
     * 查多
     *
     * @param sql
     * @param param
     * @return
     */
    public List<Map<String, Object>> queryForList(String sql, Map<String, Object> param) {
        Pattern p = Pattern.compile(RegExp);
        Matcher m = p.matcher(sql);
        ArrayList list = new ArrayList();
        while (m.find()) {
            String str = m.group(0);
            String str1 = m.group(1);
            sql = sql.replace(str, "?");
            list.add(param.get(str1));
        }

        return jt.queryForList(sql, list.toArray());
    }

    /**
     * 查总
     *
     * @param sql
     * @param param
     * @return
     */
    public <T> T queryForObject(String sql, Map<String, Object> param, Class<T> requiredType) {
        Pattern p = Pattern.compile(RegExp);
        Matcher m = p.matcher(sql);
        ArrayList list = new ArrayList();
        while (m.find()) {
            String str = m.group(0);
            String str1 = m.group(1);
            sql = sql.replace(str, "?");
            list.add(param.get(str1));
        }

        return jt.queryForObject(sql, requiredType, list.toArray());
    }
}
