/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.server.utils;

import java.sql.Timestamp;
import java.util.Date;


public class DiamondUtils {
    static final char[] INVALID_CHAR = { ';', '&', '%', '#', '$', '@', ',', '*', '^', '~', '(', ')', '/', '\\', '|',
                                        '+' };


    /**
     * 判断字符串是否有空格
     * 
     * @param str
     * @return
     */
    public static boolean hasInvalidChar(String str) {
        if (str == null || str.length() == 0)
            return true;
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            char ch = str.charAt(i);
            if (Character.isWhitespace(ch) || isInvalidChar(ch)) {
                return true;
            }
        }
        return false;
    }


    public static boolean isInvalidChar(char ch) {
        for (char c : INVALID_CHAR) {
            if (c == ch)
                return true;
        }
        return false;
    }


    public static Timestamp getCurrentTime() {
        Date date = new Date();
        return new Timestamp(date.getTime());
    }

}
