/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.mockserver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.taobao.diamond.client.impl.DiamondClientFactory;
import com.taobao.diamond.common.Constants;


public class MockServer {

    private static class Pair {
        String configInfo;
        Boolean checkable;


        public Pair(String configInfo) {
            this.configInfo = configInfo;
            this.checkable = true;
        }
    }

    private static ConcurrentHashMap<String, Map<String, Pair>> staticConfigInfos =
            new ConcurrentHashMap<String, Map<String, Pair>>();
    private static volatile boolean testMode = false;


    public static void setUpMockServer() {
        testMode = true;
    }


    public static void tearDownMockServer() {
        staticConfigInfos.clear();
        DiamondClientFactory.getSingletonDiamondSubscriber().close();
        testMode = false;
    }


    public static String getConfigInfo(String dataId) {
        return getConfigInfo(dataId, null);
    }


    public static String getConfigInfo(String dataId, String group) {
        if (null == group) {
            group = Constants.DEFAULT_GROUP;
        }
        Map<String, Pair> pairs = staticConfigInfos.get(dataId);
        if (null == pairs) {
            return null;
        }
        Pair pair = pairs.get(group);
        if (null == pair) {
            return null;
        }
        pair.checkable = false;
        return pair.configInfo;
    }


    public static String getUpdateConfigInfo(String dataId) {
        return getUpdateConfigInfo(dataId, null);
    }


    public static String getUpdateConfigInfo(String dataId, String group) {
        if (null == group) {
            group = Constants.DEFAULT_GROUP;
        }
        Map<String, Pair> pairs = staticConfigInfos.get(dataId);
        if (null == pairs) {
            return null;
        }
        Pair pair = pairs.get(group);
        if (null != pair && pair.checkable) {
            pair.checkable = false;
            return pair.configInfo;
        }
        return null;
    }


    public static void setConfigInfos(Map<String, String> configInfos) {
        if (null != configInfos) {
            for (Map.Entry<String, String> entry : configInfos.entrySet()) {
                setConfigInfo(entry.getKey(), entry.getValue());
            }
        }
    }


    public static void setConfigInfo(String dataId, String configInfo) {
        setConfigInfo(dataId, null, configInfo);
    }


    public static void setConfigInfo(String dataId, String group, String configInfo) {
        if (null == group) {
            group = Constants.DEFAULT_GROUP;
        }
        Pair pair = new Pair(configInfo);
        Map<String, Pair> newPairs = new ConcurrentHashMap<String, Pair>();
        Map<String, Pair> pairs = staticConfigInfos.putIfAbsent(dataId, newPairs);
        if (null == pairs) {
            pairs = newPairs;
        }
        pairs.put(group, pair);
    }


    public static boolean isTestMode() {
        return testMode;
    }
}