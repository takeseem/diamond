/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.server.controller;

import static com.taobao.diamond.common.Constants.LINE_SEPARATOR;
import static com.taobao.diamond.common.Constants.WORD_SEPARATOR;

import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.taobao.diamond.common.Constants;
import com.taobao.diamond.server.service.ConfigService;
import com.taobao.diamond.server.service.DiskService;
import com.taobao.diamond.server.utils.GlobalCounter;


/**
 * 处理配置信息获取和提交的controller
 * 
 * @author boyan
 * @date 2010-5-4
 */
@Controller
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @Autowired
    private DiskService diskService;


    public String getConfig(HttpServletRequest request, HttpServletResponse response, String dataId, String group) {
        response.setHeader("Content-Type", "text/html;charset=GBK");
        final String address = getRemortIP(request);
        if (address == null) {
            // 未找到远端地址，返回400错误
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "400";
        }

        if (GlobalCounter.getCounter().decrementAndGet() >= 0) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return "503";
        }

        String md5 = this.configService.getContentMD5(dataId, group);
        if (md5 == null) {
            return "404";
        }

        response.setHeader(Constants.CONTENT_MD5, md5);

        // 正在被修改，返回304，这里的检查并没有办法保证一致性，因此做double-check尽力保证
        if (diskService.isModified(dataId, group)) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return "304";
        }
        String path = configService.getConfigInfoPath(dataId, group);
        // 再次检查
        if (diskService.isModified(dataId, group)) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return "304";
        }
        // 禁用缓存
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-cache,no-store");
        return "forward:" + path;
    }


    public String getProbeModifyResult(HttpServletRequest request, HttpServletResponse response, String probeModify) {
        response.setHeader("Content-Type", "text/html;charset=GBK");
        final String address = getRemortIP(request);
        if (address == null) {
            // 未找到远端地址，返回400错误
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "400";
        }

        if (GlobalCounter.getCounter().decrementAndGet() >= 0) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return "503";
        }

        final List<ConfigKey> configKeyList = getConfigKeyList(probeModify);

        StringBuilder resultBuilder = new StringBuilder();
        for (ConfigKey key : configKeyList) {
            String md5 = this.configService.getContentMD5(key.getDataId(), key.getGroup());
            if (!StringUtils.equals(md5, key.getMd5())) {
                resultBuilder.append(key.getDataId()).append(WORD_SEPARATOR).append(key.getGroup())
                    .append(LINE_SEPARATOR);
            }
        }

        String returnHeader = resultBuilder.toString();
        try {
            returnHeader = URLEncoder.encode(resultBuilder.toString(), "UTF-8");
        }
        catch (Exception e) {
            // ignore
        }

        request.setAttribute("content", returnHeader);
        // 禁用缓存
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-cache,no-store");
        return "200";
    }


    public ConfigService getConfigService() {
        return configService;
    }


    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }


    public DiskService getDiskService() {
        return diskService;
    }


    public void setDiskService(DiskService diskService) {
        this.diskService = diskService;
    }


    /**
     * 查找真实的IP地址
     * 
     * @param request
     * @return
     */
    public String getRemortIP(HttpServletRequest request) {
        if (request.getHeader("x-forwarded-for") == null) {
            return request.getRemoteAddr();
        }
        return request.getHeader("x-forwarded-for");
    }


    public static List<ConfigKey> getConfigKeyList(String configKeysString) {
        List<ConfigKey> configKeyList = new LinkedList<ConfigKey>();
        if (null == configKeysString || "".equals(configKeysString)) {
            return configKeyList;
        }
        String[] configKeyStrings = configKeysString.split(LINE_SEPARATOR);
        for (String configKeyString : configKeyStrings) {
            String[] configKey = configKeyString.split(WORD_SEPARATOR);
            if (configKey.length > 3) {
                continue;
            }
            ConfigKey key = new ConfigKey();
            if ("".equals(configKey[0])) {
                continue;
            }
            key.setDataId(configKey[0]);
            if (configKey.length >= 2 && !"".equals(configKey[1])) {
                key.setGroup(configKey[1]);
            }
            if (configKey.length == 3 && !"".equals(configKey[2])) {
                key.setMd5(configKey[2]);
            }
            configKeyList.add(key);
        }

        return configKeyList;
    }

    public static class ConfigKey {
        private String dataId;
        private String group;
        private String md5;


        public String getDataId() {
            return dataId;
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


        public String getMd5() {
            return md5;
        }


        public void setMd5(String md5) {
            this.md5 = md5;
        }


        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("DataID: ").append(dataId).append("\r\n");
            sb.append("Group: ").append((null == group ? "" : group)).append("\r\n");
            sb.append("MD5: ").append((null == md5 ? "" : md5)).append("\r\n");
            return sb.toString();
        }
    }
}
