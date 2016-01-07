/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.configinfo;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.taobao.diamond.common.Constants;


public class CacheData {

    private String dataId;
    private String group;
    private volatile String lastModifiedHeader = Constants.NULL;
    private volatile String md5 = Constants.NULL;
    private AtomicInteger domainNamePos = new AtomicInteger(0);
    private volatile String localConfigInfoFile = null;
    private volatile long localConfigInfoVersion;
    private volatile boolean useLocalConfigInfo = false;
    /**
     * 统计成功获取配置信息的次数
     */
    private final AtomicLong fetchCounter = new AtomicLong(0);


    public CacheData(String dataId, String group) {
        this.dataId = dataId;
        this.group = group;
    }


    public String getDataId() {
        return dataId;
    }


    public long getFetchCount() {
        return this.fetchCounter.get();
    }


    public long incrementFetchCountAndGet() {
        return this.fetchCounter.incrementAndGet();
    }


    public void setDataId(String dataId) {
        this.dataId = dataId;
    }


    public String getGroup() {
        return group;
    }


    public void setGroup(String group) {
        this.group = group;
    }


    public String getLocalConfigInfoFile() {
        return localConfigInfoFile;
    }


    public void setLocalConfigInfoFile(String localConfigInfoFile) {
        this.localConfigInfoFile = localConfigInfoFile;
    }


    public long getLocalConfigInfoVersion() {
        return localConfigInfoVersion;
    }


    public void setLocalConfigInfoVersion(long localConfigInfoVersion) {
        this.localConfigInfoVersion = localConfigInfoVersion;
    }


    public boolean isUseLocalConfigInfo() {
        return useLocalConfigInfo;
    }


    public void setUseLocalConfigInfo(boolean useLocalConfigInfo) {
        this.useLocalConfigInfo = useLocalConfigInfo;
    }


    public String getLastModifiedHeader() {
        return lastModifiedHeader;
    }


    public void setLastModifiedHeader(String lastModifiedHeader) {
        this.lastModifiedHeader = lastModifiedHeader;
    }


    public AtomicInteger getDomainNamePos() {
        return domainNamePos;
    }


    public void setDomainNamePos(int domainNamePos) {
        this.domainNamePos.set(domainNamePos);
    }


    public String getMd5() {
        return md5;
    }


    public void setMd5(String md5) {
        this.md5 = md5;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataId == null) ? 0 : dataId.hashCode());
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CacheData other = (CacheData) obj;
        if (dataId == null) {
            if (other.dataId != null)
                return false;
        }
        else if (!dataId.equals(other.dataId))
            return false;
        if (group == null) {
            if (other.group != null)
                return false;
        }
        else if (!group.equals(other.group))
            return false;
        return true;
    }

}
