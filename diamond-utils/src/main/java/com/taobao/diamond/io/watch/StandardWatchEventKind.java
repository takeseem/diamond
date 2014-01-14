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

import com.taobao.diamond.io.Path;


/**
 * ±ê×¼WatchEvent
 * 
 * @author boyan
 * 
 */
public class StandardWatchEventKind {

    /**
     * Directory entry created
     */
    public static final WatchEvent.Kind<Path> ENTRY_CREATE = new WatchEvent.Kind<Path>() {
        public String name() {
            return "ENTRY_CREATE";
        }


        public Class<Path> type() {
            return Path.class;
        }

    };

    /**
     * Directory entry deleted
     */
    public static final WatchEvent.Kind<Path> ENTRY_DELETE = new WatchEvent.Kind<Path>() {
        public String name() {
            return "ENTRY_DELETE";
        }


        public Class<Path> type() {
            return Path.class;
        }

    };

    /**
     * Directory entry modified
     */
    public static final WatchEvent.Kind<Path> ENTRY_MODIFY = new WatchEvent.Kind<Path>() {
        public String name() {
            return "ENTRY_MODIFY";
        }


        public Class<Path> type() {
            return Path.class;
        }

    };

    /**
     * Directory entry overflow
     */
    public static final WatchEvent.Kind<Void> OVERFLOW = new WatchEvent.Kind<Void>() {
        public String name() {
            return "OVERFLOW";
        }


        public Class<Void> type() {
            return Void.class;
        }

    };
}
