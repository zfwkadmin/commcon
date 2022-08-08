package com.zqazfl.common.permission;

import com.zqazfl.common.utils.StringUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NodeTest {
    public static List<com.zqazfl.common.permission.Node> getDatas(List<Map<String, Object>> lists) {
        if (lists != null && lists.size() > 0) {
            // 获取顶层元素
            List<Map<String, Object>> firstLevel = lists.stream().filter(m -> ((BigDecimal) m.get("PARENTID")).intValue() == 0).collect(Collectors.toList());
            return generateTree(firstLevel, lists);
        }
        return null;
    }


    public static List<com.zqazfl.common.permission.Node> generateTree(List<Map<String, Object>> parentList, List<Map<String, Object>> allList) {
        List<com.zqazfl.common.permission.Node> result = new ArrayList<>();
        if (parentList != null && parentList.size() > 0) {
            for (Map map : parentList) {
                com.zqazfl.common.permission.Node node = new com.zqazfl.common.permission.Node();
                node.setId(( StringUtil.getString(map.get("ID"))));
                node.setLabel((String) map.get("NAME"));
                List<Map<String, Object>> children = allList.stream().filter(m -> ((BigDecimal) m.get("PARENTID")).intValue()
                        == ((BigDecimal) (map.get("ID"))).intValue()).collect(Collectors.toList());
                node.setChildren(generateTree(children, allList));
                result.add(node);
            }
        }
        return result;
    }

    public static List<com.zqazfl.common.permission.Node> getMenuDatas(List<Map<String, Object>> lists) {
        if (lists != null && lists.size() > 0) {
            // 获取顶层元素 ((BigDecimal) m.get("SUPMENUID")).intValue() == 0
            List<Map<String, Object>> firstLevel = lists.stream().filter(m -> StringUtil.getString(m.get("SUPMENUID")).isEmpty()).collect(Collectors.toList());
            return generateTrees(firstLevel, lists);
        }
        return null;
    }

    public static List<com.zqazfl.common.permission.Node> generateTrees(List<Map<String, Object>> parentList, List<Map<String, Object>> allList) {
        List<com.zqazfl.common.permission.Node> result = new ArrayList<>();
        if (parentList != null && parentList.size() > 0) {
            for (Map map : parentList) {
                com.zqazfl.common.permission.Node node = new com.zqazfl.common.permission.Node();
                node.setId(( StringUtil.getString(map.get("ID"))));
                node.setLabel((String) map.get("MENUNAME"));
                List<Map<String, Object>> children = allList.stream().filter(m -> ( StringUtil.getString(m.get("SUPMENUID")).equals(StringUtil.getString(map.get("ID"))))
                        ).collect(Collectors.toList());
                node.setChildren(generateTrees(children, allList));
                result.add(node);
            }

        }
        return result;
    }
    public static List<com.zqazfl.common.permission.Node> getNotes(List<Map<String, Object>> lists){
        List<com.zqazfl.common.permission.Node> result = new ArrayList<>();
        ArrayList<String> arrayList = new ArrayList<String>();
        List<Map<String, Object>> firstLevel = new ArrayList<Map<String, Object>>();

        if(lists != null && lists.size() > 0){
            for(Map map : lists){
                if(!StringUtil.getString(map.get("ORGID")).isEmpty()&&!arrayList.contains(StringUtil.getString(map.get("ORGID")))){
                    arrayList.add(StringUtil.getString(map.get("ORGID")));
                    firstLevel.add(map);
                }
                if(StringUtil.getString(map.get("ORGID")).isEmpty()){
                    firstLevel.add(map);
                }
            }
            return generateToTree(firstLevel, lists);
        }
        return  null;
    }
    public static List<com.zqazfl.common.permission.Node> generateToTree(List<Map<String, Object>> parentList, List<Map<String, Object>> allList){
        List<com.zqazfl.common.permission.Node> result = new ArrayList<>();
        if (parentList != null && parentList.size() > 0) {
            for (Map map : parentList) {
                com.zqazfl.common.permission.Node node = new com.zqazfl.common.permission.Node();
                if(StringUtil.getString(map.get("ORGID")).isEmpty()){
                    List<com.zqazfl.common.permission.Node> nodes = new ArrayList<>();
                    node.setId(( StringUtil.getString(map.get("ID"))));
                    node.setLabel((String) map.get("GWMC"));
                    node.setChildren(nodes);
                    result.add(node);
                }else{
                    node.setId(( StringUtil.getString(map.get("ORGID"))));
                    node.setLabel((String) map.get("ORGNAME"));
                    List<Map<String, Object>> children = allList.stream().filter(m -> ( StringUtil.getString(m.get("ORGID")).equals(StringUtil.getString(map.get("ORGID"))))
                    ).collect(Collectors.toList());
                    node.setChildren(generateToTree(null,children));
                    result.add(node);
                }
            }
        }else{
            for(Map map : allList){
                com.zqazfl.common.permission.Node nodes = new com.zqazfl.common.permission.Node();
                List<com.zqazfl.common.permission.Node> nodes1 = new ArrayList<>();
                nodes.setId(( StringUtil.getString(map.get("ID"))));
                nodes.setLabel((String) map.get("GWMC"));
                nodes.setChildren(nodes1);
                result.add(nodes);
           }
        }
        return result;
    }

    // 单位类型查询
    public static List<com.zqazfl.common.permission.Node> changeToTree(List<Map<String, Object>> lists){
        List<com.zqazfl.common.permission.Node> result = new ArrayList<>();
        if(lists!=null || lists.size()>0){
            for(Map map : lists){
                com.zqazfl.common.permission.Node node = new com.zqazfl.common.permission.Node();
                List<com.zqazfl.common.permission.Node> nodes1 = new ArrayList<>();
                node.setId(( StringUtil.getString(map.get("ID"))));
                node.setLabel((String) map.get("NAME"));
                node.setChildren(nodes1);
                result.add(node);
            }
        }
        return result;
    }
    //
    public static List<com.zqazfl.common.permission.Node> funChangeToTree(List<Map<String, Object>> lists){
        List<com.zqazfl.common.permission.Node> result = new ArrayList<>();
        if(lists!=null || lists.size()>0){
            com.zqazfl.common.permission.Node node = new com.zqazfl.common.permission.Node();
            node.setId(( StringUtil.getString(lists.get(0).get("ID"))));
            node.setLabel((String) lists.get(0).get("DWXZ"));
            List<com.zqazfl.common.permission.Node> node_list = new ArrayList<>();
            List<com.zqazfl.common.permission.Node> node_list1 = new ArrayList<>();
            for(Map map : lists){
                com.zqazfl.common.permission.Node node1 = new com.zqazfl.common.permission.Node();
                node1.setId(( StringUtil.getString(map.get("ID"))));
                node1.setLabel((String) map.get("NAME"));
                node1.setChildren(node_list1);
                node_list.add(node1);
            }
            node.setChildren(node_list);
            result.add(node);
        }
        return result;
    }
   /* public static List<Node> funChangeToTree(List<Node> parentList, List<Map<String, Object>> funList){
        List<Node> result = new ArrayList<>();
        if(parentList!=null || parentList.size()>0){
            for(Node node:parentList){
                List<Map<String,Object>> list = funList.stream().filter(m->StringUtil.getString(m.get("MENUID")).equals(StringUtil.getString(node.getId()))).collect(Collectors.toList());
                if(list.size()>0){
                    for(Map maps : list){
                        List<Node> node_list = new ArrayList<>();
                        Node nodes = new Node();
                        nodes.setId(( StringUtil.getString(maps.get("ID"))));
                        nodes.setLabel((String) maps.get("NAME"));
                        nodes.setChildren(node_list);
                        result.add(nodes);
                    }
                }
                if(node.getChildren().size()>0){
                    funChangeToTree(node.getChildren(),list);
                }

            }
        }
        return result;
    }
*/
   public static List<com.zqazfl.common.permission.Node> getConData(List<Map<String, Object>> lists) {
       if (lists != null && lists.size() > 0) {
           List<Object> array_list = new ArrayList<Object>();
           List<Map<String, Object>> firstLevel = new ArrayList<>();
           for(int i=0;i<lists.size();i++){
               if(StringUtil.getString(lists.get(i).get("PARENTID")).isEmpty()){
                   if(!array_list.contains(lists.get(i).get("ID"))){
                       array_list.add(lists.get(i).get("ID"));
                       firstLevel.add(lists.get(i));
                   }
               }else{
                   if(!array_list.contains(lists.get(i).get("PARENTID"))){
                       array_list.add(lists.get(i).get("PARENTID"));
                       firstLevel.add(lists.get(i));
                   }
               }
           }
          // List<Map<String, Object>> firstLevel = lists.stream().filter(m -> !StringUtil.getString(m.get("PARENTID")).isEmpty()).collect(Collectors.toList());
           return generateTr(firstLevel, lists);

       }
       return null;
   }
    public static List<com.zqazfl.common.permission.Node> generateTr(List<Map<String, Object>> parentList, List<Map<String, Object>> allList) {
        List<com.zqazfl.common.permission.Node> result = new ArrayList<>();
        if (parentList != null && parentList.size() > 0) {
            for (Map map : parentList) {
                com.zqazfl.common.permission.Node node = new com.zqazfl.common.permission.Node();
                if(StringUtil.getString(map.get("PARENTID")).isEmpty()){
                    List<com.zqazfl.common.permission.Node> node_list = new ArrayList<>();
                    node.setId(( StringUtil.getString(map.get("ID"))));
                    node.setLabel((String) map.get("NAME"));
                    node.setChildren(node_list);
                }else{
                    node.setId(( StringUtil.getString(map.get("PARENTID"))));
                    node.setLabel((String) map.get("PARENTNAME"));
                    List<Map<String, Object>> children = allList.stream().filter(m -> ( StringUtil.getString(m.get("PARENTID")).equals(StringUtil.getString(map.get("PARENTID"))))
                    ).collect(Collectors.toList());
                    List<com.zqazfl.common.permission.Node> list_node = new ArrayList<>();
                    for(Map map1 : children){
                        com.zqazfl.common.permission.Node node1 = new com.zqazfl.common.permission.Node();
                        List<Node> child_node = new ArrayList<>();
                        node1.setId(( StringUtil.getString(map1.get("ID"))));
                        node1.setLabel((String) map1.get("NAME"));
                        node1.setChildren(child_node);
                        list_node.add(node1);
                    }
                    node.setChildren(list_node);
                }

                result.add(node);
            }

        }
        return result;
    }
}
