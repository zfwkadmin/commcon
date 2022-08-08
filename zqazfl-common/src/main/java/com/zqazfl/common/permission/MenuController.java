package com.zqazfl.common.permission;

import com.alibaba.fastjson.JSONObject;
import com.zqazfl.common.form.FormMethod;
import com.zqazfl.common.utils.CollectionUtil;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/menu")
public class MenuController {
    @Autowired
    MenuService menuService;
    @Autowired
    FormMethod formMethod;

    @Transactional
    @RequestMapping("/saveMenu")
    public void saveMenu(@RequestBody JSONObject jsonObject)throws Exception{
        JSONObject o = jsonObject.getJSONObject("taskList");
        jsonObject.put("taskList",o);
        JSONObject row=(JSONObject)o.getJSONArray("UAA_MENU").get(0);
        String url=row.getString("url");
        row.put("url",StringEscapeUtils.unescapeHtml4(url));
        if(row.get("use_type").equals("add")){
            row.put("sort",menuService.getMaxSort((String) row.get("supmenu_id")));
        }

        formMethod.saveForm(jsonObject,"/form/UAA_MENU/addMenu.json");
       // menuService.saveMenu(jsonObject);

    }
    @RequestMapping("/menuModify")
    public void menuModifu(@RequestBody JSONObject jsonObject){
        menuService.menuModify(jsonObject);
    }

    @RequestMapping("/getMenuDe")
    public Map<String,Object> getMenuDe(@RequestBody JSONObject jsonObject)throws Exception{
       return formMethod.loadForm(jsonObject,"/form/UAA_MENU/addMenu.json");
    }

    @RequestMapping("/getEnumerate")
    public List<Map<String,Object>> getBalance() throws Exception{
        return menuService.getEnumerate();
    }

    // 获取菜单ID
    @RequestMapping("/getMenuID")
    public String getMenuID()throws Exception{
        return  menuService.getMenuId();
    }
    // 加载菜单
    @RequestMapping("/getMenu")
    public List<Map<String,String>> getMenu()throws Exception{
        return  menuService.getMenu();
    }
    // 删除菜单
    @Transactional
    @RequestMapping("/deleteMenu")
    public void deleteMenu(@RequestBody JSONObject jsonObject)throws Exception{
        menuService.deleteMenu(jsonObject);
    }
    // 保存功能菜单
    @Transactional
    @RequestMapping("/saveMenuFunction")
    public void saveMenuFunction(@RequestBody JSONObject jsonObject)throws Exception{
        formMethod.saveForm(jsonObject,"/form/UAA_MENU/Function.json");
    }
    // 查询菜单功能信息
    @RequestMapping("/getMenuFunction")
    public List<Map<String,Object>> getMenuFunction(@RequestBody JSONObject jsonObject)throws Exception{
        return menuService.getMenuFunction(jsonObject);
    }
    // 保存菜单功能接口
    @Transactional
    @RequestMapping("/saveFunInterface")
    public void saveFunInterface(@RequestBody JSONObject jsonObject)throws Exception{
        formMethod.saveForm(jsonObject,"/form/UAA_MENU/MenuFunction.json");
    }
    // 查询菜单功能信息
    @RequestMapping("/getFunInterFace")
    public Map<String,Object> getFunInterFace(@RequestBody JSONObject jsonObject)throws Exception{
        return formMethod.loadForm(jsonObject,"/form/UAA_MENU/MenuFunction.json");
    }
    // 删除功能菜单
    @RequestMapping("/deleteFunction")
    public void deleteFunction(@RequestBody JSONObject jsonObject)throws Exception{
        menuService.deleteFunction(jsonObject);
    }
    // 加载首页菜单
    // 删除功能菜单
    @RequestMapping("/initIndexMenu")
    public List<Map<String,String>> initIndexMenu()throws Exception{
       return CollectionUtil.objectToString(menuService.initIndexMenu());
    }

    @RequestMapping("/getNextVal")
    public String getNextVal() throws Exception {
        return menuService.getNextVal();
    }
    @RequestMapping("/getMaxSer")
    public int getMaxSer(){
        return menuService.getMaxSer();
    }


}
