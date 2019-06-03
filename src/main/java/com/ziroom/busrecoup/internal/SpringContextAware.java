package com.ziroom.busrecoup.internal;

import com.ziroom.busrecoup.IRecoup;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by Yangjy on 2018/8/8.
 */
@Component
public class SpringContextAware implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    public IRecoup getIRecoupBean(String jobClass) {
        IRecoup recoupBean = null;

        try {
            int n = jobClass.lastIndexOf(".") + 1;
            String simpleJobClassName = jobClass.substring(n);
            simpleJobClassName = simpleJobClassName.substring(0, 1).toLowerCase() + simpleJobClassName.substring(1);
            recoupBean = (IRecoup) applicationContext.getBean(simpleJobClassName);
        } catch (BeansException e) {
        }

        if (recoupBean == null) {
            try {
                Class beanClass = Class.forName(jobClass, false, applicationContext.getClassLoader());
                recoupBean = (IRecoup) applicationContext.getBean(beanClass);
            } catch (Exception e) {
            }
        }
        return recoupBean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
