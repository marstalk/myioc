package com.marstalk.utils;

/**
 * @author Mars
 * Created on 11/26/2019
 */
public class StringUtils {
    public static String captureName(String name) {
        char[] cs=name.toCharArray();
        cs[0]-=32;
        return String.valueOf(cs);

    }
}
