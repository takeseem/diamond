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

/**
 * 全局计数器
 * 
 * @author boyan
 * @date 2010-5-31
 */
public class GlobalCounter {
    private static GlobalCounter instance = new GlobalCounter();

    private long count = 0;


    public static GlobalCounter getCounter() {
        return instance;
    }


    public synchronized long decrementAndGet() {
        if (count == Long.MIN_VALUE) {
            count = 0;
        }
        else
            count--;
        return count;
    }


    public synchronized long get() {
        return this.count;
    }


    public synchronized void set(long value) {
        this.count = value;
    }

}
