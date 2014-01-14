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

import java.io.Serializable;


/**
 * 配置信息的类
 * 
 * @author aoqiong
 * 
 */
public class ConfigureInfomation implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6684264073344815420L;

    private String dataId;
    private String group;
    private String ConfigureInfomation;


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


    public String getConfigureInfomation() {
        return ConfigureInfomation;
    }


    public void setConfigureInfomation(String configureInfomation) {
        ConfigureInfomation = configureInfomation;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ConfigureInfomation == null) ? 0 : ConfigureInfomation.hashCode());
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
        ConfigureInfomation other = (ConfigureInfomation) obj;
        if (ConfigureInfomation == null) {
            if (other.ConfigureInfomation != null)
                return false;
        }
        else if (!ConfigureInfomation.equals(other.ConfigureInfomation))
            return false;
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


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DataId: ").append(dataId);
        sb.append(", Group: ").append(group);
        sb.append(", ConfigureInfomation: ").append(ConfigureInfomation);
        return sb.toString();
    }
}
