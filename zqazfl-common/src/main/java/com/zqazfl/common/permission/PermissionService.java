package com.zqazfl.common.permission;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zqazfl.common.utils.CollectionUtil;
import com.zqazfl.common.utils.JdbcUtilUse;
import com.zqazfl.common.utils.StringUtil;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PermissionService {

    @Resource(name = "Permission_Config")
    Permission_Config pconfig;

    @Resource(name = "JdbcUtilImpl")
    JdbcUtilUse jdbcUtilUse;

    public List<Node> getMenu(Map<String, Object> param) throws Exception {
        String filter = "";
        String partitionType = StringUtil.getString(param.get("partitionType"));
        String menu_type = StringUtil.getString(pconfig.getPermissionConfig(StringUtil.getString(param.get("partitionType"))).get("menuType"));
        String partitionId = StringUtil.getString(param.get("partitionId"));

        Map<String,String> paramMap=new HashMap<>();
        paramMap.put("menutype",menu_type);

        if(StringUtil.isEqual(StringUtil.getString(param.get("authType")),"3") && !StringUtil.isEmpty(partitionId)){
            filter = " and id in (select menu_id from UAA_PERMISSION_CONTROL where AUTHOBJ_ID = :partitionId and partition_Type = :partitionType and AUTH_TYPE=2) ";

            paramMap.put("partitionId",partitionId);
            paramMap.put("partitionType",partitionType);
        }

        String sql =
                " select * from (" +
                        " select ID,menu_name MENUNAME,supmenu_id SUPMENUID,sort from uaa_v_meun_un_func cd" +
                        " where (select count(*) from (select path from uaa_v_meun_un_func where menu_type = :menutype and display = '1' and security_level = 'permission' "+filter+") s where s.PATH like Concat(cd.PATH,'%'))>0 " +
                        " and path is not null " +
                ") a order by sort ";

        List<Map<String, Object>> list = jdbcUtilUse.getNPJdbcTemplate().queryForList(sql,paramMap);
        return NodeTest.getMenuDatas(list);
    }
    public List<Map<String, Object>> getPermission(Map<String, Object> params) throws Exception {
        String type = StringUtil.getString(params.get("partitionType"));
        String auth_type = StringUtil.getString(params.get("authType"));
        String sql = "SELECT concat(MENU_ID,'') MENU_ID,concat(AUTHOBJ_ID,'') AUTHOBJ_ID FROM UAA_PERMISSION_CONTROL where partition_Type = ? and auth_type=? ";
        return jdbcUtilUse.getJdbcTemplate().queryForList(sql,type,auth_type);
    }
    public void savePermission(Map<String, Object> params) throws Exception {
        String type = StringUtil.getString(params.get("partitionType"));
        String auth_type = StringUtil.getString(params.get("authType"));
        Map<String, Object> recordData = JSONObject.parseObject(JSON.toJSONString(params.get("recordData")));
        StringBuilder sql = new StringBuilder();
        //编程式事务
        DataSourceTransactionManager tran = new DataSourceTransactionManager(jdbcUtilUse.getJdbcTemplate().getDataSource());
        TransactionStatus status1 = tran.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED));
        if (recordData.size() > 0) {
            for (Map.Entry<String, Object> entry : recordData.entrySet()) {
                System.out.println("key = " + entry.getKey() + ", value = " + entry.getValue());
                String menuId = entry.getKey().substring(0, entry.getKey().indexOf("_"));
                String AUTHOBJID = entry.getKey().substring(entry.getKey().indexOf("_") + 1);
                if ("true".equals(StringUtil.getString(entry.getValue()))) {
                    sql.append(" INSERT INTO UAA_PERMISSION_CONTROL(menu_id,authobj_id,partition_type,auth_type) " +
                            " VALUES('" + menuId + "','" + AUTHOBJID + "','" + type + "','"+auth_type+"');");
                } else {
                    sql.append(" DELETE FROM UAA_PERMISSION_CONTROL WHERE MENU_ID = '" + menuId + "' AND AUTHOBJ_ID = '" + AUTHOBJID + "';");
                }
            }
            String c_sql = cascadeMdify(params);
            sql.append(c_sql);
            jdbcUtilUse.getJdbcTemplate().update(sql.toString());
            tran.commit(status1);
        }


    }

    /**
     * 获取授权对象
     * @param param 前端传的参数
     * @return
     * @throws Exception
     */
    public List<Node> getAuthObj(Map<String, Object> param) throws Exception{
        Map<String, Object> map_conf=pconfig.getPermissionConfig(StringUtil.getString(param.get("partitionType")));
        if(StringUtil.getString(param.get("authType")).equals("1")){
            Method m = (Method) map_conf.get("partitionKindMethod");
            List<Map<String, Object>> list = (List<Map<String, Object>>) m.invoke(null,param);
            return NodeTest.getConData((List<Map<String, Object>>) list);
        }else if(StringUtil.getString(param.get("authType")).equals("2")){
            Method m = (Method) map_conf.get("partitionListMethod");
            List<Map<String, Object>> list = (List<Map<String, Object>>) m.invoke(null,param);
            return NodeTest.getConData((List<Map<String, Object>>) list);
        } else{
            Method m = (Method) map_conf.get("userGroupMethod");
            List<Map<String, Object>> list = (List<Map<String, Object>>) m.invoke(null,param);
            return NodeTest.getConData((List<Map<String, Object>>) list);
        }
    }

    // 级联修改
    public String cascadeMdify(Map<String, Object> params) throws Exception{
        String partitionType = StringUtil.getString(params.get("partitionType"));
        Map<String, Object> recordData = JSONObject.parseObject(JSON.toJSONString(params.get("recordData")));
        StringBuilder sql = new StringBuilder();
        Map<String, Object> map_config = pconfig.getPermissionConfig(partitionType);
        Map<String, Object> param1 = new HashMap<String, Object>();
        if(recordData.size() > 0){
            // 循环保存的数据
            param1 = params;
            List<Object> array_list = new ArrayList<Object>();
            String  c_param = "";
            for (Map.Entry<String, Object> entry : recordData.entrySet()) {
                String gwId = entry.getKey().substring(entry.getKey().indexOf("_") + 1);
                String auth_type = StringUtil.getString(params.get("authType"));
                // array_list存放gwid，目的是避免相同的gwid多次查询进行级联删除
                if(!array_list.contains(gwId)){
                    array_list.add(gwId);
                    param1.put("partitionKind",gwId);
                    if(StringUtil.isEqual(auth_type,"1")){
                        List<Map<String, Object>> list =null;
                        Method m = (Method) map_config.get("partitionListMethod");
                        List<Map<String, Object>> list_obj = (List<Map<String, Object>>) m.invoke(null,param1);
                        if(list_obj.size()>0){
                            for(int i=0;i<list_obj.size();i++){
                                for (Map.Entry<String, Object> entry1 : recordData.entrySet()) {
                                    String gwid1 = entry1.getKey().substring(entry1.getKey().indexOf("_") + 1);
                                    String menuId = entry1.getKey().substring(0, entry1.getKey().indexOf("_"));
                                    if(StringUtil.isEqual(gwId,gwid1)){
                                        String m_gwid = StringUtil.getString(list_obj.get(i).get("ID"));
                                        if ("true".equals(StringUtil.getString(entry1.getValue()))) {
                                            sql.append(" INSERT INTO UAA_PERMISSION_CONTROL (menu_id,authobj_id,partition_type,auth_type) " +
                                                    "VALUES('" + menuId + "','" + m_gwid + "','"+partitionType+"','2');");
                                        } else {
                                            sql.append(" DELETE FROM UAA_PERMISSION_CONTROL WHERE MENU_ID = '" + menuId + "' AND authobj_id = '" + m_gwid + "';");
                                        }
                                    }
                                }
                                param1.put("partitionId",StringUtil.getString(list_obj.get(i).get("ID")));
                                Method m1 = (Method) map_config.get("userGroupMethod");
                                List<Map<String, Object>> list_obj1 = (List<Map<String, Object>>) m1.invoke(null,param1);
                                if(list_obj1.size()>0){
                                    for(int n=0;n<list_obj1.size();n++){
                                        for (Map.Entry<String, Object> entry2 : recordData.entrySet()) {
                                            String menu_Id = entry2.getKey().substring(0, entry2.getKey().indexOf("_"));
                                            String gw_id = StringUtil.getString(list_obj1.get(n).get("ID"));
                                            if ("false".equals(StringUtil.getString(entry2.getValue()))) {
                                                sql.append(" DELETE FROM UAA_PERMISSION_CONTROL WHERE MENU_ID = '" + menu_Id + "' AND authobj_id = '" + gw_id + "';");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // 授权类型为分区类型权限和分区权限时，级联删除对象分区权限
                    if(StringUtil.isEqual(auth_type,"2")){
                        Method ms = (Method) map_config.get("userGroupMethod");
                        List<Map<String, Object>> list_obj1 = (List<Map<String, Object>>) ms.invoke(null,param1);
                        if(list_obj1.size()>0){
                            for(int i=0;i<list_obj1.size();i++){
                                for (Map.Entry<String, Object> entry2 : recordData.entrySet()) {
                                    String gwid2 = entry2.getKey().substring(entry2.getKey().indexOf("_") + 1);
                                    String menuId = entry2.getKey().substring(0, entry2.getKey().indexOf("_"));
                                    if(StringUtil.isEqual(gwId,gwid2)){
                                        String gwid = StringUtil.getString(list_obj1.get(i).get("ID"));
                                        if ("false".equals(StringUtil.getString(entry2.getValue()))) {
                                            sql.append(" DELETE FROM UAA_PERMISSION_CONTROL WHERE MENU_ID = '" + menuId + "' AND authobj_id = '" + gwid + "';");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return sql.toString();
    }

    // 查询当前用户菜单
    public List<Map<String, String>> getUserMenu() throws Exception {

        Map<String,Object> param=new HashMap<String,Object>();
        param.put("userId",pconfig.getNowUserId());
        param.put("partitionId",pconfig.getNowPartitionId());

        //根据当前分区类型获取配置
        Map<String, Object> map_conf=pconfig.getPermissionConfig(StringUtil.getString(pconfig.getNowPartitionType()));
        String menuType=StringUtil.getString(map_conf.get("menuType"));

        Method m = (Method) map_conf.get("partitionAdminMethod");
        List<Map<String, Object>> list = new ArrayList<>();
        if(m!=null){
            list = (List<Map<String, Object>>) m.invoke(null,param);
        }

        String filter=" 1=2 ";
        if(list.size()>0){//如果是单位管理员
            filter=" id in (select menu_id from uaa_permission_control where AUTHOBJ_ID = '"+pconfig.getNowPartitionId()+"' and AUTH_TYPE=2) ";
        }else{//否则获取用在分区中的所有用户组
            Method ugm = (Method) map_conf.get("groupMemberMethod");
            List<Map<String, Object>> uglist = (List<Map<String, Object>>) ugm.invoke(null,param);
            if(uglist.size()>0){
                String GROUPIDs=StringUtil.sumString(uglist,"GROUPID","','");
                filter=" id in (select menu_id from uaa_permission_control where AUTHOBJ_ID in ('"+GROUPIDs+"') and AUTH_TYPE=3) ";
            }
        }
        //String filters=" id in (select id from uaa_v_meun_un_func connect by prior supmenu_id = id start with "+filter+") ";
        String filters=" id in (select id from uaa_v_meun_un_func cd where (select count(*) from (select path from uaa_v_meun_un_func where "+filter+") s where s.PATH like Concat(cd.PATH,'%'))>0 and path is not null) ";
        String sql =
            " select um.*,umf.func from uaa_menu um " +
            " left join ( " +
                " select menu_id,GROUP_CONCAT(type) func from uaa_menu_function where "+filter+" group by menu_id " +
            " ) umf " +
            " on um.id = umf.menu_id " +
            " where ("+filters+" or security_level='online' or security_level is null) and menu_type = '"+menuType+"' and display = '1' " +
            " order by um.sort";

        return CollectionUtil.objectToString(jdbcUtilUse.getJdbcTemplate().queryForList(sql));
    }

    // 查询有权限的接口列表
    public List<Map<String,Object>> getUserInterface(List<Map<String,Object>> params) throws Exception {

        Map<String,Object> param=new HashMap<String,Object>();
        param.put("userId",pconfig.getNowUserId());
        param.put("partitionId",pconfig.getNowPartitionId());

        //根据当前分区类型获取配置
        Map<String, Object> map_conf=pconfig.getPermissionConfig(StringUtil.getString(pconfig.getNowPartitionType()));
        Method m = (Method) map_conf.get("partitionAdminMethod");
        List<Map<String, Object>> list = new ArrayList<>();
        if(m!=null){
            list = (List<Map<String, Object>>) m.invoke(null,param);
        }

        String filter=" 1=2 ";
        if(list.size()>0){//如果是单位管理员
            filter=" id in (select menu_id from uaa_permission_control where AUTHOBJ_ID = '"+pconfig.getNowPartitionId()+"' and AUTH_TYPE=2) ";
        }else{//否则获取用在分区中的所有用户组
            Method ugm = (Method) map_conf.get("groupMemberMethod");
            List<Map<String, Object>> uglist = (List<Map<String, Object>>) ugm.invoke(null,param);
            if(uglist.size()>0){
                String GROUPIDs=StringUtil.sumString(uglist,"GROUPID","','");
                filter=" id in (select menu_id from uaa_permission_control where AUTHOBJ_ID in ('"+GROUPIDs+"') and AUTH_TYPE=3) ";
            }
        }
        String filters=" fid in (select id from uaa_v_meun_un_func connect by prior supmenu_id = id start with "+filter+") ";

        String sql = " select * from uaa_function_interface where "+filters;
        return jdbcUtilUse.getJdbcTemplate().queryForList(sql);
    }

    // 查询菜单里面所有的接口
    public List<Map<String,Object>> getMenuAllInterface(List<Map<String,Object>> params){
        String sql = " select * from uaa_function_interface ";
        return jdbcUtilUse.getJdbcTemplate().queryForList(sql);
    }
}

