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

import java.text.MessageFormat;

/**
 * 单个diamond基本信息配置类
 * 
 * @filename DiamondConf.java
 * @author libinbin.pt
 * @datetime 2010-8-24 下午03:52:15
 */
public class DiamondConf {

    // diamondServer web访问地址
    private String diamondIp;

    // diamondServer web访问端口
    private String diamondPort;

    // diamondServer web登录用户名
    private String diamondUsername;

    // diamondServer web登录密码
    private String diamondPassword;
    private static MessageFormat DIAMONDURL_FORMAT = new MessageFormat("http://{0}:{1}");
    public DiamondConf(){
        
    }
    public DiamondConf(String diamondIp, String diamondPort, String diamondUsername, String diamondPassword) {
        this.diamondIp = diamondIp;
        this.diamondPort = diamondPort;
        this.diamondUsername = diamondUsername;
        this.diamondPassword = diamondPassword;
    }
    
    //合成diamond访问路径
    public String getDiamondConUrl(){
        return DIAMONDURL_FORMAT.format(new String[]{this.diamondIp,this.diamondPort});
    }

    public String getDiamondIp() {
        return diamondIp;
    }


    public void setDiamondIp(String diamondIp) {
        this.diamondIp = diamondIp;
    }


    public String getDiamondPort() {
        return diamondPort;
    }


    public void setDiamondPort(String diamondPort) {
        this.diamondPort = diamondPort;
    }


    public String getDiamondUsername() {
        return diamondUsername;
    }


    public void setDiamondUsername(String diamondUsername) {
        this.diamondUsername = diamondUsername;
    }


    public String getDiamondPassword() {
        return diamondPassword;
    }


    public void setDiamondPassword(String diamondPassword) {
        this.diamondPassword = diamondPassword;
    }


    @Override
    public String toString() {
        return "[diamondIp=" + diamondIp + ",diamondPort=" + diamondPort + ",diamondUsername=" + diamondUsername
                + ",diamondPassword=" + diamondPassword + "]";
    }

}
