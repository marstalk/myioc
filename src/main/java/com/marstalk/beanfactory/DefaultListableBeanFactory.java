package com.marstalk.beanfactory;

import com.marstalk.parser.ConfigurationClassParser;
import com.marstalk.register.BeanDefinition;
import com.marstalk.register.Registry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mars
 * Created on 11/26/2019
 */
public class DefaultListableBeanFactory implements BeanFactory, Registry {

    protected final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);
    protected final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);
    protected final Map<Class<?>, String[]> allBeanNameByType = new ConcurrentHashMap<>(64);
    protected final Set<String> beanDefinitionNames = new HashSet<>();


    @Override
    public Object getBean(String name) {
        return this.singletonObjects.get(name);
    }

    @Override
    public <V> V getBean(Class<V> clazz) {
        return null;
    }

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanDefinitionMap.put(beanName, beanDefinition);
        beanDefinitionNames.add(beanName);

        if (allBeanNameByType.get(beanDefinition.getClassType()) == null) {
            allBeanNameByType.put(beanDefinition.getClassType(), new String[]{beanName});
        }else{
            String[] strings = allBeanNameByType.get(beanDefinition.getClassType());
            String[] strings1 = Arrays.copyOf(strings, strings.length + 1);
            strings1[strings1.length - 1] = beanName;
            allBeanNameByType.put(beanDefinition.getClassType(), strings1);
        }
    }

    public Map<String, Object> getSingletonObjects() {
        return singletonObjects;
    }

    public Map<String, BeanDefinition> getBeanDefinitionMap() {
        return beanDefinitionMap;
    }

}
