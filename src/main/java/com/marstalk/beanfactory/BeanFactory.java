package com.marstalk.beanfactory;

/**
 * @author Mars
 * Created on 11/26/2019
 */
public interface BeanFactory {
    Object getBean(String name);

    <V> V getBean(Class<V> clazz);
}
