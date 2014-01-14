/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.io.watch;

/**
 * 
 * @author boyan
 * @date 2010-5-4
 * @param <T>
 */
public class WatchEvent<T> {
    public static interface Kind<T> {
        public String name();


        public Class<T> type();
    }

    private Kind<T> kind;

    private int count;

    private T context;


    public WatchEvent(Kind<T> kind, int count, T context) {
        super();
        this.kind = kind;
        this.count = count;
        this.context = context;
    }


    public Kind<T> kind() {
        return kind;

    }


    public int count() {
        return count;
    }


    public T context() {
        return context;
    }
}
