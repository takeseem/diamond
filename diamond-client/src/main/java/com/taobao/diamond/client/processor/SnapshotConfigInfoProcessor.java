/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.client.processor;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.commons.lang.StringUtils;

import com.taobao.diamond.common.Constants;


public class SnapshotConfigInfoProcessor {
    private final String dir;


    public SnapshotConfigInfoProcessor(String dir) {
        super();
        this.dir = dir;
        File file = new File(this.dir);
        if (!file.exists()) {
            file.mkdir();
        }
    }


    public String getConfigInfomation(String dataId, String group) throws IOException {
        if (StringUtils.isBlank(dataId)) {
            return null;
        }
        if (StringUtils.isBlank(group)) {
            return null;
        }

        String path = dir + File.separator + group;
        final File dir = new File(path);
        if (!dir.exists()) {
            return null;
        }
        String filePath = path + File.separator + dataId;
        final File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        FileInputStream in = null;
        StringBuilder sb = new StringBuilder(512);
        try {
            in = new FileInputStream(file);
            byte[] data = new byte[8192];
            int n = -1;
            while ((n = in.read(data)) != -1) {
                sb.append(new String(data, 0, n, Constants.ENCODE));
            }
            return sb.toString();
        }
        finally {
            if (in != null) {
                in.close();
            }
        }

    }


    /**
     * 保存snapshot
     * 
     * @param dataId
     * @param group
     * @param config
     * @throws IOException
     */
    public void saveSnaptshot(String dataId, String group, String config) throws IOException {
        if (StringUtils.isBlank(dataId)) {
            throw new IllegalArgumentException("blank dataId");
        }
        if (StringUtils.isBlank(group)) {
            throw new IllegalArgumentException("blank group");
        }
        if (config == null) {
            config = "";
        }
        File file = getTargetFile(dataId, group);
        FileOutputStream out = null;
        PrintWriter writer = null;
        try {
            out = new FileOutputStream(file);
            BufferedOutputStream stream = new BufferedOutputStream(out);
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(stream, Constants.ENCODE)));
            writer.write(config);
            writer.flush();
        }
        finally {
            if (writer != null)
                writer.close();
            if (out != null) {
                out.close();
            }
        }
    }


    /**
     * 删除snapshot
     * 
     * @param dataId
     * @param group
     */
    public void removeSnapshot(String dataId, String group) {
        if (StringUtils.isBlank(dataId)) {
            return;
        }
        if (StringUtils.isBlank(group)) {
            return;
        }

        String path = dir + File.separator + group;
        final File dir = new File(path);
        if (!dir.exists()) {
            return;
        }
        String filePath = path + File.separator + dataId;
        final File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        file.delete();

        // 如果目录没有文件了，删除目录
        String[] list = dir.list();
        if (list == null || list.length == 0) {
            dir.delete();
        }
    }


    private File getTargetFile(String dataId, String group) throws IOException {
        String path = dir + File.separator + group;
        createDirIfNessary(path);
        String filePath = path + File.separator + dataId;
        File file = createFileIfNessary(filePath);
        return file;
    }


    private void createDirIfNessary(String path) {
        final File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }


    private File createFileIfNessary(String path) throws IOException {
        final File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }
}
