package com.orion.mobile.gateway.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 *@Description TODO
 *@Author potsmart
 *@Date 2019/4/24 17:26
 *@Version 1.0.0
 */
@Component
public class GatewaySpringContextUtils implements ApplicationContextAware {
	static  ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public static <T> T getBean(String beanName)
	{
		return (T) applicationContext.getBean(beanName);
	}

	public static  <T> T getBean(Class beanName) {
		return (T) applicationContext.getBean(beanName);
	}
}
