package com.zqazfl.common.utils;

import java.lang.reflect.Method;

public class ReflectMethod {

    /**
     * 反射方法
     * @return
     */
    public String getResult(String defaultValues) throws Exception{
        ReflectMethod reflectMethod = new ReflectMethod();
        String className = null;
        String methodName = null;
        String column = null;
        if(defaultValues.split("\\.").length == 2){
            className =  defaultValues.substring(0,defaultValues.indexOf("."));
            methodName = defaultValues.substring(defaultValues.lastIndexOf(".")+1);
        }else if(defaultValues.split("\\.").length == 3){
            className =  defaultValues.substring(0,defaultValues.indexOf("."));
            methodName = defaultValues.substring(defaultValues.indexOf(".")+1,defaultValues.lastIndexOf("."));
            column = defaultValues.substring(defaultValues.lastIndexOf(".")+1);
        }else {
            throw new Exception("参数配置错误！应该是 xxx.xxx 或 xxx.xxx.xxx ");
        }

        //System.out.println("类名="+className+" : "+"方法名="+methodName+" : "+"参数="+column);
        //className = "com.zqaz.expression."+className;
        String result = "";
        //执行代理方法
        try {
            Object obj = BeanUtil.popBean("Exp_"+className);
            Class c = obj.getClass();
            Method m = c.getMethod(methodName, String.class);
            result= (String) m.invoke(obj, column);
        }catch(Exception e){
            throw new Exception(e);
        }


        return result;
    }


}
