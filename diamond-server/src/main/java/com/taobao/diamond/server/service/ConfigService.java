/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.server.service;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.taobao.diamond.common.Constants;
import com.taobao.diamond.domain.ConfigInfo;
import com.taobao.diamond.domain.Page;
import com.taobao.diamond.md5.MD5;
import com.taobao.diamond.server.exception.ConfigServiceException;


@Service
public class ConfigService {

    private static final Log log = LogFactory.getLog(ConfigService.class);

    @Autowired
    private PersistService persistService;

    @Autowired
    private DiskService diskService;

    @Autowired
    private NotifyService notifyService;

    /**
     * content的MD5的缓存,key为group/dataId，value为md5值
     */
    private final ConcurrentHashMap<String, String> contentMD5Cache = new ConcurrentHashMap<String, String>();


    public String getConfigInfoPath(String dataId, String group) {
        StringBuilder sb = new StringBuilder("/");
        sb.append(Constants.BASE_DIR).append("/");
        sb.append(group).append("/");
        sb.append(dataId);
        return sb.toString();
    }


    public void updateMD5Cache(ConfigInfo configInfo) {
        this.contentMD5Cache.put(generateMD5CacheKey(configInfo.getDataId(), configInfo.getGroup()), MD5.getInstance()
            .getMD5String(configInfo.getContent()));
    }


    public String getContentMD5(String dataId, String group) {
        String key = generateMD5CacheKey(dataId, group);
        String md5 = this.contentMD5Cache.get(key);
        if (md5 == null) {
            synchronized (this) {
                // 二重检查
                return this.contentMD5Cache.get(key);
            }
        }
        else {
            return md5;
        }
    }


    String generateMD5CacheKey(String dataId, String group) {
        String key = group + "/" + dataId;
        return key;
    }


    String generatePath(String dataId, final String group) {
        StringBuilder sb = new StringBuilder("/");
        sb.append(Constants.BASE_DIR).append("/");
        sb.append(group).append("/");
        sb.append(dataId);
        return sb.toString();
    }


    public void removeConfigInfo(long id) {
        try {
            ConfigInfo configInfo = this.persistService.findConfigInfo(id);
            this.diskService.removeConfigInfo(configInfo.getDataId(), configInfo.getGroup());
            this.contentMD5Cache.remove(generateMD5CacheKey(configInfo.getDataId(), configInfo.getGroup()));
            this.persistService.removeConfigInfo(configInfo);
            // 通知其他节点
            this.notifyOtherNodes(configInfo.getDataId(), configInfo.getGroup());

        }
        catch (Exception e) {
            log.error("删除配置信息错误", e);
            throw new ConfigServiceException(e);
        }
    }


    public void addConfigInfo(String dataId, String group, String content) {
        checkParameter(dataId, group, content);
        ConfigInfo configInfo = new ConfigInfo(dataId, group, content);
        // 保存顺序：先数据库，再磁盘
        try {
            persistService.addConfigInfo(configInfo);
            // 切记更新缓存
            this.contentMD5Cache.put(generateMD5CacheKey(dataId, group), configInfo.getMd5());
            diskService.saveToDisk(configInfo);
            // 通知其他节点
            this.notifyOtherNodes(dataId, group);
        }
        catch (Exception e) {
            log.error("保存ConfigInfo失败", e);
            throw new ConfigServiceException(e);
        }
    }


    /**
     * 更新配置信息
     * 
     * @param dataId
     * @param group
     * @param content
     */
    public void updateConfigInfo(String dataId, String group, String content) {
        checkParameter(dataId, group, content);
        ConfigInfo configInfo = new ConfigInfo(dataId, group, content);
        // 先更新数据库，再更新磁盘
        try {
            persistService.updateConfigInfo(configInfo);
            // 切记更新缓存
            this.contentMD5Cache.put(generateMD5CacheKey(dataId, group), configInfo.getMd5());
            diskService.saveToDisk(configInfo);
            // 通知其他节点
            this.notifyOtherNodes(dataId, group);
        }
        catch (Exception e) {
            log.error("保存ConfigInfo失败", e);
            throw new ConfigServiceException(e);
        }
    }


    /**
     * 将配置信息从数据库加载到磁盘
     * 
     * @param id
     */
    public void loadConfigInfoToDisk(String dataId, String group) {
        try {
            ConfigInfo configInfo = this.persistService.findConfigInfo(dataId, group);
            if (configInfo != null) {
                this.contentMD5Cache.put(generateMD5CacheKey(dataId, group), configInfo.getMd5());
                this.diskService.saveToDisk(configInfo);
            }
            else {
                // 删除文件
                this.contentMD5Cache.remove(generateMD5CacheKey(dataId, group));
                this.diskService.removeConfigInfo(dataId, group);
            }
        }
        catch (Exception e) {
            log.error("保存ConfigInfo到磁盘失败", e);
            throw new ConfigServiceException(e);
        }
    }


    public ConfigInfo findConfigInfo(String dataId, String group) {
        return persistService.findConfigInfo(dataId, group);
    }


    /**
     * 分页查找配置信息
     * 
     * @param pageNo
     * @param pageSize
     * @param group
     * @param dataId
     * @return
     */
    public Page<ConfigInfo> findConfigInfo(final int pageNo, final int pageSize, final String group, final String dataId) {
        if (StringUtils.hasLength(dataId) && StringUtils.hasLength(group)) {
            ConfigInfo ConfigInfo = this.persistService.findConfigInfo(dataId, group);
            Page<ConfigInfo> page = new Page<ConfigInfo>();
            if (ConfigInfo != null) {
                page.setPageNumber(1);
                page.setTotalCount(1);
                page.setPagesAvailable(1);
                page.getPageItems().add(ConfigInfo);
            }
            return page;
        }
        else if (StringUtils.hasLength(dataId) && !StringUtils.hasLength(group)) {
            return this.persistService.findConfigInfoByDataId(pageNo, pageSize, dataId);
        }
        else if (!StringUtils.hasLength(dataId) && StringUtils.hasLength(group)) {
            return this.persistService.findConfigInfoByGroup(pageNo, pageSize, group);
        }
        else {
            return this.persistService.findAllConfigInfo(pageNo, pageSize);
        }
    }


    /**
     * 分页模糊查找配置信息
     * 
     * @param pageNo
     * @param pageSize
     * @param group
     * @param dataId
     * @return
     */
    public Page<ConfigInfo> findConfigInfoLike(final int pageNo, final int pageSize, final String group,
            final String dataId) {
        return this.persistService.findConfigInfoLike(pageNo, pageSize, dataId, group);
    }


    private void checkParameter(String dataId, String group, String content) {
        if (!StringUtils.hasLength(dataId) || StringUtils.containsWhitespace(dataId))
            throw new ConfigServiceException("无效的dataId");

        if (!StringUtils.hasLength(group) || StringUtils.containsWhitespace(group))
            throw new ConfigServiceException("无效的group");

        if (!StringUtils.hasLength(content))
            throw new ConfigServiceException("无效的content");
    }


    private void notifyOtherNodes(String dataId, String group) {
        this.notifyService.notifyConfigInfoChange(dataId, group);
    }


    public DiskService getDiskService() {
        return diskService;
    }


    public void setDiskService(DiskService diskService) {
        this.diskService = diskService;
    }


    public PersistService getPersistService() {
        return persistService;
    }


    public void setPersistService(PersistService persistService) {
        this.persistService = persistService;
    }


    public NotifyService getNotifyService() {
        return notifyService;
    }


    public void setNotifyService(NotifyService notifyService) {
        this.notifyService = notifyService;
    }

}
