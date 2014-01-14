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

import static com.taobao.diamond.common.Constants.LINE_SEPARATOR;
import static com.taobao.diamond.common.Constants.WORD_SEPARATOR;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.diamond.client.DiamondConfigure;
import com.taobao.diamond.client.DiamondSubscriber;
import com.taobao.diamond.client.SubscriberListener;
import com.taobao.diamond.client.processor.LocalConfigInfoProcessor;
import com.taobao.diamond.client.processor.ServerAddressProcessor;
import com.taobao.diamond.client.processor.SnapshotConfigInfoProcessor;
import com.taobao.diamond.common.Constants;
import com.taobao.diamond.configinfo.CacheData;
import com.taobao.diamond.configinfo.ConfigureInfomation;
import com.taobao.diamond.md5.MD5;
import com.taobao.diamond.mockserver.MockServer;
import com.taobao.diamond.utils.LoggerInit;
import com.taobao.diamond.utils.SimpleCache;


/**
 * 缺省的DiamondSubscriber
 * 
 * @author aoqiong
 * 
 */
class DefaultDiamondSubscriber implements DiamondSubscriber {
    // 本地文件监视目录
    private static final String DATA_DIR = "data";
    // 上一次正确配置的镜像目录
    private static final String SNAPSHOT_DIR = "snapshot";

    private static final Log log = LogFactory.getLog(DefaultDiamondSubscriber.class);

    private static final int SC_OK = 200;

    private static final int SC_NOT_MODIFIED = 304;

    private static final int SC_NOT_FOUND = 404;

    private static final int SC_SERVICE_UNAVAILABLE = 503;

    static {
        try {
            LoggerInit.initLogFromBizLog();
        }
        catch (Throwable _) {
        }
    }
    private final Log dataLog = LogFactory.getLog(LoggerInit.LOG_NAME_CONFIG_DATA);

    private final ConcurrentHashMap<String/* DataID */, ConcurrentHashMap<String/* Group */, CacheData>> cache =
            new ConcurrentHashMap<String, ConcurrentHashMap<String, CacheData>>();

    private volatile SubscriberListener subscriberListener = null;
    private volatile DiamondConfigure diamondConfigure;

    private ScheduledExecutorService scheduledExecutor = null;

    private final LocalConfigInfoProcessor localConfigInfoProcessor = new LocalConfigInfoProcessor();

    private SnapshotConfigInfoProcessor snapshotConfigInfoProcessor;

    private final SimpleCache<String> contentCache = new SimpleCache<String>();

    private ServerAddressProcessor serverAddressProcessor = null;

    private final AtomicInteger domainNamePos = new AtomicInteger(0);

    private volatile boolean isRun = false;

    private HttpClient httpClient = null;

    private volatile boolean bFirstCheck = true;


    public DefaultDiamondSubscriber(SubscriberListener subscriberListener) {
        this.subscriberListener = subscriberListener;
        this.diamondConfigure = new DiamondConfigure();
    }


    /**
     * 启动DiamondSubscriber：<br>
     * 1.阻塞主动获取所有的DataId配置信息<br>
     * 2.启动定时线程定时获取所有的DataId配置信息<br>
     */
    public synchronized void start() {
        if (isRun) {
            return;
        }

        if (null == scheduledExecutor || scheduledExecutor.isTerminated()) {
            scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        }

        localConfigInfoProcessor.start(this.diamondConfigure.getFilePath() + "/" + DATA_DIR);
        serverAddressProcessor = new ServerAddressProcessor(this.diamondConfigure, this.scheduledExecutor);
        serverAddressProcessor.start();

        this.snapshotConfigInfoProcessor =
                new SnapshotConfigInfoProcessor(this.diamondConfigure.getFilePath() + "/" + SNAPSHOT_DIR);
        // 设置domainNamePos值
        randomDomainNamePos();
        initHttpClient();

        // 初始化完毕
        isRun = true;

        if (log.isInfoEnabled()) {
            log.info("当前使用的域名有：" + this.diamondConfigure.getDomainNameList());
        }

        if (MockServer.isTestMode()) {
            bFirstCheck = false;
        }
        else {
            // 设置轮询间隔时间
            this.diamondConfigure.setPollingIntervalTime(Constants.POLLING_INTERVAL_TIME);
        }
        // 轮询
        rotateCheckConfigInfo();

        addShutdownHook();
    }


    private void randomDomainNamePos() {
        // 随机化起始服务器地址
        Random rand = new Random();
        List<String> domainList = this.diamondConfigure.getDomainNameList();
        if (!domainList.isEmpty()) {
            this.domainNamePos.set(rand.nextInt(domainList.size()));
        }
    }


    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                // 关闭单例订阅者
                close();
            }

        });
    }


    protected void initHttpClient() {
        if (MockServer.isTestMode()) {
            return;
        }
        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setHost(diamondConfigure.getDomainNameList().get(this.domainNamePos.get()),
            diamondConfigure.getPort());

        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.closeIdleConnections(diamondConfigure.getPollingIntervalTime() * 4000);

        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setStaleCheckingEnabled(diamondConfigure.isConnectionStaleCheckingEnabled());
        params.setMaxConnectionsPerHost(hostConfiguration, diamondConfigure.getMaxHostConnections());
        params.setMaxTotalConnections(diamondConfigure.getMaxTotalConnections());
        params.setConnectionTimeout(diamondConfigure.getConnectionTimeout());
        // 设置读超时为1分钟,
        // boyan@taobao.com
        params.setSoTimeout(60 * 1000);

        connectionManager.setParams(params);
        httpClient = new HttpClient(connectionManager);
        httpClient.setHostConfiguration(hostConfiguration);
    }


    /**
     * 仅供测试，切勿调用
     * 
     * @param pos
     */
    void setDomainNamesPos(int pos) {
        this.domainNamePos.set(pos);
    }


    /**
     * 循环探测配置信息是否变化，如果变化，则再次向DiamondServer请求获取对应的配置信息
     */
    private void rotateCheckConfigInfo() {
        scheduledExecutor.schedule(new Runnable() {
            public void run() {
                if (!isRun) {
                    log.warn("DiamondSubscriber不在运行状态中，退出查询循环");
                    return;
                }
                try {
                    checkLocalConfigInfo();
                    checkDiamondServerConfigInfo();
                    checkSnapshot();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    log.error("循环探测发生异常", e);
                }
                finally {
                    rotateCheckConfigInfo();
                }
            }

        }, bFirstCheck ? 60 : diamondConfigure.getPollingIntervalTime(), TimeUnit.SECONDS);
        bFirstCheck = false;
    }


    /**
     * 向DiamondServer请求dataId对应的配置信息，并将结果抛给客户的监听器
     * 
     * @param dataId
     */
    private void receiveConfigInfo(final CacheData cacheData) {
        scheduledExecutor.execute(new Runnable() {
            public void run() {
                if (!isRun) {
                    log.warn("DiamondSubscriber不在运行状态中，退出查询循环");
                    return;
                }

                try {
                    String configInfo =
                            getConfigureInfomation(cacheData.getDataId(), cacheData.getGroup(),
                                diamondConfigure.getReceiveWaitTime(), true);
                    if (null == configInfo) {
                        return;
                    }

                    if (null == subscriberListener) {
                        log.warn("null == subscriberListener");
                        return;
                    }

                    popConfigInfo(cacheData, configInfo);
                }
                catch (Exception e) {
                    log.error("向Diamond服务器索要配置信息的过程抛异常", e);
                }
            }
        });
    }


    private void checkSnapshot() {
        for (Entry<String, ConcurrentHashMap<String, CacheData>> cacheDatasEntry : cache.entrySet()) {
            ConcurrentHashMap<String, CacheData> cacheDatas = cacheDatasEntry.getValue();
            if (null == cacheDatas) {
                continue;
            }
            for (Entry<String, CacheData> cacheDataEntry : cacheDatas.entrySet()) {
                final CacheData cacheData = cacheDataEntry.getValue();
                // 没有获取本地配置，也没有从diamond server获取配置成功,则加载上一次的snapshot
                if (!cacheData.isUseLocalConfigInfo() && cacheData.getFetchCount() == 0) {
                    String configInfo = getSnapshotConfiginfomation(cacheData.getDataId(), cacheData.getGroup());
                    if (configInfo != null) {
                        popConfigInfo(cacheData, configInfo);
                    }
                }
            }
        }
    }


    private void checkDiamondServerConfigInfo() {
        Set<String> updateDataIdGroupPairs = checkUpdateDataIds(diamondConfigure.getReceiveWaitTime());
        if (null == updateDataIdGroupPairs || updateDataIdGroupPairs.size() == 0) {
            log.debug("没有被修改的DataID");
            return;
        }
        // 对于每个发生变化的DataID，都请求一次对应的配置信息
        for (String freshDataIdGroupPair : updateDataIdGroupPairs) {
            int middleIndex = freshDataIdGroupPair.indexOf(WORD_SEPARATOR);
            if (middleIndex == -1)
                continue;
            String freshDataId = freshDataIdGroupPair.substring(0, middleIndex);
            String freshGroup = freshDataIdGroupPair.substring(middleIndex + 1);

            ConcurrentHashMap<String, CacheData> cacheDatas = cache.get(freshDataId);
            if (null == cacheDatas) {
                continue;
            }
            CacheData cacheData = cacheDatas.get(freshGroup);
            if (null == cacheData) {
                continue;
            }
            receiveConfigInfo(cacheData);
        }
    }


    private void checkLocalConfigInfo() {
        for (Entry<String/* dataId */, ConcurrentHashMap<String/* group */, CacheData>> cacheDatasEntry : cache
            .entrySet()) {
            ConcurrentHashMap<String, CacheData> cacheDatas = cacheDatasEntry.getValue();
            if (null == cacheDatas) {
                continue;
            }
            for (Entry<String, CacheData> cacheDataEntry : cacheDatas.entrySet()) {
                final CacheData cacheData = cacheDataEntry.getValue();
                try {
                    String configInfo = getLocalConfigureInfomation(cacheData);
                    if (null != configInfo) {
                        if (log.isInfoEnabled()) {
                            log.info("本地配置信息被读取, dataId:" + cacheData.getDataId() + ", group:" + cacheData.getGroup());
                        }
                        popConfigInfo(cacheData, configInfo);
                        continue;
                    }
                    if (cacheData.isUseLocalConfigInfo()) {
                        continue;
                    }
                }
                catch (Exception e) {
                    log.error("向本地索要配置信息的过程抛异常", e);
                }
            }
        }
    }


    /**
     * 将订阅信息抛给客户的监听器
     * 
     */
    void popConfigInfo(final CacheData cacheData, final String configInfo) {
        final ConfigureInfomation configureInfomation = new ConfigureInfomation();
        configureInfomation.setConfigureInfomation(configInfo);
        final String dataId = cacheData.getDataId();
        final String group = cacheData.getGroup();
        configureInfomation.setDataId(dataId);
        configureInfomation.setGroup(group);
        cacheData.incrementFetchCountAndGet();
        if (null != this.subscriberListener.getExecutor()) {
            this.subscriberListener.getExecutor().execute(new Runnable() {
                public void run() {
                    try {
                        subscriberListener.receiveConfigInfo(configureInfomation);
                        saveSnapshot(dataId, group, configInfo);
                    }
                    catch (Throwable t) {
                        log.error("配置信息监听器中有异常，group为：" + group + ", dataId为：" + dataId, t);
                    }
                }
            });
        }
        else {
            try {
                subscriberListener.receiveConfigInfo(configureInfomation);
                saveSnapshot(dataId, group, configInfo);
            }
            catch (Throwable t) {
                log.error("配置信息监听器中有异常，group为：" + group + ", dataId为：" + dataId, t);
            }
        }
    }


    public synchronized void close() {
        if (!isRun) {
            return;
        }
        log.warn("开始关闭DiamondSubscriber");

        localConfigInfoProcessor.stop();
        serverAddressProcessor.stop();
        isRun = false;
        scheduledExecutor.shutdown();
        cache.clear();

        log.warn("关闭DiamondSubscriber完成");
    }


    /**
     * 
     * @param waitTime
     *            本次查询已经耗费的时间(已经查询的多次HTTP耗费的时间)
     * @param timeout
     *            本次查询总的可耗费时间(可供多次HTTP查询使用)
     * @return 本次HTTP查询能够使用的时间
     */
    long getOnceTimeOut(long waitTime, long timeout) {
        long onceTimeOut = this.diamondConfigure.getOnceTimeout();
        long remainTime = timeout - waitTime;
        if (onceTimeOut > remainTime) {
            onceTimeOut = remainTime;
        }
        return onceTimeOut;
    }


    public String getLocalConfigureInfomation(CacheData cacheData) throws IOException {
        if (!isRun) {
            throw new RuntimeException("DiamondSubscriber不在运行状态中，无法获取本地ConfigureInfomation");
        }
        return localConfigInfoProcessor.getLocalConfigureInfomation(cacheData, false);
    }


    public String getConfigureInfomation(String dataId, long timeout) {
        return getConfigureInfomation(dataId, null, timeout);
    }


    public String getConfigureInfomation(String dataId, String group, long timeout) {
        // 同步接口流控
        // flowControl();
        if (null == group) {
            group = Constants.DEFAULT_GROUP;
        }
        CacheData cacheData = getCacheData(dataId, group);
        // 优先使用本地配置
        try {
            String localConfig = localConfigInfoProcessor.getLocalConfigureInfomation(cacheData, true);
            if (localConfig != null) {
                cacheData.incrementFetchCountAndGet();
                saveSnapshot(dataId, group, localConfig);
                return localConfig;
            }
        }
        catch (IOException e) {
            log.error("获取本地配置文件出错", e);
        }
        // 获取本地配置失败，从网络取
        String result = getConfigureInfomation(dataId, group, timeout, false);
        if (result != null) {
            saveSnapshot(dataId, group, result);
            cacheData.incrementFetchCountAndGet();
        }
        return result;
    }


    private void saveSnapshot(String dataId, String group, String config) {
        if (config != null) {
            try {
                this.snapshotConfigInfoProcessor.saveSnaptshot(dataId, group, config);
            }
            catch (IOException e) {
                log.error("保存snapshot出错,dataId=" + dataId + ",group=" + group, e);
            }
        }
    }


    public String getAvailableConfigureInfomation(String dataId, String group, long timeout) {
        // 尝试先从本地和网络获取配置信息
        try {
            String result = getConfigureInfomation(dataId, group, timeout);
            if (result != null && result.length() > 0) {
                return result;
            }
        }
        catch (Throwable t) {
            log.error(t.getMessage(), t);
        }

        // 测试模式不使用本地dump
        if (MockServer.isTestMode()) {
            return null;
        }
        return getSnapshotConfiginfomation(dataId, group);
    }


    public String getAvailableConfigureInfomationFromSnapshot(String dataId, String group, long timeout) {
        String result = getSnapshotConfiginfomation(dataId, group);
        if (!StringUtils.isBlank(result)) {
            return result;
        }
        return getConfigureInfomation(dataId, group, timeout);
    }


    private String getSnapshotConfiginfomation(String dataId, String group) {
        if (group == null) {
            group = Constants.DEFAULT_GROUP;
        }
        try {
            CacheData cacheData = getCacheData(dataId, group);
            String config = this.snapshotConfigInfoProcessor.getConfigInfomation(dataId, group);
            if (config != null && cacheData != null) {
                cacheData.incrementFetchCountAndGet();
            }
            return config;
        }
        catch (Exception e) {
            log.error("获取snapshot出错， dataId=" + dataId + ",group=" + group, e);
            return null;
        }
    }


    /**
     * 
     * @param dataId
     * @param group
     * @param timeout
     * @param skipContentCache
     *            是否使用本地的内容cache。主动get时会使用，有check触发的异步get不使用本地cache。
     * @return
     */
    String getConfigureInfomation(String dataId, String group, long timeout, boolean skipContentCache) {
        start();
        if (!isRun) {
            throw new RuntimeException("DiamondSubscriber不在运行状态中，无法获取ConfigureInfomation");
        }
        if (null == group) {
            group = Constants.DEFAULT_GROUP;
        }
        // =======================使用测试模式=======================
        if (MockServer.isTestMode()) {
            return MockServer.getConfigInfo(dataId, group);
        }
        // ==========================================================
        /**
         * 使用带有TTL的cache，
         */
        if (!skipContentCache) {
            String key = makeCacheKey(dataId, group);
            String content = contentCache.get(key);
            if (content != null) {
                return content;
            }
        }

        long waitTime = 0;

        String uri = getUriString(dataId, group);
        if (log.isInfoEnabled()) {
            log.info(uri);
        }

        CacheData cacheData = getCacheData(dataId, group);

        // 总的重试次数
        int retryTimes = this.getDiamondConfigure().getRetrieveDataRetryTimes();
        log.info("设定的获取配置数据的重试次数为：" + retryTimes);
        // 已经尝试过的次数
        int tryCount = 0;

        while (0 == timeout || timeout > waitTime) {
            // 尝试次数加1
            tryCount++;
            if (tryCount > retryTimes + 1) {
                log.warn("已经到达了设定的重试次数");
                break;
            }
            log.info("获取配置数据，第" + tryCount + "次尝试, waitTime:" + waitTime);

            // 设置超时时间
            long onceTimeOut = getOnceTimeOut(waitTime, timeout);
            waitTime += onceTimeOut;

            HttpMethod httpMethod = new GetMethod(uri);

            configureHttpMethod(skipContentCache, cacheData, onceTimeOut, httpMethod);

            try {
                int httpStatus = httpClient.executeMethod(httpMethod);

                switch (httpStatus) {

                case SC_OK: {
                    String result = getSuccess(dataId, group, cacheData, httpMethod);
                    return result;
                }

                case SC_NOT_MODIFIED: {
                    String result = getNotModified(dataId, cacheData, httpMethod);
                    return result;
                }

                case SC_NOT_FOUND: {
                    log.warn("没有找到DataID为:" + dataId + "对应的配置信息");
                    cacheData.setMd5(Constants.NULL);
                    this.snapshotConfigInfoProcessor.removeSnapshot(dataId, group);
                    return null;
                }

                case SC_SERVICE_UNAVAILABLE: {
                    rotateToNextDomain();
                }
                    break;

                default: {
                    log.warn("HTTP State: " + httpStatus + ":" + httpClient.getState());
                    rotateToNextDomain();
                }
                }
            }
            catch (HttpException e) {
                log.error("获取配置信息Http异常", e);
                rotateToNextDomain();
            }
            catch (IOException e) {

                log.error("获取配置信息IO异常", e);
                rotateToNextDomain();
            }
            catch (Exception e) {
                log.error("未知异常", e);
                rotateToNextDomain();
            }
            finally {
                httpMethod.releaseConnection();
            }
        }
        throw new RuntimeException("获取ConfigureInfomation超时, DataID" + dataId + ", Group为：" + group + ",超时时间为："
                + timeout);
    }


    private CacheData getCacheData(String dataId, String group) {
        CacheData cacheData = null;
        ConcurrentHashMap<String, CacheData> cacheDatas = this.cache.get(dataId);
        if (null != cacheDatas) {
            cacheData = cacheDatas.get(group);
        }
        if (null == cacheData) {
            cacheData = new CacheData(dataId, group);
            ConcurrentHashMap<String, CacheData> newCacheDatas = new ConcurrentHashMap<String, CacheData>();
            ConcurrentHashMap<String, CacheData> oldCacheDatas = this.cache.putIfAbsent(dataId, newCacheDatas);
            if (null == oldCacheDatas) {
                oldCacheDatas = newCacheDatas;
            }
            if (null != oldCacheDatas.putIfAbsent(group, cacheData)) {
                cacheData = oldCacheDatas.get(group);
            }
        }
        return cacheData;
    }


    /**
     * 回馈的结果为RP_NO_CHANGE，则整个流程为：<br>
     * 1.检查缓存中的MD5码与返回的MD5码是否一致，如果不一致，则删除缓存行。重新再次查询。<br>
     * 2.如果MD5码一致，则直接返回NULL<br>
     */
    private String getNotModified(String dataId, CacheData cacheData, HttpMethod httpMethod) {
        Header md5Header = httpMethod.getResponseHeader(Constants.CONTENT_MD5);
        if (null == md5Header) {
            throw new RuntimeException("RP_NO_CHANGE返回的结果中没有MD5码");
        }
        String md5 = md5Header.getValue();
        if (!cacheData.getMd5().equals(md5)) {
            String lastMd5 = cacheData.getMd5();
            cacheData.setMd5(Constants.NULL);
            cacheData.setLastModifiedHeader(Constants.NULL);
            throw new RuntimeException("MD5码校验对比出错,DataID为:[" + dataId + "]上次MD5为:[" + lastMd5 + "]本次MD5为:[" + md5
                    + "]");
        }

        cacheData.setMd5(md5);
        changeSpacingInterval(httpMethod);
        if (log.isInfoEnabled()) {
            log.info("DataId: " + dataId + ", 对应的configInfo没有变化");
        }
        return null;
    }


    /**
     * 回馈的结果为RP_OK，则整个流程为：<br>
     * 1.获取配置信息，如果配置信息为空或者抛出异常，则抛出运行时异常<br>
     * 2.检测配置信息是否符合回馈结果中的MD5码，不符合，则再次获取配置信息，并记录日志<br>
     * 3.符合，则存储LastModified信息和MD5码，调整查询的间隔时间，将获取的配置信息发送给客户的监听器<br>
     */
    private String getSuccess(String dataId, String group, CacheData cacheData, HttpMethod httpMethod) {
        String configInfo = Constants.NULL;
        configInfo = getContent(httpMethod);
        if (null == configInfo) {
            throw new RuntimeException("RP_OK获取了错误的配置信息");
        }

        Header md5Header = httpMethod.getResponseHeader(Constants.CONTENT_MD5);
        if (null == md5Header) {
            throw new RuntimeException("RP_OK返回的结果中没有MD5码, " + configInfo);
        }
        String md5 = md5Header.getValue();
        if (!checkContent(configInfo, md5)) {
            throw new RuntimeException("配置信息的MD5码校验出错,DataID为:[" + dataId + "]配置信息为:[" + configInfo + "]MD5为:[" + md5
                    + "]");
        }

        Header lastModifiedHeader = httpMethod.getResponseHeader(Constants.LAST_MODIFIED);
        if (null == lastModifiedHeader) {
            throw new RuntimeException("RP_OK返回的结果中没有lastModifiedHeader");
        }
        String lastModified = lastModifiedHeader.getValue();

        cacheData.setMd5(md5);
        cacheData.setLastModifiedHeader(lastModified);

        changeSpacingInterval(httpMethod);

        // 设置到本地cache
        String key = makeCacheKey(dataId, group);
        contentCache.put(key, configInfo);

        // 记录接收到的数据
        StringBuilder buf = new StringBuilder();
        buf.append("dataId=").append(dataId);
        buf.append(" ,group=").append(group);
        buf.append(" ,content=").append(configInfo);
        dataLog.info(buf.toString());

        return configInfo;
    }


    private void configureHttpMethod(boolean skipContentCache, CacheData cacheData, long onceTimeOut,
            HttpMethod httpMethod) {
        if (skipContentCache && null != cacheData) {
            if (null != cacheData.getLastModifiedHeader() && Constants.NULL != cacheData.getLastModifiedHeader()) {
                httpMethod.addRequestHeader(Constants.IF_MODIFIED_SINCE, cacheData.getLastModifiedHeader());
            }
            if (null != cacheData.getMd5() && Constants.NULL != cacheData.getMd5()) {
                httpMethod.addRequestHeader(Constants.CONTENT_MD5, cacheData.getMd5());
            }
        }

        httpMethod.addRequestHeader(Constants.ACCEPT_ENCODING, "gzip,deflate");

        // 设置HttpMethod的参数
        HttpMethodParams params = new HttpMethodParams();
        params.setSoTimeout((int) onceTimeOut);
        // ///////////////////////
        httpMethod.setParams(params);
        httpClient.getHostConfiguration().setHost(diamondConfigure.getDomainNameList().get(this.domainNamePos.get()),
            diamondConfigure.getPort());
    }


    private String makeCacheKey(String dataId, String group) {
        String key = dataId + "-" + group;
        return key;
    }


    /**
     * 从DiamondServer获取值变化了的DataID列表
     * 
     * @param timeout
     * @return
     */
    Set<String> checkUpdateDataIds(long timeout) {
        if (!isRun) {
            throw new RuntimeException("DiamondSubscriber不在运行状态中，无法获取修改过的DataID列表");
        }
        // =======================使用测试模式=======================
        if (MockServer.isTestMode()) {
            return testData();
        }
        // ==========================================================
        long waitTime = 0;

        // Set<String> localModifySet = getLocalUpdateDataIds();
        String probeUpdateString = getProbeUpdateString();
        if (StringUtils.isBlank(probeUpdateString)) {
            return null;
        }

        while (0 == timeout || timeout > waitTime) {
            // 设置超时时间
            long onceTimeOut = getOnceTimeOut(waitTime, timeout);
            waitTime += onceTimeOut;

            PostMethod postMethod = new PostMethod(Constants.HTTP_URI_FILE);

            postMethod.addParameter(Constants.PROBE_MODIFY_REQUEST, probeUpdateString);

            // 设置HttpMethod的参数
            HttpMethodParams params = new HttpMethodParams();
            params.setSoTimeout((int) onceTimeOut);
            // ///////////////////////
            postMethod.setParams(params);

            try {
                httpClient.getHostConfiguration()
                    .setHost(diamondConfigure.getDomainNameList().get(this.domainNamePos.get()),
                        this.diamondConfigure.getPort());

                int httpStatus = httpClient.executeMethod(postMethod);

                switch (httpStatus) {
                case SC_OK: {
                    Set<String> result = getUpdateDataIds(postMethod);
                    return result;
                }

                case SC_SERVICE_UNAVAILABLE: {
                    rotateToNextDomain();
                }
                    break;

                default: {
                    log.warn("获取修改过的DataID列表的请求回应的HTTP State: " + httpStatus);
                    rotateToNextDomain();
                }
                }
            }
            catch (HttpException e) {
                log.error("获取配置信息Http异常", e);
                rotateToNextDomain();
            }
            catch (IOException e) {
                log.error("获取配置信息IO异常", e);
                rotateToNextDomain();
            }
            catch (Exception e) {
                log.error("未知异常", e);
                rotateToNextDomain();
            }
            finally {
                postMethod.releaseConnection();
            }
        }
        throw new RuntimeException("获取修改过的DataID列表超时 "
                + diamondConfigure.getDomainNameList().get(this.domainNamePos.get()) + ", 超时时间为：" + timeout);
    }


    private Set<String> testData() {
        Set<String> dataIdList = new HashSet<String>();
        for (String dataId : this.cache.keySet()) {
            ConcurrentHashMap<String, CacheData> cacheDatas = this.cache.get(dataId);
            for (String group : cacheDatas.keySet()) {
                if (null != MockServer.getUpdateConfigInfo(dataId, group)) {
                    dataIdList.add(dataId + WORD_SEPARATOR + group);
                }
            }
        }
        return dataIdList;
    }


    /**
     * 获取探测更新的DataID的请求字符串
     * 
     * @param localModifySet
     * @return
     */
    private String getProbeUpdateString() {
        // 获取check的DataID:Group:MD5串
        StringBuilder probeModifyBuilder = new StringBuilder();
        for (Entry<String, ConcurrentHashMap<String, CacheData>> cacheDatasEntry : this.cache.entrySet()) {
            String dataId = cacheDatasEntry.getKey();
            ConcurrentHashMap<String, CacheData> cacheDatas = cacheDatasEntry.getValue();
            if (null == cacheDatas) {
                continue;
            }
            for (Entry<String, CacheData> cacheDataEntry : cacheDatas.entrySet()) {
                final CacheData data = cacheDataEntry.getValue();
                // 非使用本地配置，才去diamond server检查
                if (!data.isUseLocalConfigInfo()) {
                    probeModifyBuilder.append(dataId).append(WORD_SEPARATOR);

                    if (null != cacheDataEntry.getValue().getGroup()
                            && Constants.NULL != cacheDataEntry.getValue().getGroup()) {
                        probeModifyBuilder.append(cacheDataEntry.getValue().getGroup()).append(WORD_SEPARATOR);
                    }
                    else {
                        probeModifyBuilder.append(WORD_SEPARATOR);
                    }

                    if (null != cacheDataEntry.getValue().getMd5()
                            && Constants.NULL != cacheDataEntry.getValue().getMd5()) {
                        probeModifyBuilder.append(cacheDataEntry.getValue().getMd5()).append(LINE_SEPARATOR);
                    }
                    else {
                        probeModifyBuilder.append(LINE_SEPARATOR);
                    }
                }
            }
        }
        String probeModifyString = probeModifyBuilder.toString();
        return probeModifyString;
    }


    synchronized void rotateToNextDomain() {
        int domainNameCount = diamondConfigure.getDomainNameList().size();
        int index = domainNamePos.incrementAndGet();
        if (index < 0) {
            index = -index;
        }
        if (domainNameCount == 0) {
            log.error("diamond服务器地址列表长度为零, 请联系负责人排查");
            return;
        }
        domainNamePos.set(index % domainNameCount);
        if (diamondConfigure.getDomainNameList().size() > 0)
            log.warn("轮换DiamondServer域名到：" + diamondConfigure.getDomainNameList().get(domainNamePos.get()));
    }


    /**
     * 获取查询Uri的String
     * 
     * @param dataId
     * @param group
     * @return
     */
    String getUriString(String dataId, String group) {
        StringBuilder uriBuilder = new StringBuilder();
        uriBuilder.append(Constants.HTTP_URI_FILE);
        uriBuilder.append("?");
        uriBuilder.append(Constants.DATAID).append("=").append(dataId);
        if (null != group) {
            uriBuilder.append("&");
            uriBuilder.append(Constants.GROUP).append("=").append(group);
        }
        return uriBuilder.toString();
    }


    /**
     * 设置新的消息轮询间隔时间
     * 
     * @param httpMethod
     */
    void changeSpacingInterval(HttpMethod httpMethod) {
        Header[] spacingIntervalHeaders = httpMethod.getResponseHeaders(Constants.SPACING_INTERVAL);
        if (spacingIntervalHeaders.length >= 1) {
            try {
                diamondConfigure.setPollingIntervalTime(Integer.parseInt(spacingIntervalHeaders[0].getValue()));
            }
            catch (RuntimeException e) {
                log.error("设置下次间隔时间失败", e);
            }
        }
    }


    /**
     * 获取Response的配置信息
     * 
     * @param httpMethod
     * @return
     */
    String getContent(HttpMethod httpMethod) {
        StringBuilder contentBuilder = new StringBuilder();
        if (isZipContent(httpMethod)) {
            // 处理压缩过的配置信息的逻辑
            InputStream is = null;
            GZIPInputStream gzin = null;
            InputStreamReader isr = null;
            BufferedReader br = null;
            try {
                is = httpMethod.getResponseBodyAsStream();
                gzin = new GZIPInputStream(is);
                isr = new InputStreamReader(gzin, ((HttpMethodBase) httpMethod).getResponseCharSet()); // 设置读取流的编码格式，自定义编码
                br = new BufferedReader(isr);
                char[] buffer = new char[4096];
                int readlen = -1;
                while ((readlen = br.read(buffer, 0, 4096)) != -1) {
                    contentBuilder.append(buffer, 0, readlen);
                }
            }
            catch (Exception e) {
                log.error("解压缩失败", e);
            }
            finally {
                try {
                    br.close();
                }
                catch (Exception e1) {
                    // ignore
                }
                try {
                    isr.close();
                }
                catch (Exception e1) {
                    // ignore
                }
                try {
                    gzin.close();
                }
                catch (Exception e1) {
                    // ignore
                }
                try {
                    is.close();
                }
                catch (Exception e1) {
                    // ignore
                }
            }
        }
        else {
            // 处理没有被压缩过的配置信息的逻辑
            String content = null;
            try {
                content = httpMethod.getResponseBodyAsString();
            }
            catch (Exception e) {
                log.error("获取配置信息失败", e);
            }
            if (null == content) {
                return null;
            }
            contentBuilder.append(content);
        }
        return contentBuilder.toString();
    }


    Set<String> getUpdateDataIdsInBody(HttpMethod httpMethod) {
        Set<String> modifiedDataIdSet = new HashSet<String>();
        try {
            String modifiedDataIdsString = httpMethod.getResponseBodyAsString();
            return convertStringToSet(modifiedDataIdsString);
        }
        catch (Exception e) {

        }
        return modifiedDataIdSet;

    }


    Set<String> getUpdateDataIds(HttpMethod httpMethod) {
        return getUpdateDataIdsInBody(httpMethod);
    }


    private Set<String> convertStringToSet(String modifiedDataIdsString) {

        if (null == modifiedDataIdsString || "".equals(modifiedDataIdsString)) {
            return null;
        }

        Set<String> modifiedDataIdSet = new HashSet<String>();

        try {
            modifiedDataIdsString = URLDecoder.decode(modifiedDataIdsString, "UTF-8");
        }
        catch (Exception e) {
            log.error("解码modifiedDataIdsString出错", e);
        }

        if (log.isInfoEnabled() && modifiedDataIdsString != null) {
            if (modifiedDataIdsString.startsWith("OK")) {
                log.debug("探测的返回结果:" + modifiedDataIdsString);
            }
            else {
                log.info("探测到数据变化:" + modifiedDataIdsString);
            }
        }

        final String[] modifiedDataIdStrings = modifiedDataIdsString.split(LINE_SEPARATOR);
        for (String modifiedDataIdString : modifiedDataIdStrings) {
            if (!"".equals(modifiedDataIdString)) {
                modifiedDataIdSet.add(modifiedDataIdString);
            }
        }
        return modifiedDataIdSet;
    }


    /**
     * 检测配置信息内容与MD5码是否一致
     * 
     * @param configInfo
     * @param md5
     * @return
     */
    boolean checkContent(String configInfo, String md5) {
        String realMd5 = MD5.getInstance().getMD5String(configInfo);
        return realMd5 == null ? md5 == null : realMd5.equals(md5);
    }


    /**
     * 查看是否为压缩的内容
     * 
     * @param httpMethod
     * @return
     */
    boolean isZipContent(HttpMethod httpMethod) {
        if (null != httpMethod.getResponseHeader(Constants.CONTENT_ENCODING)) {
            String acceptEncoding = httpMethod.getResponseHeader(Constants.CONTENT_ENCODING).getValue();
            if (acceptEncoding.toLowerCase().indexOf("gzip") > -1) {
                return true;
            }
        }
        return false;
    }


    public void setSubscriberListener(SubscriberListener subscriberListener) {
        this.subscriberListener = subscriberListener;
    }


    public SubscriberListener getSubscriberListener() {
        return this.subscriberListener;
    }


    public void addDataId(String dataId, String group) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日   HH:mm:ss");
        log.info("diamond client start:" + formatter.format(new Date(System.currentTimeMillis())));
        if (null == group) {
            group = Constants.DEFAULT_GROUP;
        }

        ConcurrentHashMap<String, CacheData> cacheDatas = this.cache.get(dataId);
        if (null == cacheDatas) {
            ConcurrentHashMap<String, CacheData> newCacheDatas = new ConcurrentHashMap<String, CacheData>();
            ConcurrentHashMap<String, CacheData> oldCacheDatas = this.cache.putIfAbsent(dataId, newCacheDatas);
            if (null != oldCacheDatas) {
                cacheDatas = oldCacheDatas;
            }
            else {
                cacheDatas = newCacheDatas;
            }
        }
        CacheData cacheData = cacheDatas.get(group);
        if (null == cacheData) {
            cacheDatas.putIfAbsent(group, new CacheData(dataId, group));
            if (log.isInfoEnabled()) {
                log.info("添加了DataID[" + dataId + "]，其Group为" + group);
            }
            this.start();
        }
    }


    public void addDataId(String dataId) {
        addDataId(dataId, null);
    }


    public boolean containDataId(String dataId) {
        return containDataId(dataId, null);
    }


    public boolean containDataId(String dataId, String group) {
        if (null == group) {
            group = Constants.DEFAULT_GROUP;
        }
        ConcurrentHashMap<String, CacheData> cacheDatas = this.cache.get(dataId);
        if (null == cacheDatas) {
            return false;
        }
        return cacheDatas.containsKey(group);
    }


    public void clearAllDataIds() {
        this.cache.clear();
    }


    public Set<String> getDataIds() {
        return new HashSet<String>(this.cache.keySet());
    }


    public ConcurrentHashMap<String, ConcurrentHashMap<String, CacheData>> getCache() {
        return cache;
    }


    public void removeDataId(String dataId) {
        removeDataId(dataId, null);
    }


    public synchronized void removeDataId(String dataId, String group) {
        if (null == group) {
            group = Constants.DEFAULT_GROUP;
        }
        ConcurrentHashMap<String, CacheData> cacheDatas = this.cache.get(dataId);
        if (null == cacheDatas) {
            return;
        }
        cacheDatas.remove(group);

        log.warn("删除了DataID[" + dataId + "]中的Group: " + group);

        if (cacheDatas.size() == 0) {
            this.cache.remove(dataId);
            log.warn("删除了DataID[" + dataId + "]");
        }
    }


    public DiamondConfigure getDiamondConfigure() {
        return this.diamondConfigure;
    }


    public void setDiamondConfigure(DiamondConfigure diamondConfigure) {
        if (!isRun) {
            this.diamondConfigure = diamondConfigure;
        }
        else {
            // 运行之后，某些参数无法更新
            copyDiamondConfigure(diamondConfigure);
        }
    }


    private void copyDiamondConfigure(DiamondConfigure diamondConfigure) {
        // TODO 哪些值可以在运行时动态更新?
    }

}
