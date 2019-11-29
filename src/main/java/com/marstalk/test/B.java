package com.marstalk.test;

import com.marstalk.annotation.Autowired;
import com.marstalk.annotation.Component;

/**
 * @author Mars
 * Created on 11/26/2019
 */
@Component
public class B {

    @Autowired
    private A a;

    public void hi(){
        System.out.println(this + " say Hi" + ", contains a = " + a);
    }
}
