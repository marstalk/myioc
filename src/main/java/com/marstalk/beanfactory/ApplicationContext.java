package com.marstalk.beanfactory;

import com.marstalk.annotation.Autowired;
import com.marstalk.parser.ConfigurationClassParser;
import com.marstalk.register.BeanDefinition;
import com.marstalk.utils.ObjectFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * @author Mars
 * Created on 11/26/2019
 */
public class ApplicationContext implements BeanFactory {
    private DefaultListableBeanFactory defaultListableBeanFactory;
    private ConfigurationClassParser parser;

    public ApplicationContext() {
        this.defaultListableBeanFactory = new DefaultListableBeanFactory();
    }

    public ApplicationContext(Class clazz) {
        this();
        parser = new ConfigurationClassParser();

        //refresh之前，把创世界类扫面到bdm中。

        refresh(clazz);
    }

    private void refresh(Class clazz) {
        try {
            //scan：完成beanDefinitionMap
            scanAndParse(clazz, defaultListableBeanFactory);

            //initializeBean: 对于单例bean，提前初始化
            finishBeanFactoryInitialization();

            //clear middle status
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void finishBeanFactoryInitialization() {
        ArrayList<String> beanNames = new ArrayList<>(defaultListableBeanFactory.beanDefinitionNames);
        for (String beanName : beanNames) {
            //TODO 当前只支持单例。
            getBean(beanName);
        }
    }

    public Object createBean(String beanName) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        BeanDefinition bd = defaultListableBeanFactory.beanDefinitionMap.get(beanName);
        if (bd == null) {
            return null;
        }

        //推断构造器，使用反射实例化。
        Object o = getObject(bd);

        //在处理autowired之前，提前暴露自己。
        exposeObject(beanName, o);

        //autowired
        populate(bd, o);

        //TODO
        //1， 处理autowired
        //2， 处理生命周期回调
        //3， 调用beanPostProcessor

        defaultListableBeanFactory.getSingletonObjects().put(beanName, o);

        //清理中间状态
        defaultListableBeanFactory.earlySingletonObjects.remove(beanName);
        defaultListableBeanFactory.currentlyInCreation.remove(beanName);

        return o;
    }

    private void exposeObject(String beanName, Object o) {
        defaultListableBeanFactory.earlySingletonObjects.put(beanName, o);
    }

    private void populate(BeanDefinition bd, Object o) {
        Field[] declaredFields = bd.getClassType().getDeclaredFields();
        for (Field field : declaredFields) {
            Autowired annotation = field.getAnnotation(Autowired.class);
            if (annotation != null) {
                Class<?> type = field.getType();
                Object bean = getBean(type);
                if (bean != null) {
                    boolean accessible = field.isAccessible();
                    field.setAccessible(true);
                    try {
                        field.set(o, bean);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        field.setAccessible(accessible);
                    }
                }
            }
        }
    }

    private Object getObject(BeanDefinition bd) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Class classType = bd.getClassType();
        Constructor[] declaredConstructors = classType.getDeclaredConstructors();
        Constructor declaredConstructor = declaredConstructors[0];
        return declaredConstructor.newInstance();
    }

    private void scanAndParse(Class clazz, DefaultListableBeanFactory defaultListableBeanFactory) {
        parser.parse(clazz, defaultListableBeanFactory);

        //调用beanFactoryProcessor
    }

    @Override
    public Object getBean(String name) {
        Object bean = getSingleton(name);

        if (bean != null) {
            return bean;
        }

        return getSingleton(name, () -> {
            try {
                return createBean(name);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public Object getSingleton(String beanName) {
        Object singletonObject = defaultListableBeanFactory.singletonObjects.get(beanName);
        if (singletonObject == null && defaultListableBeanFactory.currentlyInCreation.contains(beanName)) {
            singletonObject = defaultListableBeanFactory.earlySingletonObjects.get(beanName);
        }
        return singletonObject;
    }

    public Object getSingleton(String beanName, ObjectFactory factory) {

        beforeSingletonCreation(beanName);
        Object o = defaultListableBeanFactory.getSingletonObjects().get(beanName);
        if (o == null) {
            //TODO 处理代理。
            return factory.get();
        }
        return o;
    }

    private void beforeSingletonCreation(String beanName) {
        defaultListableBeanFactory.currentlyInCreation.add(beanName);
    }

    @Override
    public <V> V getBean(Class<V> clazz) {
        String[] strings = defaultListableBeanFactory.allBeanNameByType.get(clazz);
        if (null == strings || strings.length == 0) {
            System.err.println("no candidate bean found");
            return null;
        }

        if (strings.length > 1) {
            System.err.println("duplicated bean found");
        }

        String beanName = strings[0];
        return (V) getBean(beanName);
    }
}
