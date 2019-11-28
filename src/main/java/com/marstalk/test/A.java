package com.marstalk.test;

import com.marstalk.annotation.Autowired;
import com.marstalk.annotation.Component;

/**
 * @author Mars
 * Created on 11/26/2019
 */
@Component
public class A {

    @Autowired
    private B b;

    public void hi(){
        System.out.println(this);
        System.out.println("Hi");
        System.out.println(b);
        b.hi();
    }

    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
    }
}
