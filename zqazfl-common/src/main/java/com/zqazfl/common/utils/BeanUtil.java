package com.zqazfl.common.utils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * @Description: 获得bean
 * @author: zhenghao
 * @date: 2019/12/14 12:04
 */
@Service
public  class BeanUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {

        applicationContext = context;
    }

    /**
     * 获取bean
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T popBean(Class<T> clazz) {
        //先判断是否为空
        if (applicationContext == null) {
            return null;
        }
        return applicationContext.getBean(clazz);
    }


    public static <T> T popBean(String name, Class<T> clazz) {
        if (applicationContext == null) {
            return null;
        }

        return applicationContext.getBean(name, clazz);

    }
    public static <T> Object popBean(String name) {
        if (applicationContext == null) {
            return null;
        }

        return applicationContext.getBean(name);

    }
}