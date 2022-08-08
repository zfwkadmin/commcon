package com.zqazfl.common.utils;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

public interface JdbcUtilUse {
    /**
     * 根据数据源名称获取JdbcTemplate
     */
    JdbcTemplate getJdbcTemplate(String sourceId);

    /**
     * 获取默认数据源的JdbcTemplate
     */
    JdbcTemplate getJdbcTemplate();

    /**
     * 获取NamedParameterJdbcTemplate
     */
    NamedParameterJdbcTemplate getNPJdbcTemplate();

    NamedParameterJdbcTemplate getNPJdbcTemplate(String sourceId);

    DataSource getDataSource(String sourceId);

    /**
     * 分页查询  返回sql
     * @param tableName 表名
     * @param current  当前页数
     * @param pageSize 数量
     * @param orders  排序
     * @return
     */
    String getPageSql(String tableName, String current, String pageSize, String orders);
}
