/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.client.impl;

import com.taobao.diamond.client.DiamondSubscriber;


/**
 * Diamond客户端工厂类，可以产生一个单例的DiamondSubscriber，供所有的DiamondManager共用 不同的集群对应不同的单例
 * 
 * @author aoqiong
 * 
 */
public class DiamondClientFactory {

    private static DiamondSubscriber diamondSubscriber = new DefaultDiamondSubscriber(new DefaultSubscriberListener());


    public static DiamondSubscriber getSingletonDiamondSubscriber() {
        return diamondSubscriber;
    }

}
