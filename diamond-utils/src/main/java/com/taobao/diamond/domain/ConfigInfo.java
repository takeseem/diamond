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

import java.io.PrintWriter;
import java.io.Serializable;

import com.taobao.diamond.md5.MD5;


/**
 * ≈‰÷√–≈œ¢¿‡
 * 
 * @author boyan
 * @date 2010-5-4
 */
public class ConfigInfo implements Serializable, Comparable<ConfigInfo> {
    static final long serialVersionUID = -1L;
    private String dataId;
    private String content;
    private String md5;

    private String group;
    private long id;


    public ConfigInfo() {

    }


    public ConfigInfo(String dataId, String group, String content) {
        super();
        this.dataId = dataId;
        this.content = content;
        this.group = group;
        if (this.content != null) {
            this.md5 = MD5.getInstance().getMD5String(this.content);
        }
    }


    public long getId() {
        return id;
    }


    public void setId(long id) {
        this.id = id;
    }


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


    public String getContent() {
        return content;
    }


    public void setContent(String content) {
        this.content = content;
    }


    public void dump(PrintWriter writer) {
        writer.write(this.content);
    }


    public String getMd5() {
        return md5;
    }


    public void setMd5(String md5) {
        this.md5 = md5;
    }


    public int compareTo(ConfigInfo o) {
        if (o == null)
            return 1;

        if (this.dataId == null && o.getDataId() != null)
            return -1;
        int cmpDataId = this.dataId.compareTo(o.getDataId());
        if (cmpDataId != 0) {
            return cmpDataId;
        }
        if (this.group == null && o.getGroup() != null)
            return -1;
        int cmpGroup = this.group.compareTo(o.getGroup());
        if (cmpGroup != 0) {
            return cmpGroup;
        }
        if (this.content == null && o.getContent() != null)
            return -1;
        int cmpContent = this.content.compareTo(o.getContent());
        if (cmpContent != 0) {
            return cmpContent;
        }
        return 0;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((content == null) ? 0 : content.hashCode());
        result = prime * result + ((dataId == null) ? 0 : dataId.hashCode());
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        result = prime * result + ((md5 == null) ? 0 : md5.hashCode());
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
        ConfigInfo other = (ConfigInfo) obj;
        if (content == null) {
            if (other.content != null)
                return false;
        }
        else if (!content.equals(other.content))
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
        if (md5 == null) {
            if (other.md5 != null)
                return false;
        }
        else if (!md5.equals(other.md5))
            return false;
        return true;
    }


    @Override
    public String toString() {
        return "[" + "dataId=" + dataId + "," + "groupName=" + group + "," + "context=" + content + "," + "]";
    }
}
