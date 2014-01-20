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

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.taobao.diamond.common.Constants;
import com.taobao.diamond.mockserver.MockServer;


/**
 * Diamond客户端的配置信息
 * 
 * @author aoqiong
 * 
 */
public class DiamondConfigure {

    private volatile int pollingIntervalTime = Constants.POLLING_INTERVAL_TIME;// 异步查询的间隔时间
    private volatile int onceTimeout = Constants.ONCE_TIMEOUT;// 获取对于一个DiamondServer所对应的查询一个DataID对应的配置信息的Timeout时间
    private volatile int receiveWaitTime = Constants.RECV_WAIT_TIMEOUT;// 同步查询一个DataID所花费的时间

    private volatile List<String> domainNameList = new LinkedList<String>();

    private volatile boolean useFlowControl = true;

    private boolean localFirst = false;

    // 以下参数不支持运行后动态更新
    private int maxHostConnections = 1;
    private boolean connectionStaleCheckingEnabled = true;
    private int maxTotalConnections = 20;
    private int connectionTimeout = Constants.CONN_TIMEOUT;
    private int port = Constants.DEFAULT_PORT;
    private int scheduledThreadPoolSize = 1;
    // 获取数据时的重试次数
    private int retrieveDataRetryTimes = Integer.MAX_VALUE / 10;

    private String configServerAddress = null;
    private int configServerPort = Constants.DEFAULT_PORT;

    // 本地数据保存路径
    private String filePath;


    public DiamondConfigure() {
        filePath = System.getProperty("user.home") + "/diamond";
        File dir = new File(filePath);
        dir.mkdirs();

        if (!dir.exists()) {
            throw new RuntimeException("创建diamond目录失败：" + filePath);
        }
    }


    /**
     * 获取和同一个DiamondServer的最大连接数
     * 
     * @return
     */
    public int getMaxHostConnections() {
        return maxHostConnections;
    }


    /**
     * 设置和同一个DiamondServer的最大连接数<br>
     * 不支持运行时动态更新
     * 
     * @param maxHostConnections
     */
    public void setMaxHostConnections(int maxHostConnections) {
        this.maxHostConnections = maxHostConnections;
    }


    /**
     * 是否允许对陈旧的连接情况进行检测。<br>
     * 如果不检测，性能上会有所提升，但是，会有使用不可用连接的风险导致的IO Exception
     * 
     * @return
     */
    public boolean isConnectionStaleCheckingEnabled() {
        return connectionStaleCheckingEnabled;
    }


    /**
     * 设置是否允许对陈旧的连接情况进行检测。<br>
     * 不支持运行时动态更新
     * 
     * @param connectionStaleCheckingEnabled
     */
    public void setConnectionStaleCheckingEnabled(boolean connectionStaleCheckingEnabled) {
        this.connectionStaleCheckingEnabled = connectionStaleCheckingEnabled;
    }


    /**
     * 获取允许的最大的连接数量。
     * 
     * @return
     */
    public int getMaxTotalConnections() {
        return maxTotalConnections;
    }


    /**
     * 设置允许的最大的连接数量。<br>
     * 不支持运行时动态更新
     * 
     * @param maxTotalConnections
     */
    public void setMaxTotalConnections(int maxTotalConnections) {
        this.maxTotalConnections = maxTotalConnections;
    }


    /**
     * 获取轮询的间隔时间。单位：秒<br>
     * 此间隔时间代表轮询查找一次配置信息的间隔时间，对于容灾相关，请设置短一些；<br>
     * 对于其他不可变的配置信息，请设置长一些
     * 
     * @return
     */
    public int getPollingIntervalTime() {
        return pollingIntervalTime;
    }


    /**
     * 设置轮询的间隔时间。单位：秒
     * 
     * @param pollingIntervalTime
     */
    public void setPollingIntervalTime(int pollingIntervalTime) {
        if (pollingIntervalTime < Constants.POLLING_INTERVAL_TIME && !MockServer.isTestMode()) {
            return;
        }
        this.pollingIntervalTime = pollingIntervalTime;
    }


    /**
     * 获取当前支持的所有的DiamondServer域名列表
     * 
     * @return
     */
    public List<String> getDomainNameList() {
        return domainNameList;
    }


    /**
     * 设置当前支持的所有的DiamondServer域名列表，当设置了域名列表后，缺省的域名列表将失效
     * 
     * @param domainNameList
     */
    public void setDomainNameList(List<String> domainNameList) {
        if (null == domainNameList) {
            throw new NullPointerException();
        }
        this.domainNameList = new LinkedList<String>(domainNameList);
    }


    /**
     * 添加一个DiamondServer域名，当设置了域名列表后，缺省的域名列表将失效
     * 
     * @param domainName
     */
    public void addDomainName(String domainName) {
        if (null == domainName) {
            throw new NullPointerException();
        }
        this.domainNameList.add(domainName);
    }


    /**
     * 添加多个DiamondServer域名，当设置了域名列表后，缺省的域名列表将失效
     * 
     * @param domainNameList
     */
    public void addDomainNames(Collection<String> domainNameList) {
        if (null == domainNameList) {
            throw new NullPointerException();
        }
        this.domainNameList.addAll(domainNameList);
    }


    /**
     * 获取DiamondServer的端口号
     * 
     * @return
     */
    public int getPort() {
        return port;
    }


    /**
     * 设置DiamondServer的端口号<br>
     * 不支持运行时动态更新
     * 
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }


    /**
     * 获取探测本地文件的路径
     * 
     * @return
     */
    public String getFilePath() {
        return filePath;
    }


    /**
     * 设置探测本地文件的路径<br>
     * 不支持运行时动态更新
     * 
     * @param filePath
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    /**
     * 获取对于一个DiamondServer所对应的查询一个DataID对应的配置信息的Timeout时间<br>
     * 即一次HTTP请求的超时时间<br>
     * 单位：毫秒<br>
     * 
     * @return
     */
    public int getOnceTimeout() {
        return onceTimeout;
    }


    /**
     * 设置对于一个DiamondServer所对应的查询一个DataID对应的配置信息的Timeout时间<br>
     * 单位：毫秒<br>
     * 配置信息越大，请将此值设置得越大
     * 
     * @return
     */
    public void setOnceTimeout(int onceTimeout) {
        this.onceTimeout = onceTimeout;
    }


    /**
     * 获取和DiamondServer的连接建立超时时间。单位：毫秒
     * 
     * @return
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }


    /**
     * 设置和DiamondServer的连接建立超时时间。单位：毫秒<br>
     * 不支持运行时动态更新
     * 
     * @param connectionTimeout
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }


    /**
     * 同步查询一个DataID的最长等待时间<br>
     * 实际最长等待时间小于receiveWaitTime + min(connectionTimeout, onceTimeout)
     * 
     * @return
     */
    public int getReceiveWaitTime() {
        return receiveWaitTime;
    }


    /**
     * 设置一个DataID的最长等待时间<br>
     * 实际最长等待时间小于receiveWaitTime + min(connectionTimeout, onceTimeout)
     * 建议此值设置为OnceTimeout * （DomainName个数 + 1）
     * 
     * @param receiveWaitTime
     */
    public void setReceiveWaitTime(int receiveWaitTime) {
        this.receiveWaitTime = receiveWaitTime;
    }


    /**
     * 获取线程池的线程数量
     * 
     * @return
     */
    public int getScheduledThreadPoolSize() {
        return scheduledThreadPoolSize;
    }


    /**
     * 设置线程池的线程数量，缺省为1
     * 
     * @param scheduledThreadPoolSize
     */
    public void setScheduledThreadPoolSize(int scheduledThreadPoolSize) {
        this.scheduledThreadPoolSize = scheduledThreadPoolSize;
    }


    /**
     * 是否使用同步接口流控
     * 
     * @return
     */
    public boolean isUseFlowControl() {
        return useFlowControl;
    }


    /**
     * 设置是否使用同步接口流控
     * 
     * @param useFlowControl
     */
    public void setUseFlowControl(boolean useFlowControl) {
        this.useFlowControl = useFlowControl;
    }


    public String getConfigServerAddress() {
        return configServerAddress;
    }


    public void setConfigServerAddress(String configServerAddress) {
        this.configServerAddress = configServerAddress;
    }


    public int getConfigServerPort() {
        return configServerPort;
    }


    public void setConfigServerPort(int configServerPort) {
        this.configServerPort = configServerPort;
    }


    public int getRetrieveDataRetryTimes() {
        return retrieveDataRetryTimes;
    }


    public void setRetrieveDataRetryTimes(int retrieveDataRetryTimes) {
        this.retrieveDataRetryTimes = retrieveDataRetryTimes;
    }


    public boolean isLocalFirst() {
        return localFirst;
    }


    public void setLocalFirst(boolean localFirst) {
        this.localFirst = localFirst;
    }

}
