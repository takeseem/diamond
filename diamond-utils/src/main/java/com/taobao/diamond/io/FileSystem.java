/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.io;

import com.taobao.diamond.io.watch.WatchService;


/**
 * 文件系统类，提供基础操作
 * 
 * @author boyan
 * @date 2010-5-4
 */
public class FileSystem {
    private static final FileSystem instance = new FileSystem();

    private String interval = System.getProperty("diamon.watch.interval", "5000");


    public void setInterval(String interval) {
        this.interval = interval;
    }


    public static final FileSystem getDefault() {
        return instance;
    }


    /**
     * 生成一个新的WatchService
     * 
     * @return
     */
    public WatchService newWatchService() {
        return new WatchService(Long.valueOf(interval));
    }

}
