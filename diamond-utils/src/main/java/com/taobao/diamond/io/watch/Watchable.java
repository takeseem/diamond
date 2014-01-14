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
 * 标记接口，实现此接口的类可以被注册到WatchService
 * 
 * @author boyan
 * 
 */
public interface Watchable {
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events);
}
