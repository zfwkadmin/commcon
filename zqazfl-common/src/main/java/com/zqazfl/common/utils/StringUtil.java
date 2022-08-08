package com.zqazfl.common.utils;

import java.util.*;
import java.util.Map.Entry;

public class StringUtil {
    /**
     * 判断字符串是否为空
     * @param str
     * @return true or false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 判断字符串是否不为空
     * @param str
     * @return true or false
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 对象转字符串
     * @param obj
     * @return
     */
    public static String getString(Object obj){
        if(obj!=null){
            return obj.toString();
        }else{
            return "";
        }
    }

    /**
     *
     * isEqual(比较两个字符串的内容)
     * @Title: isEqual
     * @Description: TODO
     * @param @param s1
     * @param @param s2
     * @param @return    设定文件
     * @return boolean    返回类型 两个字符串内容相同、返回true、其他返回false
     * @throws
     */
    public static boolean isEqual(String s1, String s2) {
        if (s1 == null) {
            s1 = "";
        }
        if (s2 == null) {
            s2 = "";
        }

        return (s1.equals(s2));
    }

    /**
     * 拼装签名  xl 2017-12-18
     * @param map 所有参数
     * @param serect 开发者识别号
     * @return
     * @throws Exception
     */
    public static String creatSign(Map<String, Object> map, String serect) throws Exception{
        //获取主要参数拼装签名
        String Signstr="";
        String sign="";
        Signstr+=serect;

        //参数排序数组
        List<String> sortList=new ArrayList<String>();
        for(Entry<String, Object> entry : map.entrySet()){
            String key=StringUtil.getString(entry.getKey());
            if(!StringUtil.isEqual(key, "_sign")){
                sortList.add(key);
            }
        }
        Collections.sort(sortList,new Comparator<String>()
                {

                    public int compare(String o1, String o2){
                        return o1.compareTo(o2);
                    }
                }
        );
        for(int i=0;i<sortList.size();i++){
            String value=StringUtil.getString(map.get(sortList.get(i)));
            Signstr+=StringUtil.getString(sortList.get(i))+value;
        }
        Signstr+=serect;
        sign=MD5.getMD5(Signstr.getBytes("UTF-8"));

        return sign;
    }

    public static String sumString(List<Map<String, Object>> data,String field,String connector){
        StringBuilder sb=new StringBuilder();
        data.forEach(item->{
            sb.append(item.get(field)).append(connector);
        });
        sb.delete(sb.length()-connector.length(),sb.length());
        return sb.toString();
    }
}
