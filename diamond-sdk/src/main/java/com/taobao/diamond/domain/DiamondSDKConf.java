/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.domain;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;


public class DiamondSDKConf {

    private static final long serialVersionUID = 8378550702596810462L;

    private String serverId;

    // 多个diamond配置
    private List<DiamondConf> diamondConfs;


    // 构造时需要传入diamondConfs 列表
    public DiamondSDKConf(List<DiamondConf> diamondConfs) {
        this.diamondConfs = diamondConfs;
    }


    // setter,getter
    public String getServerId() {
        return serverId;
    }


    public void setServerId(String serverId) {
        this.serverId = serverId;
    }


    public List<DiamondConf> getDiamondConfs() {
        return diamondConfs;
    }


    public void setDiamondConfs(List<DiamondConf> diamondConfs) {
        this.diamondConfs = diamondConfs;
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
