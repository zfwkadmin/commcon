package com.zqazfl.common.form;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zqazfl.common.openFeign.MinioControllerFeign;
import com.zqazfl.common.permission.Permission_Config;
import com.zqazfl.common.utils.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

@Component
public class FormMethod {
    //    @Autowired
    MinioControllerFeign minioControllerFeign;
    @Resource(name = "JdbcUtilImpl")
    JdbcUtilUse jdbcUtilUse;
    @Resource(name = "Permission_Config")
    Permission_Config pconfig;
    public void saveForm(JSONObject jsonObject, String confUrl) throws Exception {
        ReflectMethod reflectMethod = new ReflectMethod();//反射
        JsonUtil jsonUtil = new JsonUtil();
        int insertCount = 0;
        StringBuilder sql = new StringBuilder();
//        sql.append("begin ");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // String date2=sdf.format(date);
        JSONObject o = jsonObject.getJSONObject("taskList");
        jsonObject.put("taskList",o);
        String o1 = jsonObject.getString("addfj");
        String o2 = jsonObject.getString("deletefj");
        List<Object> params = new ArrayList<Object>();//  数组形式的参数
        Map<String, Object> map_addfj = new HashMap<>();
        Map<String, Object> map_deletefj = new HashMap<>();
        List<Map<String, String>> list_fj = new ArrayList<>();
        // Map<String, Object> map_conf = JSON.parseObject(o1.toString());//配置集合
        Map<String, Object> map_conf = jsonUtil.getJson(confUrl);
        System.out.println("json数据=" + map_conf);
        JSONArray main_list = jsonObject.getJSONObject("taskList").getJSONArray(map_conf.get("MAIN_TABLE").toString());// 获取主表数据信息
        Map<String, Object> table_config = JSON.parseObject(StringUtil.getString(map_conf.get("Table_Config")));//表单配置集合
        Map<String, Object> maintable_config = JSON.parseObject(StringUtil.getString(table_config.get(map_conf.get("MAIN_TABLE"))));//主表配置集合
        Map<String, Object> mainField_config = JSON.parseObject(StringUtil.getString(maintable_config.get("Field_Config")));//主表字段配置
        String main_id = "";// 主表主键
        String detail_id = "";// 明细表主键
        String main_primary = StringUtil.getString(maintable_config.get("Primary_key"));// 从配置项获取主表主键字段
        if (main_primary.isEmpty()) {//配置项中未配置主键字段，默认为ID
            main_primary = "ID";
        }
        Map<String, Object> mainPrimary_config = JSON.parseObject(mainField_config.get(main_primary).toString());//主表主键配置
        JSONObject jsonObjects = main_list.getJSONObject(0);
        // 主键没有值或者未配置默认值，抛出异常
        if (!StringUtil.getString(main_list.getJSONObject(0).get(main_primary)).isEmpty()) {
            main_id = StringUtil.getString(main_list.getJSONObject(0).get(main_primary));
        } else {
            if (StringUtil.getString(mainPrimary_config.get("add_defaultValue")).isEmpty()) {
                throw new Exception("主键未设置默认值");
            } else {
                String defaultValues = StringUtil.getString(mainPrimary_config.get("add_defaultValue"));
                main_id = reflectMethod.getResult(defaultValues);
                jsonObjects.put(main_primary, main_id);
            }
        }
        // 循环配置集合,通过循环配置的RELATED从taskList里面取值。
        for (String key : jsonObject.getJSONObject("taskList").keySet()) {

            Map<String, Object> main_map = main_list.getJSONObject(0);
            Map<String, Object> table_configs = new HashMap<>();
            if (StringUtil.getString(table_config.get(key)).isEmpty()) {
                continue;
            } else {
                table_configs = JSON.parseObject(StringUtil.getString(table_config.get(key)));//获取每个表的配置集合
            }
            JSONArray jsonArray = jsonObject.getJSONObject("taskList").getJSONArray(key);
            if (!map_conf.get("MAIN_TABLE").toString().isEmpty() && key.equals(map_conf.get("MAIN_TABLE").toString())) {//保存主表
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject map1 = jsonArray.getJSONObject(i);
                    String use_type = StringUtil.getString(map1.get("use_type"));
                    StringBuilder sql1 = new StringBuilder();
                    if ("add".equals(use_type)) {
                        sql.append("insert into " + map_conf.get("MAIN_TABLE").toString() + "(");
                        sql1.append(" values(");
                    } else if ("modify".equals(use_type)) {
                        sql.append("update " + key + " set ");
                    }
                    //map1.remove("use_type");//use_type 为操作类型，不需要保存到数据库
                    for (String keys : map1.keySet()) {
                        Map<String, Object> mainField_configs = new HashMap<>();//主表字段配置
                        if (!StringUtil.getString(mainField_config.get(keys)).isEmpty()) {
                            mainField_configs = JSON.parseObject(StringUtil.getString(mainField_config.get(keys)));//主表字段配置
                        }
                        // 去掉保存的字段空格
                        if(!StringUtil.getString(mainField_configs.get("dataType")).equals("file")||!StringUtil.getString(mainField_configs.get("dataType")).equalsIgnoreCase("date")){
                            // map1.put(keys,StringUtil.getString(map1.get(keys)).replaceAll(" ",""));
                            if (StringUtil.isEmpty(StringUtil.getString(map1.get(keys)))){
                                map1.put(keys,null);
                            }else {
                                map1.put(keys,StringUtil.getString(map1.get(keys)).trim());
                            }
                        }

                        // 判断字段是否需要保存，false 不保存
                        if (StringUtil.getString(mainField_configs.get("needSubmit")).equals("false")) {
                            continue;
                        }
                        if(StringUtil.isEqual(keys,"use_type")){//use_type 为操作类型，不需要保存到数据库
                            continue;
                        }
                        Map<String, String> mapFile = new HashMap<>();
                        // 附件
                        if (!StringUtil.getString(o1).isEmpty() && StringUtil.getString(mainField_configs.get("dataType")).equals("file") && !StringUtil.getString(map1.get(keys)).isEmpty()) {
                            mapFile.put("filed_token", StringUtil.getString(map1.get(keys)));
                            mapFile.put("data_obj", key);
                            mapFile.put("data_pk", main_id);
                            mapFile.put("data_field", keys);
                            // mapFile.put("GROUPTYPE",);
                            list_fj.add(mapFile);
                        }
                        if ("add".equals(use_type)) {
                            sql.append(keys + ",");
                            if (keys.equals(main_primary)) {//主键保存
                                if (StringUtil.getString(mainField_configs.get("dataType")).equalsIgnoreCase("date") && !StringUtil.getString(map1.get(keys)).isEmpty()) {
//                                    sql1.append("to_date(?,'yyyy-mm-dd hh24:mi:ss')" + ",");
                                    sql1.append("STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s')" + ",");
                                    params.add(sdf1.format(sdf.parse(main_id)));
                                } else {
                                    sql1.append("?" + ",");
                                    params.add(main_id);
                                }
                            } else {
                                if (!StringUtil.getString(mainField_configs.get("add_defaultValue")).isEmpty() && StringUtil.getString(map1.get(keys)).isEmpty()) {// 字段默认值配置不为空且值为空的时候
                                    String defaultValues = StringUtil.getString(mainField_configs.get("add_defaultValue"));
                                    String returnVal = reflectMethod.getResult(defaultValues);//通过配置获取默认值
                                    jsonObjects.put(keys, returnVal);
                                    if (StringUtil.getString(mainField_configs.get("dataType")).equalsIgnoreCase("date")) {// 日期格式保存
//                                        sql1.append(" to_date(?,'yyyy-mm-dd hh24:mi:ss') " + ",");
                                        sql1.append(" STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s') " + ",");
                                        params.add(sdf1.format(sdf1.parse(returnVal)) );
                                    } else {
                                        sql1.append("?" + ",");
                                        params.add(StringUtil.getString(returnVal));
                                    }
                                } else {//未设置默认值
                                    if (StringUtil.getString(mainField_configs.get("dataType")).equalsIgnoreCase("date") && !StringUtil.getString(map1.get(keys)).isEmpty()) {
                                        sql1.append(" STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s') " + ",");
//                                        sql1.append(" to_date(?,'yyyy-mm-dd hh24:mi:ss') " + ",");
                                        params.add( sdf1.format(sdf1.parse(StringUtil.getString(map1.get(keys)))));
                                    } else {
                                        sql1.append("?" + ",");
                                        params.add(map1.get(keys));
                                    }
                                }
                            }
                        } else if ("modify".equals(use_type)) {
                            if (!StringUtil.getString(mainField_configs.get("modify_defaultValue")).isEmpty() && !keys.equals(main_primary)) {// 字段默认值不为空（修改的时候主键设置的默认值不再修改）
                                String defaultValues = StringUtil.getString(mainField_configs.get("modify_defaultValue"));
                                String returnVal = reflectMethod.getResult(defaultValues);//通过配置获取默认值
                                jsonObjects.put(keys, returnVal);
                                if (StringUtil.getString(mainField_configs.get("dataType")).equalsIgnoreCase("date")) {
//                                    sql.append(keys + "=" + "to_date(?,'yyyy-mm-dd hh24:mi:ss')" + ",");
                                    sql.append(keys + "=" + "STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s')" + ",");
                                    params.add(sdf1.format(sdf1.parse(returnVal)));
                                } else {
                                    sql.append(keys + "=" + "?" + ",");
                                    params.add(StringUtil.getString(returnVal));
                                }
                            } else {
                                if (StringUtil.getString(mainField_configs.get("dataType")).equalsIgnoreCase("date") && !StringUtil.getString(map1.get(keys)).isEmpty()) {
//                                    sql.append(keys + "=" + "to_date(?,'yyyy-mm-dd hh24:mi:ss')" + ",");
                                    sql.append(keys + "=" + "STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s')" + ",");
                                    params.add(sdf1.format(sdf1.parse(StringUtil.getString(map1.get(keys)))));
                                } else {
                                    sql.append(keys + "=" + "?" + ",");
                                    params.add(map1.get(keys));
                                }
                            }
                        }
                    }
                    JSONArray retunlist=new JSONArray();
                    retunlist.set(0, jsonObjects);
                    o.put(map_conf.get("MAIN_TABLE").toString(), retunlist);
                    if ("add".equals(use_type)) {
                        sql.deleteCharAt(sql.length() - 1);
                        sql1.deleteCharAt(sql1.length() - 1);
                        sql.append(")");
                        sql1.append(");");
                        sql.append(sql1);
                    } else if ("modify".equals(use_type)) {
                        sql.deleteCharAt(sql.length() - 1);
                        sql.append(" where " + main_primary + "= " + "?" + ";");
                        params.add(StringUtil.getString(map1.get(main_primary)));
                    }
                }
            } else {// 明细表保存
                //明细表主键
                Map<String, Object> Field_config = JSON.parseObject(StringUtil.getString(table_configs.get("Field_Config")));//获取表字段配置
                String table_primary = StringUtil.getString(Field_config.get("Primary_key"));// 从配置项获取主键字段
                if (table_primary.isEmpty()) {
                    table_primary = "ID";
                }
                Map<String, Object> map_related = JSON.parseObject(StringUtil.getString(table_configs.get("Table_RELATION")));//获取明细表关联关系
                if (map_related.size() > 0) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject map1 = jsonArray.getJSONObject(i);
                        String use_type = StringUtil.getString(map1.get("use_type"));
                        StringBuilder sql1 = new StringBuilder();
                        if ("add".equals(use_type)) {
                            sql.append("insert into " + key + "(");
                            sql1.append(" values(");
                        } else if ("modify".equals(use_type)) {
                            sql.append("update " + key + " set ");
                        } else if ("delete".equals(use_type)) {
                            sql.append("delete from " + key + " where ");
                        }
                        //map1.remove("use_type");//use_type 为操作类型，不需要保存到数据库
                        for (String keys : map1.keySet()) {
                            // Map<String,Object> field_configs=JSON.parseObject(StringUtil.getString(Field_config.get(keys)));//获取表字段配置
                            Map<String, Object> mainField_configs = new HashMap<>();//表字段配置
                            if (!StringUtil.getString(Field_config.get(keys)).isEmpty()) {
                                mainField_configs = JSON.parseObject(StringUtil.getString(Field_config.get(keys)));//表字段配置
                            }
                            // 去掉保存的字段空格
                            if(!StringUtil.getString(mainField_configs.get("dataType")).equals("file")||!StringUtil.getString(mainField_configs.get("dataType")).equalsIgnoreCase("date")){
                                //  map1.put(keys,StringUtil.getString(map1.get(keys)).replaceAll(" ",""));
                            }
                            // 判断表字段是否需要保存，false不保存
                            if (StringUtil.getString(mainField_configs.get("needSubmit")).equals("false")) {
                                continue;
                            }
                            if(StringUtil.isEqual(keys,"use_type")){//use_type 为操作类型，不需要保存到数据库
                                continue;
                            }
                            // 判断明细表字段是否在关系map中
                            if (map_related.containsKey(keys)) {
                                // 关联键是主表主键
                                if (main_primary.equals(map_related.get(keys))) {
                                    if ("add".equals(use_type)) {
                                        sql.append(keys + ",");
                                        if (StringUtil.getString(mainField_configs.get("dataType")).equalsIgnoreCase("date") && !StringUtil.getString(map1.get(keys)).isEmpty()) {
//                                            sql1.append(" to_date(?,'yyyy-mm-dd hh24:mi:ss') "  + ",");
                                            sql1.append(" STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s') "  + ",");
                                            params.add(sdf1.format(sdf1.parse(main_id)));
                                        } else {
                                            sql1.append("?" + ",");
                                            params.add(main_id);
                                            map1.put(keys, main_id);
                                        }
                                    } else if ("modify".equals(use_type)) {
                                        // 修改的时候，关联键不能为空
                                        if (StringUtil.getString(map1.get(keys)).isEmpty()) {
                                            throw new Exception("明细表关联键为空，无法修改");
                                        }
                                        if (StringUtil.getString(mainField_configs.get("dataType")).equalsIgnoreCase("date") && !StringUtil.getString(map1.get(keys)).isEmpty()) {
//                                            sql.append(keys + "=" + "to_date(?,'yyyy-mm-dd hh24:mi:ss')" + ",");
                                            sql.append(keys + "=" + "STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s')" + ",");
                                            params.add(sdf1.format(sdf1.parse(StringUtil.getString(map1.get(keys)))));
                                        } else {
                                            sql.append(keys + "=" + "?" + ",");
                                            params.add(StringUtil.getString(map1.get(keys)));
                                        }
                                    } else if ("delete".equals(use_type)) {

                                    }
                                } else {
                                    if ("add".equals(use_type)) {
                                        sql.append(keys + ",");
                                        if (main_map.get(map_related.get(keys)).toString().isEmpty() || StringUtil.getString(map1.get(keys)).isEmpty()) {
                                            throw new Exception("关联键不能为空");
                                        } else {
                                            if (!StringUtil.getString(mainField_configs.get("add_defaultValue")).isEmpty() && StringUtil.getString(map1.get(keys)).isEmpty()) {
                                                String defaultValues = StringUtil.getString(mainField_configs.get("add_defaultValue"));
                                                String returnVal = reflectMethod.getResult(defaultValues);//通过配置获取默认值
                                                map1.put(keys, returnVal);
                                                if(keys.equals(table_primary)){
                                                    detail_id = returnVal;
                                                }
                                                if (StringUtil.getString(mainField_configs.get("dataType")).equalsIgnoreCase("date")) {
//                                                    sql1.append(" to_date(?,'yyyy-mm-dd hh24:mi:ss') " + ",");
                                                    sql1.append(" STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s') " + ",");
                                                    params.add(sdf1.format(sdf1.parse(returnVal)));
                                                } else {
                                                    sql1.append("?" + ",");
                                                    params.add(StringUtil.getString(returnVal));
                                                }
                                            } else {
                                                if (StringUtil.getString(mainField_configs.get("dataType")).equalsIgnoreCase("date") && !StringUtil.getString(map1.get(keys)).isEmpty()) {
//                                                    sql1.append(" to_date(?,'yyyy-mm-dd hh24:mi:ss') " + ",");
                                                    sql1.append(" STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s') " + ",");
                                                    params.add(sdf1.format(sdf1.parse(StringUtil.getString(map1.get(keys)))));
                                                } else {
                                                    sql1.append("?" + ",");
                                                    params.add(StringUtil.getString(map1.get(keys)));
                                                }
                                            }
                                        }
                                    } else if ("modify".equals(use_type)) {
                                        // 修改的时候，关联键不能为空
                                        if (StringUtil.getString(map1.get(keys)).isEmpty()) {
                                            throw new Exception("明细表关联键为空，无法修改");
                                        }
                                        if (!StringUtil.getString(mainField_configs.get("modify_defaultValue")).isEmpty() && !table_primary.equals(keys)) {
                                            String defaultValues = StringUtil.getString(mainField_configs.get("modify_defaultValue"));
                                            String returnVal = reflectMethod.getResult(defaultValues);//通过配置获取默认值
                                            map1.put(keys, returnVal);
                                            if (StringUtil.getString(mainField_configs.get("dataType")).equalsIgnoreCase("date")) {
//                                                sql.append(keys + "=" + "to_date(?,'yyyy-mm-dd hh24:mi:ss')" + ",");
                                                sql.append(keys + "=" + "STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s')" + ",");
                                                params.add(sdf1.format(sdf1.parse(returnVal)));
                                            } else {
                                                sql.append(keys + "=" + "?" + ",");
                                                params.add(StringUtil.getString(returnVal));
                                            }
                                        } else {
                                            if (StringUtil.getString(mainField_configs.get("dataType")).equalsIgnoreCase("date") && !StringUtil.getString(map1.get(keys)).isEmpty()) {
//                                                sql.append(keys + "=" + "to_date(?,'yyyy-mm-dd hh24:mi:ss')" + ",");
                                                sql.append(keys + "=" + "STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s')" + ",");
                                                params.add(sdf1.format(sdf1.parse(StringUtil.getString(map1.get(keys)))));
                                            } else {
                                                sql.append(keys + "=" + "?" + ",");
                                                params.add(StringUtil.getString(map1.get(keys)));
                                            }
                                        }
                                    } else if ("delete".equals(use_type)) {

                                    }
                                }
                            } else {
                                if ("add".equals(use_type)) {
                                    sql.append(keys + ",");
                                    // 字段为明细表主键
                                    if (table_primary.equals(keys)) {
                                        // 获取明细表主键的值,前端传的有值，就用该值，没有值就从配置项查找其配置的默认值。
                                        if (!StringUtil.getString(map1.get(keys)).isEmpty()) {
                                            detail_id = StringUtil.getString(map1.get(keys));
                                        } else {
                                            if (StringUtil.getString(mainField_configs.get("add_defaultValue")).isEmpty()) {
                                                throw new Exception("明细表主键未配置默认值");
                                            } else {
                                                String defaultValues = StringUtil.getString(mainField_configs.get("add_defaultValue"));
                                                detail_id = reflectMethod.getResult(defaultValues);
                                                map1.put(keys, detail_id);

                                            }
                                        }
                                        if (StringUtil.getString(mainField_configs.get("dataType")).equalsIgnoreCase("date") && !StringUtil.getString(map1.get(keys)).isEmpty()) {
//                                            sql1.append(" to_date(?,'yyyy-mm-dd hh24:mi:ss') " + ",");
                                            sql1.append(" STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s') " + ",");
                                            params.add(sdf1.format(sdf1.parse(main_id)));
                                        } else {
                                            sql1.append("?" + ",");
                                            params.add(detail_id);
                                        }

                                    } else {
                                        if (!StringUtil.getString(mainField_configs.get("add_defaultValue")).isEmpty() && StringUtil.getString(map1.get(keys)).isEmpty()) {
                                            String defaultValues = StringUtil.getString(mainField_configs.get("add_defaultValue"));
                                            String returnVal = reflectMethod.getResult(defaultValues);//通过配置获取默认值
                                            map1.put(keys, returnVal);
                                            if (StringUtil.getString(mainField_configs.get("dataType")).equalsIgnoreCase("date")) {
//                                                sql1.append(" to_date(?,'yyyy-mm-dd hh24:mi:ss') " + ",");
                                                sql1.append(" STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s') " + ",");
                                                params.add(sdf1.format(sdf1.parse(StringUtil.getString(returnVal))));
                                            } else {
                                                sql1.append("?" + ",");
                                                params.add(StringUtil.getString(returnVal));
                                            }
                                        } else {
                                            if (StringUtil.getString(mainField_configs.get("dataType")).equalsIgnoreCase("date") && !StringUtil.getString(map1.get(keys)).isEmpty()) {
//                                                sql1.append(" to_date(?,'yyyy-mm-dd hh24:mi:ss') "+",");
                                                sql1.append(" STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s') "+",");
                                                params.add(sdf1.format(sdf1.parse(StringUtil.getString(map1.get(keys)))));
                                            } else {
                                                sql1.append("?" + ",");
                                                params.add(StringUtil.getString(map1.get(keys)));
                                            }
                                        }
                                    }
                                } else if ("modify".equals(use_type)) {
                                    if (table_primary.equals(keys)&&!StringUtil.getString(map1.get(keys)).isEmpty()) {
                                        detail_id = StringUtil.getString(map1.get(keys));
                                    }
                                    if (!StringUtil.getString(mainField_configs.get("modify_defaultValue")).isEmpty() && !table_primary.equals(keys)) {
                                        String defaultValues = StringUtil.getString(mainField_configs.get("modify_defaultValue"));
                                        String returnVal = reflectMethod.getResult(defaultValues);//通过配置获取默认值
                                        map1.put(keys, returnVal);
                                        if (StringUtil.getString(mainField_configs.get("dataType")).equalsIgnoreCase("date")) {
//                                            sql.append(keys + "=" + "to_date(?,'yyyy-mm-dd hh24:mi:ss')" + ",");
                                            sql.append(keys + "=" + "STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s')" + ",");
                                            params.add(sdf1.format(sdf1.parse(StringUtil.getString(returnVal))));
                                        } else {
                                            sql.append(keys + "=" + "?" + ",");
                                            params.add(StringUtil.getString(returnVal));
                                        }
                                    } else {
                                        if (StringUtil.getString(mainField_configs.get("dataType")).equalsIgnoreCase("date") && !StringUtil.getString(map1.get(keys)).isEmpty()) {
//                                            sql.append(keys + "=" + "to_date(?,'yyyy-mm-dd hh24:mi:ss')" + ",");
                                            sql.append(keys + "=" + "STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s')" + ",");
                                            params.add(sdf1.format(sdf1.parse(StringUtil.getString(map1.get(keys)))));
                                        } else {
                                            sql.append(keys + "=" + "?" + ",");
                                            params.add(StringUtil.getString(map1.get(keys)));
                                        }
                                    }
                                } else if ("delete".equals(use_type)) {

                                }

                            }
                            Map<String, String> mapFile = new HashMap<>();
                            // 附件
                            if (!StringUtil.getString(o1).isEmpty() && StringUtil.getString(mainField_configs.get("dataType")).equals("file") && !StringUtil.getString(map1.get(keys)).isEmpty()) {
                                mapFile.put("filed_token", StringUtil.getString(map1.get(keys)));
                                mapFile.put("data_obj", key);
                                mapFile.put("data_pk", detail_id);
                                mapFile.put("data_field", keys);
                                // mapFile.put("GROUPTYPE",);
                                list_fj.add(mapFile);
                            }
                        }
                        jsonArray.set(i, map1);
                        if ("add".equals(use_type)) {
                            sql.deleteCharAt(sql.length() - 1);
                            sql1.deleteCharAt(sql1.length() - 1);
                            sql.append(")");
                            sql1.append(");");
                            sql.append(sql1);
                        } else if ("modify".equals(use_type)) {
                            sql.deleteCharAt(sql.length() - 1);
                            sql.append(" where " + table_primary + "= " + "?" + ";");
                            params.add(StringUtil.getString(map1.get(table_primary)));
                        } else if ("delete".equals(use_type)) {
                            sql.append(table_primary + "=" + "?" + "; ");
                            params.add(StringUtil.getString(map1.get(table_primary)));
                        }
                    }

                    //o.put(key, main_list);
                }
            }
        }
        Logger.getGlobal().info("附件上传情况"+(list_fj.size() > 0 && !StringUtil.getString(o1).isEmpty())+" 修改附件状态list:"+list_fj);
        if (list_fj.size() > 0 && !StringUtil.getString(o1).isEmpty()) {
            List<String> arrayo1 = Arrays.asList(o1.split(","));
            map_addfj.put("addInfo", JSON.toJSONString(arrayo1));
            map_addfj.put("list", StringUtil.getString(JSONArray.parseArray(JSON.toJSONString(list_fj))));
            Logger.getGlobal().info("附件上传addinfo"+map_addfj);
            minioControllerFeign.updateType(map_addfj);
        }
        if (!StringUtil.getString(o2).isEmpty()) {
            List<String> arrayo2 = Arrays.asList(o2.split(","));
            map_deletefj.put("deleteInfo", JSON.toJSONString(arrayo2));
            minioControllerFeign.delete(map_deletefj);
        }
//        sql.append(" end;");
        insertCount = jdbcUtilUse.getJdbcTemplate().update(sql.toString(),params.toArray());
        // JdbcUtil.getJdbcTemplate().update(sql.toString(),list_fj);
        Logger.getGlobal().info("操作人："+pconfig.getNowUserId()+" ;主表id："+main_id+" ;参数："+jsonObject.toJSONString());
    }

    public Map<String, Object> loadForm(JSONObject jsonObject, String confUrl) throws Exception {
        ReflectMethod reflectMethod = new ReflectMethod();//反射
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> maps = new HashMap<>();// 表单数据
        String pkValue = StringUtil.getString(jsonObject.get("pkValue"));//获取主表主键
        Object o2 = jsonObject.get("taskList");//表单数据配置
        JsonUtil jsonUtil = new JsonUtil();
        Map<String, Object> map_conf = jsonUtil.getJson(confUrl);
        Map<String, Object> map_list = JSON.parseObject(o2.toString());//数据集合
        System.out.println("ssss" + map_list);
        if (pkValue.isEmpty()) {
            Map<String, Object> table_config = JSON.parseObject(StringUtil.getString(map_conf.get("Table_Config")));//表单配置集合
            //Map<String,Object> maintable_config = JSON.parseObject(StringUtil.getString(table_config.get(map_conf.get("MAIN_TABLE"))));//主表配置集合
            //Map<String,Object> mainField_config = JSON.parseObject(StringUtil.getString(maintable_config.get("Field_Config")));//主表字段配置
            //遍历表单配置集合
            for (String keys : table_config.keySet()) {
                Map<String, Object> maintable_config = JSON.parseObject(StringUtil.getString(table_config.get(keys)));//表配置集合
                Map<String, Object> mainField_config = JSON.parseObject(StringUtil.getString(maintable_config.get("Field_Config")));//表字段配置
                List<Map<String, Object>> mainField_list = (List) map_list.get(keys);
                if (mainField_config.size() > 0 && mainField_list.size() > 0) {
                    Map<String, Object> map1 = new HashMap<>();
                    List<Map<String, Object>> data_list = new ArrayList<Map<String, Object>>();
                    for (String key : mainField_list.get(0).keySet()) {
                        Map<String, Object> mainField_configs = new HashMap<>();//主表字段配置
                        if (StringUtil.getString(mainField_config.get(key)).isEmpty()) {
                            map1.put(key, mainField_list.get(0).get(key));
                        } else {
                            mainField_configs = JSON.parseObject(StringUtil.getString(mainField_config.get(key)));//主表字段配置
                            // 配置加载默认值并且值为空时，获取默认值
                            if (!StringUtil.getString(mainField_configs.get("load_defaultValue")).isEmpty() && StringUtil.getString(mainField_list.get(0).get(key)).isEmpty()) {
                                String defaultValues = StringUtil.getString(mainField_configs.get("load_defaultValue"));
                                String returnVal = reflectMethod.getResult(defaultValues);//通过配置获取默认值
                                map1.put(key, returnVal);
                            } else {
                                map1.put(key, mainField_list.get(0).get(key));
                            }
                        }
                    }
                    if (map1.size() > 0) {
                        data_list.add(map1);
                    }
                    maps.put(keys, data_list);
                }

            }
            //throw new Exception("主键不能为空");
        } else {
            String mainTable = StringUtil.getString(map_conf.get("MAIN_TABLE"));//主表名称
            if (mainTable.isEmpty()) {
                throw new Exception("请设置主表");
            }
            Map<String, Object> table_config = JSON.parseObject(StringUtil.getString(map_conf.get("Table_Config")));//表单配置集合
            Map<String, Object> maintable_config = JSON.parseObject(StringUtil.getString(table_config.get(mainTable))); //主表配置
            String main_primary = StringUtil.getString(maintable_config.get("Primary_key"));// 从配置项获取主表主键字段
            if (main_primary.isEmpty()) {
                main_primary = "ID";
            }
            //pkValue = "3813555";
            String sql = "";
            if (!StringUtil.getString(maintable_config.get("VIEW")).isEmpty()) {
                Map<String, Object> view = JSON.parseObject(StringUtil.getString(maintable_config.get("VIEW")));//明细表与主表关系配置
                if (view.size() > 0 && !StringUtil.getString(view.get("tableName")).isEmpty()) {
                    sql = " select * from  " + view.get("tableName") + " where " + main_primary + " = ? ";
                } else {
                    sql = " select * from  " + mainTable + " where " + main_primary + " = ? ";
                }
            } else {
                sql = " select * from  " + mainTable + " where " + main_primary + " = ? ";
            }
            List<Map<String, String>> main_list = CollectionUtil.objectToString(jdbcUtilUse.getJdbcTemplate().queryForList(sql,pkValue));// 查询主表的数据
            maps.put(mainTable, main_list);
            if (main_list.size() < 1) {
                throw new Exception("主表数据为空");
            }
            Map<String, String> mainmap = main_list.get(0);
            // 循环配置map
            for (String key : table_config.keySet()) {
                boolean mxbsj = true;
                if (key.equals(mainTable)) {
                    // 是主表不再查找
                } else {
                    Map<String, Object> detail_config = JSON.parseObject(StringUtil.getString(table_config.get(key)));//明细表配置
                    Map<String, Object> detail_relation = JSON.parseObject(StringUtil.getString(detail_config.get("Table_RELATION")));//明细表与主表关系配置
                    String detail_filter = StringUtil.getString(detail_config.get("Filter"));//明细表过滤条件配置
                    String filters = "";
                    if (detail_relation.isEmpty()) {
                        throw new Exception("明细表与主表关联关系未配置");
                    } else {
                        List<Map<String, String>> detail_list = null;
                        for (String keys : detail_relation.keySet()) {
                            if (StringUtil.getString(mainmap.get(detail_relation.get(keys))).isEmpty()) { //主表与明细表的关联键为空时，抛出异常
                                //throw new Exception(" 主表关联键值不能为空! ");
                                mxbsj = false;
                            } else {
                                filters += "" + keys + "= '" + mainmap.get(detail_relation.get(keys)) + "'";
                            }
                        }
                        String sql1 = "";
                        if (mxbsj) {
                            if (!StringUtil.getString(detail_config.get("VIEW")).isEmpty()) {
                                Map<String, Object> view = JSON.parseObject(StringUtil.getString(detail_config.get("VIEW")));//明细表与主表关系配置
                                if (view.size() > 0 && !StringUtil.getString(view.get("tableName")).isEmpty()) {
                                    sql1 = " select * from  " + StringUtil.getString(view.get("tableName")) + " where " + filters + "" + detail_filter + " ";
                                } else {
                                    sql1 = " select * from  " + key + " where " + filters + "" + detail_filter + " ";
                                }
                            } else {
                                sql1 = " select * from  " + key + " where " + filters + "" + detail_filter + " ";
                            }
                            detail_list = CollectionUtil.objectToString(jdbcUtilUse.getJdbcTemplate().queryForList(sql1));// 明细表数据
                            maps.put(key, detail_list);// 将值放进map中
                        }

                    }

                }

            }
        }
        map.put("formData", maps);
        return map;
    }
    public int deleteData(JSONObject jsonObject, String confUrl)throws Exception{
        List<Object> params = new ArrayList<Object>();//  数组形式的参数
        JsonUtil jsonUtil = new JsonUtil();
        ReflectMethod reflectMethod = new ReflectMethod();//反射
        JSONObject o = jsonObject.getJSONObject("taskList");
        Map<String, Object> map_conf = jsonUtil.getJson(confUrl);
        System.out.println("qwer"+o+";asdf"+map_conf);
        String mainTable = StringUtil.getString(map_conf.get("MAIN_TABLE"));// 主表
        Map<String, Object> map_table = (Map<String, Object>) map_conf.get("Table_Config");
        StringBuilder sql = new StringBuilder();
        String primaryKey = "";
//        sql.append("begin ");
        for(String key:map_table.keySet()){
            Map<String, Object> table_config = (Map<String, Object>) map_table.get(key);
            System.out.println("表配置："+table_config);
            if(key.equals(mainTable)){
                sql.append(" delete from "+key +" where ");
                if(StringUtil.getString(table_config.get("Primary_key")).isEmpty()){
                    primaryKey = "ID";
                }else{
                    primaryKey = StringUtil.getString(table_config.get("Primary_key"));
                }
                sql.append(primaryKey+"=?;");
                params.add(StringUtil.getString(o.get(primaryKey)));
            }else{
                Map<String,Object> map_relation = (Map<String, Object>) table_config.get("Table_RELATION");// 获取关系
                int re_count = 0;
                if(StringUtil.getString(table_config.get("IsCascadeDelete")).equals("true")){
                    sql.append(" delete from "+key +" where ");
                    for(String keys:map_relation.keySet()){
                        if(StringUtil.getString(o.get(StringUtil.getString(map_relation.get(keys)))).isEmpty()){
                            throw new Exception("关联键值不能为空");
                        }else{
                            sql.append(keys+"= ? ");
                            params.add(StringUtil.getString(o.get(StringUtil.getString(map_relation.get(keys)))));
                        }
                        if(re_count>=map_relation.size()){
                            sql.append(" and ");
                            re_count++;
                        }
                    }
                    sql.append(";");
                }else{
                    continue;
                }
            }
        }
//        sql.append("end;");
        int count_delete = jdbcUtilUse.getJdbcTemplate().update(sql.toString(),params.toArray());
        Logger.getGlobal().info("操作人："+pconfig.getNowUserId()+" ;主表id："+StringUtil.getString(o.get(primaryKey))+" ;参数："+jsonObject.toJSONString());
        return count_delete;
    }
}
