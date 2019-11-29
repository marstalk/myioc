package com.marstalk.test;

import com.marstalk.annotation.ComponentScan;
import com.marstalk.beanfactory.ApplicationContext;

import java.lang.reflect.InvocationTargetException;

/**
 * Hello world!
 *
 */

@ComponentScan(value = "com.marstalk.test")
public class App 
{
    public static void main( String[] args ) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        ApplicationContext applicationContext = new ApplicationContext(App.class);
        A a = (A)applicationContext.getBean("a");
        a.hi();

        B b = applicationContext.getBean(B.class);
        b.hi();
    }
}
