/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.client.processor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.diamond.client.DiamondConfigure;
import com.taobao.diamond.common.Constants;
import com.taobao.diamond.mockserver.MockServer;


public class ServerAddressProcessor {
    private static final Log log = LogFactory.getLog(ServerAddressProcessor.class);

    private static final int SC_OK = 200;

    private volatile boolean isRun = false;
    private volatile DiamondConfigure diamondConfigure = null;
    private HttpClient configHttpClient = null;
    private ScheduledExecutorService scheduledExecutor = null;

    private int asynAcquireIntervalInSec = 300;


    public ServerAddressProcessor(DiamondConfigure diamondConfigure, ScheduledExecutorService scheduledExecutor) {
        this.diamondConfigure = diamondConfigure;
        this.scheduledExecutor = scheduledExecutor;
    }


    // 用于测试
    void setAsynAcquireIntervalInSec(int asynAcquireIntervalInSec) {
        this.asynAcquireIntervalInSec = asynAcquireIntervalInSec;
    }


    public synchronized void start() {
        if (isRun) {
            return;
        }
        isRun = true;
        initHttpClient();
        if (this.diamondConfigure.isLocalFirst()) {
            acquireServerAddressFromLocal();
        }
        else {
            synAcquireServerAddress();
            asynAcquireServerAddress();
        }

    }


    public synchronized void stop() {
        if (!isRun) {
            return;
        }
        this.scheduledExecutor.shutdown();
        isRun = false;
    }


    private void initHttpClient() {
        HostConfiguration hostConfiguration = new HostConfiguration();

        SimpleHttpConnectionManager connectionManager = new SimpleHttpConnectionManager();
        connectionManager.closeIdleConnections(5000L);

        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setStaleCheckingEnabled(diamondConfigure.isConnectionStaleCheckingEnabled());
        params.setConnectionTimeout(diamondConfigure.getConnectionTimeout());
        connectionManager.setParams(params);

        configHttpClient = new HttpClient(connectionManager);
        configHttpClient.setHostConfiguration(hostConfiguration);
    }


    protected void acquireServerAddressFromLocal() {
        if (!isRun) {
            throw new RuntimeException("ServerAddressProcessor不在运行状态，无法同步获取服务器地址列表");
        }
        if (MockServer.isTestMode()) {
            diamondConfigure.addDomainName("测试模式，没有使用的真实服务器");
            return;
        }

        int acquireCount = 0;
        if (diamondConfigure.getDomainNameList().size() == 0) {
            reloadServerAddresses();
            if (diamondConfigure.getDomainNameList().size() == 0) {
                if (!acquireServerAddressOnce(acquireCount)) {
                    acquireCount++;
                    if (acquireServerAddressOnce(acquireCount)) {
                        // 存入本地文件
                        storeServerAddressesToLocal();
                        log.info("在同步获取服务器列表时，向日常ConfigServer服务器获取到了服务器列表");
                    }
                    else {
                        throw new RuntimeException("当前没有可用的服务器列表");
                    }
                }
                else {
                    log.info("在同步获取服务器列表时，向线上ConfigServer服务器获取到了服务器列表");
                    // 存入本地文件
                    storeServerAddressesToLocal();
                }
            }
            else {
                log.info("在同步获取服务器列表时，由于本地指定了服务器列表，不向ConfigServer服务器同步获取服务器列表");
            }
        }
    }


    protected void synAcquireServerAddress() {
        if (!isRun) {
            throw new RuntimeException("ServerAddressProcessor不在运行状态，无法同步获取服务器地址列表");
        }
        if (MockServer.isTestMode()) {
            diamondConfigure.addDomainName("测试模式，没有使用的真实服务器");
            return;
        }

        int acquireCount = 0;
        if (diamondConfigure.getDomainNameList().size() == 0) {
            if (!acquireServerAddressOnce(acquireCount)) {
                acquireCount++;
                if (acquireServerAddressOnce(acquireCount)) {
                    // 存入本地文件
                    storeServerAddressesToLocal();
                    log.info("在同步获取服务器列表时，向日常ConfigServer服务器获取到了服务器列表");
                }
                else {
                    log.info("从本地获取Diamond地址列表");
                    reloadServerAddresses();
                    if (diamondConfigure.getDomainNameList().size() == 0)
                        throw new RuntimeException("当前没有可用的服务器列表");
                }
            }
            else {
                log.info("在同步获取服务器列表时，向线上ConfigServer服务器获取到了服务器列表");
                // 存入本地文件
                storeServerAddressesToLocal();
            }
        }
    }


    protected void asynAcquireServerAddress() {
        if (MockServer.isTestMode()) {
            return;
        }
        this.scheduledExecutor.schedule(new Runnable() {
            public void run() {
                if (!isRun) {
                    log.warn("ServerAddressProcessor不在运行状态，无法异步获取服务器地址列表");
                    return;
                }
                int acquireCount = 0;
                if (!acquireServerAddressOnce(acquireCount)) {
                    acquireCount++;
                    if (acquireServerAddressOnce(acquireCount)) {
                        // 存入本地文件
                        storeServerAddressesToLocal();
                    }
                }
                else {
                    // 存入本地文件
                    storeServerAddressesToLocal();
                }

                asynAcquireServerAddress();
            }
        }, asynAcquireIntervalInSec, TimeUnit.SECONDS);
    }


    void storeServerAddressesToLocal() {
        List<String> domainNameList = new ArrayList<String>(diamondConfigure.getDomainNameList());
        PrintWriter printWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            File serverAddressFile =
                    new File(generateLocalFilePath(this.diamondConfigure.getFilePath(), "ServerAddress"));
            if (!serverAddressFile.exists()) {
                serverAddressFile.createNewFile();
            }
            printWriter = new PrintWriter(serverAddressFile);
            bufferedWriter = new BufferedWriter(printWriter);
            for (String serveraddress : domainNameList) {
                bufferedWriter.write(serveraddress);
                bufferedWriter.newLine();
            }
            bufferedWriter.flush();
        }
        catch (Exception e) {
            log.error("存储服务器地址到本地文件失败", e);
        }
        finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                }
                catch (IOException e) {
                    // ignore
                }
            }
            if (printWriter != null) {
                printWriter.close();
            }
        }
    }


    void reloadServerAddresses() {
        FileInputStream fis = null;
        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            File serverAddressFile =
                    new File(generateLocalFilePath(this.diamondConfigure.getFilePath(), "ServerAddress"));

            if (!serverAddressFile.exists()) {
                return;
            }
            fis = new FileInputStream(serverAddressFile);
            reader = new InputStreamReader(fis);
            bufferedReader = new BufferedReader(reader);
            String address = null;
            while ((address = bufferedReader.readLine()) != null) {
                address = address.trim();
                if (StringUtils.isNotBlank(address)) {
                    diamondConfigure.getDomainNameList().add(address);
                }
            }
            bufferedReader.close();
            reader.close();
            fis.close();
        }
        catch (Exception e) {
            log.error("从本地文件取服务器地址失败", e);
        }
        finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                }
                catch (Exception e) {

                }
            }
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (Exception e) {

                }
            }
            if (fis != null) {
                try {
                    fis.close();
                }
                catch (Exception e) {

                }
            }
        }
    }


    String generateLocalFilePath(String directory, String fileName) {
        String sign = "";
        if (null == directory) {
            directory = System.getProperty("user.home");
        }
        if (directory.endsWith("\\") || directory.endsWith("/")) {
            sign = "";
        }
        else {
            sign = "/";
        }
        return directory + sign + fileName;
    }


    /**
     * 获取diamond服务器地址列表
     * 
     * @param acquireCount
     *            根据0或1决定从日常或线上获取
     * @return
     */
    private boolean acquireServerAddressOnce(int acquireCount) {
        HostConfiguration hostConfiguration = configHttpClient.getHostConfiguration();
        String configServerAddress;
        int port;
        if (null != diamondConfigure.getConfigServerAddress()) {
            configServerAddress = diamondConfigure.getConfigServerAddress();
            port = diamondConfigure.getConfigServerPort();
        }
        else {
            if (acquireCount == 0) {
                configServerAddress = Constants.DEFAULT_DOMAINNAME;
                port = Constants.DEFAULT_PORT;
            }
            else {
                configServerAddress = Constants.DAILY_DOMAINNAME;
                port = Constants.DEFAULT_PORT;
            }
        }
        hostConfiguration.setHost(configServerAddress, port);

        String serverAddressUrl = Constants.CONFIG_HTTP_URI_FILE;

        HttpMethod httpMethod = new GetMethod(serverAddressUrl);
        // 设置HttpMethod的参数
        HttpMethodParams params = new HttpMethodParams();
        params.setSoTimeout(diamondConfigure.getOnceTimeout());
        // ///////////////////////
        httpMethod.setParams(params);

        try {
            if (SC_OK == configHttpClient.executeMethod(httpMethod)) {
                InputStreamReader reader = new InputStreamReader(httpMethod.getResponseBodyAsStream());
                BufferedReader bufferedReader = new BufferedReader(reader);
                String address = null;
                List<String> newDomainNameList = new LinkedList<String>();
                while ((address = bufferedReader.readLine()) != null) {
                    address = address.trim();
                    if (StringUtils.isNotBlank(address)) {
                        newDomainNameList.add(address);
                    }
                }
                if (newDomainNameList.size() > 0) {
                    log.debug("更新使用的服务器列表");
                    this.diamondConfigure.setDomainNameList(newDomainNameList);
                    return true;
                }
            }
            else {
                log.warn("没有可用的新服务器列表");
            }
        }
        catch (HttpException e) {
            log.error(getErrorMessage(configServerAddress) + ", " + e);
        }
        catch (IOException e) {
            log.error(getErrorMessage(configServerAddress) + ", " + e);
        }
        catch (Exception e) {
            log.error(getErrorMessage(configServerAddress) + ", " + e);
        }
        finally {
            httpMethod.releaseConnection();
        }
        return false;
    }


    public String getErrorMessage(String configServerAddress) {
        if (configServerAddress.equals(Constants.DEFAULT_DOMAINNAME)) {
            return "获取服务器地址列表信息Http异常,如果你是在日常环境，请忽略这个异常,configServerAddress=" + configServerAddress + ",";
        }
        else {
            return "获取服务器地址列表信息Http异常, configServerAddress=" + configServerAddress + ",";
        }
    }

}
