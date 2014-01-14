/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.manager;

import java.util.List;
import java.util.Properties;

import com.taobao.diamond.client.DiamondConfigure;


/**
 * DiamondManager用于订阅一个且仅有一个DataID对应的配置信息
 * 
 * @author aoqiong
 * 
 */
public interface DiamondManager {

    /**
     * 设置ManagerListener，每当收到一个DataID对应的配置信息，则客户设置的ManagerListener会接收到这个配置信息
     * 
     * @param managerListener
     */
    public void setManagerListener(ManagerListener managerListener);


    /**
     * 设置DataID对应的多个ManagerListener，每当收到一个DataID对应的配置信息，
     * 则客户设置的多个ManagerListener会接收到这个配置信息
     * 
     * @param managerListenerList
     */
    public void setManagerListeners(List<ManagerListener> managerListenerList);


    /**
     * 返回该DiamondManager设置的listener列表
     * 
     * @return
     */
    public List<ManagerListener> getManagerListeners();


    /**
     * 同步获取配置信息,,此方法优先从${user.home
     * }/diamond/data/config-data/${group}/${dataId}下获取配置文件，如果没有，则从diamond
     * server获取配置信息
     * 
     * @param timeout
     *            从网络获取配置信息的超时，单位毫秒
     * @return
     */
    public String getConfigureInfomation(long timeout);


    /**
     * 同步获取一份有效的配置信息，按照<strong>本地文件->diamond服务器->上一次正确配置的snapshot</strong>
     * 的优先顺序获取， 如果这些途径都无效，则返回null
     * 
     * @param timeout
     *            从网络获取配置信息的超时，单位毫秒
     * @return
     */
    public String getAvailableConfigureInfomation(long timeout);


    /**
     * 同步获取一份有效的配置信息，按照<strong>上一次正确配置的snapshot->本地文件->diamond服务器</strong>
     * 的优先顺序获取， 如果这些途径都无效，则返回null
     * 
     * @param timeout
     *            从网络获取配置信息的超时，单位毫秒
     * @return
     */

    public String getAvailableConfigureInfomationFromSnapshot(long timeout);


    /**
     * 同步获取Properties格式的配置信息
     * 
     * @param timeout
     *            单位：毫秒
     * @return
     */
    public Properties getPropertiesConfigureInfomation(long timeout);


    /**
     * 同步获取Properties格式的配置信息，本地snapshot优先
     * 
     * @param timeout
     * @return
     */
    public Properties getAvailablePropertiesConfigureInfomationFromSnapshot(long timeout);


    /**
     * 同步获取一份有效的Properties配置信息，按照<strong>本地文件->diamond服务器->上一次正确配置的snapshot</
     * strong> 的优先顺序获取， 如果这些途径都无效，则返回null
     * 
     * @param timeout
     *            单位：毫秒
     * @return
     */
    public Properties getAvailablePropertiesConfigureInfomation(long timeout);


    /**
     * 设置DiamondConfigure，一个JVM中所有的DiamondManager对应这一个DiamondConfigure
     * 
     * @param diamondConfigure
     */
    public void setDiamondConfigure(DiamondConfigure diamondConfigure);


    /**
     * 获取DiamondConfigure，一个JVM中所有的DiamondManager对应这一个DiamondConfigure
     * 
     * @param diamondConfigure
     */
    public DiamondConfigure getDiamondConfigure();


    /**
     * 关闭这个DiamondManager
     */
    public void close();

}
