package com.marstalk.register;

/**
 * @author Mars
 * Created on 11/26/2019
 */
public class BeanDefinition {
    private Class classType;
    private String name;

    public Class getClassType() {
        return classType;
    }

    public void setClassType(Class classType) {
        this.classType = classType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
