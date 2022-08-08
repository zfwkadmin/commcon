package com.zqazfl.common.permission;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/permission")
public class PermissionController {
    @Autowired
    PermissionService permissionService;

    @RequestMapping(value = "/getMenu")
    public List<Node> getMenu(@RequestBody Map<String, Object> params) throws Exception {
        return permissionService.getMenu(params);
    }

    @RequestMapping(value = "/getAuthObj")
    public List<Node> getAuthObj(@RequestBody Map<String, Object> params) throws Exception{
        return permissionService.getAuthObj(params);
    }

    @RequestMapping(value = "/getPermission")
    public List<Map<String, Object>> getPermission(@RequestBody Map<String, Object> params) throws Exception {
        return permissionService.getPermission(params);
    }
    @Transactional
    @RequestMapping(value = "/savePermission")
    public void savePermission(@RequestBody Map<String, Object> params) throws Exception {
        permissionService.savePermission(params);
    }

    @RequestMapping(value = "/getUserMenu")
    public List<Map<String, String>> getUserMenu() throws Exception {
        return permissionService.getUserMenu();
    }


}
