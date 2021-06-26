package com.bobooi.watch.common.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * @author bobo
 * @date 2021/3/31
 */

@Component
@Slf4j
public class BeanHelper implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    private static final int RANDOM_BEAN_NAME_LENGTH = 6;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        BeanHelper.applicationContext = applicationContext;
    }

    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static <T> T getBean(Class<T> tClass) {
        return applicationContext.getBean(tClass);
    }

    public static <T> T getBean(String name, Class<T> tClass) {
        return applicationContext.getBean(name, tClass);
    }

    public static void registerBean(Class<?> beanClass, String name, Object... constructorArgs) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanClass);
        for (Object arg : constructorArgs) {
            beanDefinitionBuilder.addConstructorArgValue(arg);
        }
        BeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
        BeanDefinitionRegistry beanFactory = (BeanDefinitionRegistry) ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
        String beanName = Optional.ofNullable(name).orElse(beanClass.getName() + "$" + RandomStringUtils.randomAlphanumeric(RANDOM_BEAN_NAME_LENGTH));
        beanFactory.registerBeanDefinition(beanName, beanDefinition);
    }

    /**
     * 借助内省，通过副作用拷贝非空属性
     *
     * @param source        属性来源对象
     * @param target        待设置目标对象
     * @param filedExcluded 忽略的字段
     */
    public static void copyPropertiesNotNull(Object source, Object target, String... filedExcluded) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(source.getClass(), Object.class);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            List<String> toIgnore = new LinkedList<>(Arrays.asList(filedExcluded));
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                Object value = propertyDescriptor.getReadMethod().invoke(source);
                if (value == null) {
                    toIgnore.add(propertyDescriptor.getName());
                }
            }
            BeanUtils.copyProperties(source, target, toIgnore.toArray(new String[0]));
        } catch (Exception e) {
            log.error("拷贝属性出错 {} -> {}", source, target, e);
        }
    }
}
