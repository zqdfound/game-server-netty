package com.mars.dor.netty.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author zhuangqingdian
 * @date 2022/8/17
 */

@Component
public class SpringUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    /**
     * 重写父类方法
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if(SpringUtils.applicationContext == null){
            SpringUtils.applicationContext  = applicationContext;
        }
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    public static Object getBean(String name){
        return getApplicationContext().getBean(name);
    }
    public static <T> T getBean(Class<T> clazz){
        return getApplicationContext().getBean(clazz);
    }

}
