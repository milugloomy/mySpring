package com.baqi.util;

public class Util {

    public static String lowerFirst(String str) {
        char[] cs = str.toCharArray();
        cs[0] += 32;
        return String.valueOf(cs);
    }

}
