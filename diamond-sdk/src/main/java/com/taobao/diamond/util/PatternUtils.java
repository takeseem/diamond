/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.util;

/**
 * 模糊查询时合成 sql的工具类
 * 
 * @filename PatternUtils.java
 * @author libinbin.pt
 * @datetime 2010-7-23 上午11:42:58
 */
public class PatternUtils {

    /**
     * 检查参数字符串中是否包含符号 '*'
     * 
     * @param patternStr
     * @return 包含返回true, 否则返回false
     */
    public static boolean hasCharPattern(String patternStr) {
        if (patternStr == null)
            return false;
        String pattern = patternStr;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '*')
                return true;
        }
        return false;
    }


    /**
     * 替换掉所有的符号'*'为'%'
     * 
     * @param sourcePattern
     * @return 返回替换后的字符串
     */
    public static String generatePattern(String sourcePattern) {
        if (sourcePattern == null)
            return "";
        StringBuilder sb = new StringBuilder();
        String pattern = sourcePattern;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '*')
                sb.append('%');
            else
                sb.append(c);
        }
        return sb.toString();
    }

}
