package com.zqazfl.common.permission;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zqazfl.common.utils.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class MenuService {

    @Resource(name = "JdbcUtilImpl")
    JdbcUtilUse jdbcUtilUse;
    public void saveMenu(JSONObject jsonObject){
        JSONObject o = jsonObject.getJSONObject("taskList");
        String urltype = o.getString("URLTYPE");
        String serial = o.getString("SERIAL");
        String id=o.getString("ID");
        String display=o.getString("DISPLAY");
        String name=o.getString("MENUNAME");
        String icon=o.getString("ICON");
        String menutype=o.getString("MENUTYPE");
        String url=o.getString("URL");
        String security_level=o.getString("SECURITY_LEVEL");
        String supmenuid=o.getString("SUPMENUID");
        String usetype =o.getString("usertype");
        String sql = "";
        if(StringUtil.getString(usetype).equals("add")){
            sql = " insert into uaa_menu (ID,MENUNAME,SUPMENUID,URLTYPE,URL,security_level,DISPLAY,SERIAL,MENUTYPE,ICON) VALUES(?,?,?,?,?,?,?,?,?,?)  ";
            jdbcUtilUse.getJdbcTemplate().update(sql,id,name,supmenuid,urltype,url,security_level,display,serial,menutype,icon);
        }else{
            sql = " update  uaa_menu set menuname=?, SUPMENUID=?,URLTYPE=?,URL=?,security_level=?,DISPLAY=?,SERIAL=?,MENUTYPE=?,ICON=? where id =?";
            jdbcUtilUse.getJdbcTemplate().update(sql,name,supmenuid,urltype,url,security_level,display,serial,menutype,icon,id);
        }
        System.out.println(o);

    }

    public  int getMaxSort(String hashmap) throws Exception {
        String sql="";
            sql="select max(sort) as MaxSort from uaa_menu where supmenu_id=?";
            Map<String, Object> map = this.jdbcUtilUse.getJdbcTemplate().queryForMap(sql,hashmap);
            Integer big =map.get("MaxSort")==null?1:(Integer) map.get("MaxSort")+1;
            return big;
    }
    public void menuModify(JSONObject jsonObject){
        String sql="";
        JSONObject modify =jsonObject.getJSONObject("modify");
        JSONObject source =jsonObject.getJSONObject("source");
        sql+="begin;update uaa_menu set sort= ? where id = ?;";
        sql+="update uaa_menu set sort= ? where id = ?;commit;";
        jdbcUtilUse.getJdbcTemplate().update(sql,source.getString("sort"),modify.getString("id"),modify.getString("sort"),source.getString("id"));
    }

    // 获取菜单id
    public String getMenuId(){
        String id = jdbcUtilUse.getJdbcTemplate().queryForObject("select fix_seq.nextval from dual ",String.class);
        return id;
    }
    // 加载菜单
    public List<Map<String,String>> getMenu(){
        String sql = " select * from uaa_menu order by sort asc";
        List<Map<String,String>> menuList= CollectionUtil.objectToString(jdbcUtilUse.getJdbcTemplate().queryForList(sql));
        return menuList;
    }

    public List<Map<String,Object>> getEnumerate(){
        String sql="SELECT * FROM UAA_Enumerate where class='菜单类别'";
        return jdbcUtilUse.getJdbcTemplate().queryForList(sql);
    }
    // 删除菜单
    public void deleteMenu(JSONObject jsonObject)throws Exception{
        System.out.println(jsonObject);
        JSONObject o = jsonObject.getJSONObject("taskList");
        JSONArray jsonArrays = o.getJSONArray("UAA_MENU");
        String id=StringUtil.getString(jsonArrays.getJSONObject(0).getString("id"));
        String path=StringUtil.getString(jsonArrays.getJSONObject(0).getString("path"));
        String sql="";
        if(!path.equals("")){
            sql = "delete from uaa_menu where path like '"+path+"%';";
            sql =sql+" delete from uaa_menu_function where menu_id in (select id  from uaa_menu  where path like'"+path+"%');";
            sql =sql +" delete from uaa_function_interface where fid in (select id from uaa_menu  where path like '"+path+"%'); ";
            jdbcUtilUse.getJdbcTemplate().update(sql);
        }else {
            sql = "delete from uaa_menu where id = ? ;";
            sql = sql + " delete from uaa_menu_function where menu_id =? ;";
            sql = sql + " delete from uaa_function_interface where fid in (select id from uaa_menu_function where  menu_id =?); ";
            jdbcUtilUse.getJdbcTemplate().update(sql,id,id,id);
        }

    }
    public List<Map<String,Object>> getMenuFunction(@RequestBody JSONObject jsonObject)throws Exception{
        String pk_Value = jsonObject.getString("pkValue");
        String sql = " select * from uaa_menu_function where menuid=? ";
        List<Map<String,Object>> List = jdbcUtilUse.getJdbcTemplate().queryForList(sql,pk_Value);
        return List;
    }
    //删除功能菜单
    public void deleteFunction(JSONObject jsonObject)throws Exception{
        JSONObject o = jsonObject.getJSONObject("FormData");
        String id = o.getString("id");

        String sql = " begin;" +
                "delete from uaa_menu_function where id = ?;"+
                "delete from uaa_function_interface where fid=?;"+
                "commit;";
        jdbcUtilUse.getJdbcTemplate().update(sql,id,id);
    }
    //
    public List<Map<String,Object>> initIndexMenu(){
        List<Map<String,Object>> list = null;
        String sql = " select um.*,umf.* from uaa_menu um left join " +
                     " (select menu_id,GROUP_CONCAT(type) func from uaa_menu_function group by menu_id) umf on um.id = umf.menu_id order by um.sort";
        list = jdbcUtilUse.getJdbcTemplate().queryForList(sql);
        return list;
    }

    public String getNextVal() throws Exception {
        String result="";
        try {
            Object obj = BeanUtil.popBean("Exp_DefaultValue");
            Class c = obj.getClass();
            Method m = c.getMethod("getPrimaryKey", String.class);
            result= (String) m.invoke(obj, (String)null);
        }catch(Exception e){
            throw new Exception(e);
        }
        return result;
    }
    public int getMaxSer(){
        String sql="select max(sort) as MaxSer from uaa_menu";
        Map<String,Object> map=this.jdbcUtilUse.getJdbcTemplate().queryForMap(sql);
        if(map.get("MaxSer")==null){
            return 1;
        }
        Integer big= (Integer) map.get("MaxSer");
        return big;
    }
}
