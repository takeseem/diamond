/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.client;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.taobao.diamond.configinfo.CacheData;


/**
 * DiamondSubscriber用于订阅持久的文本配置信息。<br>
 * 
 * @author aoqiong
 * 
 */
public interface DiamondSubscriber extends DiamondClientSub {
    /**
     * 设置异步订阅的Listener，可以动态替换
     * 
     * @param subscriberListener
     */
    public void setSubscriberListener(SubscriberListener subscriberListener);


    /**
     * 获取异步订阅的Listener
     * 
     * @return
     */
    public SubscriberListener getSubscriberListener();


    /**
     * 获取group组DataID为dataId的ConfigureInfomation，必须在start()方法后调用,,此方法优先从${user.
     * home}/diamond/data下获取配置文件，如果没有，则从diamond server获取配置信息
     * 
     * @param dataId
     * @param group
     * @param timeout
     * @return
     */
    public String getConfigureInfomation(String dataId, String group, long timeout);


    /**
     * 获取缺省组的DataID为dataId的ConfigureInfomation，必须在start()方法后调用,此方法优先从${user.home
     * }/diamond/data下获取配置文件，如果没有，则从diamond server获取配置信息
     * 
     * @param dataId
     * @param timeout
     * @return
     */
    public String getConfigureInfomation(String dataId, long timeout);


    /**
     * 获取一份可用的配置信息，按照<strong>本地文件->diamond服务器->本地上一次保存的snapshot</strong>
     * 的优先顺序获取一份有效的配置信息，如果所有途径都无法获取一份有效配置信息 ， 则返回null
     * 
     * @param dataId
     * @param group
     * @param timeout
     * @return
     */
    public String getAvailableConfigureInfomation(String dataId, String group, long timeout);


    /**
     * 添加一个DataID，如果原来有此DataID和Group，将替换它们
     * 
     * @param dataId
     * @param group
     *            组名，可为null，代表使用缺省的组名
     */
    public void addDataId(String dataId, String group);


    /**
     * 添加一个DataID，使用缺省的组名。如果原来有此DataID和Group，将替换它们
     * 
     * @param dataId
     */
    public void addDataId(String dataId);


    /**
     * 目前是否支持对DataID对应的ConfigInfo
     * 
     * @param dataId
     * @return
     */
    public boolean containDataId(String dataId);


    /**
     * 
     * @param dataId
     * @param group
     * @return
     */
    public boolean containDataId(String dataId, String group);


    /**
     * 
     * @param dataId
     */
    public void removeDataId(String dataId);


    /**
     * 
     * @param dataId
     * @param group
     */
    public void removeDataId(String dataId, String group);


    /**
     * 清空所有的DataID
     */
    public void clearAllDataIds();


    /**
     * 获取支持的所有的DataID
     * 
     * @return
     */
    public Set<String> getDataIds();


    /**
     * 获取客户端cache
     * 
     * @return
     */
    public ConcurrentHashMap<String, ConcurrentHashMap<String, CacheData>> getCache();


    /**
     * 获取一份可用的配置信息，按照本地snapshot -> 本地文件 -> server的顺序
     * 
     * @param dataId
     * @param group
     * @param timeout
     * @return
     */
    public String getAvailableConfigureInfomationFromSnapshot(String dataId, String group, long timeout);

}
