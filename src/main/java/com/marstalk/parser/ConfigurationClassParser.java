package com.marstalk.parser;

import com.marstalk.annotation.Component;
import com.marstalk.annotation.ComponentScan;
import com.marstalk.register.BeanDefinition;
import com.marstalk.register.Registry;
import com.marstalk.utils.FileUtils;
import com.marstalk.utils.StringUtils;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Mars
 * Created on 11/26/2019
 */
public class ConfigurationClassParser {

    public void parse(Class clazz, Registry registry) {
        Set<File> files = scan(clazz);
        if (null == files || files.isEmpty()) {
            return;
        }

        for (File file : files) {
            BeanDefinition db = parse(file);
            if (null != db) {
                registry.registerBeanDefinition(db.getName(), db);
            }
        }
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
        beanDefinition.setName(StringUtils.lowerCaseFirstLetter(aClass.getSimpleName()));

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
}
