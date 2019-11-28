package com.marstalk.beanfactory;

import com.marstalk.annotation.Autowired;
import com.marstalk.annotation.Component;
import com.marstalk.annotation.ComponentScan;
import com.marstalk.register.BeanDefinition;
import com.marstalk.utils.FileUtils;
import com.marstalk.utils.StringUtils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Mars
 * Created on 11/26/2019
 */
public class ApplicationContext implements BeanFactory {
    private DefaultListableBeanFactory defaultListableBeanFactory;

    public ApplicationContext() {
        this.defaultListableBeanFactory = new DefaultListableBeanFactory();
    }

    public ApplicationContext(Class clazz) {
        this();
        try {
            //scan
            scanAndParse(clazz, defaultListableBeanFactory);
            //initializeBean
            initializationBean();
            //clear middle status
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    private void initializationBean() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        Set<String> beanDefinitionNames = defaultListableBeanFactory.beanDefinitionNames;
        Iterator<String> iterator = beanDefinitionNames.iterator();
        while (iterator.hasNext()) {
            String beanName = iterator.next();
            //new
            initializebean(beanName);
        }
    }

    private Object initializebean(String beanName) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        BeanDefinition bd = defaultListableBeanFactory.beanDefinitionMap.get(beanName);
        Object o = getObject(bd);

        //autowired
        populate(beanName, bd, o);

        defaultListableBeanFactory.getSingletonObjects().put(beanName, o);
        return o;
    }

    private void populate(String beanName, BeanDefinition bd, Object o) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Field[] declaredFields = bd.getClassType().getDeclaredFields();
        for (Field field : declaredFields) {
            Autowired annotation = field.getAnnotation(Autowired.class);
            if (annotation != null) {
                Class<?> type = field.getType();
                Object bean = getBean(type);
                if (bean != null) {
                    Method declaredMethod = bd.getClassType().getDeclaredMethod("set" + StringUtils.captureName(field.getName()), field.getType());
                    declaredMethod.invoke(o, bean);
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
        Set<File> files = scan(clazz);
        if (null == files || files.isEmpty()) {
            return;
        }

        for (File file : files) {
            BeanDefinition db = parse(file);
            if (null != db) {
                defaultListableBeanFactory.registerBeanDefinition(db.getName(), db);
            }
        }

        System.out.println(defaultListableBeanFactory.getBeanDefinitionMap());

    }

    private BeanDefinition parse(File file) {
        Class<?> aClass = null;
        String absolutePath = file.getAbsolutePath();
        String clazz = absolutePath.replace(FileUtils.rootPath(), "").replace(".class", "").replace(File.separatorChar, '.');

        try {
            aClass = Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (aClass.getAnnotation(Component.class) == null) {
            return null;
        }

        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setClassType(aClass);
        beanDefinition.setName(aClass.getSimpleName());

        return beanDefinition;
    }

    private Set<File> scan(Class clazz) {
        ComponentScan annotation = (ComponentScan) clazz.getAnnotation(ComponentScan.class);
        if (annotation == null) {
            System.out.println("Invalid configuration class");
            System.exit(-1);
            return null;
        }
        String scanPackagePath = annotation.value();

        String scanPath = new StringBuilder(FileUtils.rootPath()).append(scanPackagePath.replace(".", File.separator)).toString();
        HashSet<File> files = new HashSet<>();
        FileUtils.listAllFiles(new File(scanPath), files);
        System.out.println(files);

        Set<File> filteredSet = files.stream().filter(file -> file.getName().endsWith(".class")).collect(Collectors.toSet());

        return filteredSet;
    }

    @Override
    public Object getBean(String name) {
        Object bean = this.defaultListableBeanFactory.getBean(name);
        if (bean != null) {
            return bean;
        }

        try {
            return initializebean(name);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public <V> V getBean(Class<V> clazz) {
        String[] strings = defaultListableBeanFactory.allBeanNameByType.get(clazz);
        if (strings.length > 1) {
            System.err.println("duplicated bean found");
        }

        if (null == strings || strings.length == 0) {
            System.err.println("no candidate bean found");
        }
        String beanName = strings[0];
        return (V)getBean(beanName);
    }
}
