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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.taobao.diamond.server.service.ConfigService;


/**
 * 用于其他节点通知的控制器
 * 
 * @author boyan
 * @date 2010-5-7
 */
@Controller
@RequestMapping("/notify.do")
public class NotifyController {

    @Autowired
    private ConfigService configService;


    public ConfigService getConfigService() {
        return configService;
    }


    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }


    /**
     * 通知配置信息改变
     * 
     * @param id
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, params = "method=notifyConfigInfo")
    public String notifyConfigInfo(@RequestParam("dataId") String dataId, @RequestParam("group") String group) {
        dataId = dataId.trim();
        group = group.trim();
        this.configService.loadConfigInfoToDisk(dataId, group);
        return "200";
    }

}
