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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.taobao.diamond.utils.ResourceUtils;


/**
 * 管理服务
 * 
 * @author boyan
 * @date 2010-5-5
 */
@Service
public class AdminService {

    private static final Log log = LogFactory.getLog(AdminService.class);

    private volatile Properties properties = new Properties();

    /**
     * user.properties的路径url
     */
    private URL url;


    public URL getUrl() {
        return url;
    }


    public AdminService() {
        loadUsers();
    }


    public void loadUsers() {
        Properties tempProperties = new Properties();
        InputStream in = null;
        try {
            url = ResourceUtils.getResourceURL("user.properties");
            in = new FileInputStream(url.getPath());
            tempProperties.load(in);
        }
        catch (IOException e) {
            log.error("加载user.properties文件失败", e);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {
                    log.error("关闭user.properties文件失败", e);
                }
            }
        }
        this.properties = tempProperties;
    }


    public synchronized boolean login(String userName, String password) {
        String passwordInFile = this.properties.getProperty(userName);
        if (passwordInFile != null)
            return passwordInFile.equals(password);
        else
            return false;
    }


    public synchronized boolean addUser(String userName, String password) {
        if (this.properties.containsKey(userName))
            return false;
        this.properties.put(userName, password);
        return saveToDisk();

    }


    private boolean saveToDisk() {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(url.getPath());
            this.properties.store(out, "add user");
            out.flush();
            return true;
        }
        catch (IOException e) {
            log.error("保存user.properties文件失败", e);
            return false;
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException e) {
                    log.error("关闭user.properties文件失败", e);
                }
            }
        }
    }


    public synchronized Map<String, String> getAllUsers() {
        Map<String, String> result = new HashMap<String, String>();
        Enumeration<?> enu = this.properties.keys();
        while (enu.hasMoreElements()) {
            String address = (String) enu.nextElement();
            String group = this.properties.getProperty(address);
            result.put(address, group);
        }
        return result;
    }


    public synchronized boolean updatePassword(String userName, String newPassword) {
        if (!this.properties.containsKey(userName))
            return false;
        this.properties.put(userName, newPassword);
        return saveToDisk();
    }


    public synchronized boolean removeUser(String userName) {
        if (this.properties.size() == 1)
            return false;
        if (!this.properties.containsKey(userName))
            return false;
        this.properties.remove(userName);
        return saveToDisk();
    }
}
