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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.diamond.common.Constants;
import com.taobao.diamond.configinfo.CacheData;
import com.taobao.diamond.io.FileSystem;
import com.taobao.diamond.io.Path;
import com.taobao.diamond.io.watch.StandardWatchEventKind;
import com.taobao.diamond.io.watch.WatchEvent;
import com.taobao.diamond.io.watch.WatchKey;
import com.taobao.diamond.io.watch.WatchService;
import com.taobao.diamond.utils.FileUtils;


public class LocalConfigInfoProcessor {
    private static final Log log = LogFactory.getLog(LocalConfigInfoProcessor.class);
    private ScheduledExecutorService singleExecutor = Executors.newSingleThreadScheduledExecutor();;

    private final Map<String/* filePath */, Long/* version */> existFiles = new HashMap<String, Long>();

    private volatile boolean isRun;
    private String rootPath = null;


    /**
     * 获取本地配置
     * 
     * @param cacheData
     * @param force
     *            强制获取，在没有变更的时候不返回null
     * @return
     * @throws IOException
     */
    public String getLocalConfigureInfomation(CacheData cacheData, boolean force) throws IOException {
        String filePath = getFilePath(cacheData.getDataId(), cacheData.getGroup());
        if (!existFiles.containsKey(filePath)) {
            if (cacheData.isUseLocalConfigInfo()) {
                cacheData.setLastModifiedHeader(Constants.NULL);
                cacheData.setMd5(Constants.NULL);
                cacheData.setLocalConfigInfoFile(null);
                cacheData.setLocalConfigInfoVersion(0L);
                cacheData.setUseLocalConfigInfo(false);
            }
            return null;
        }
        if (force) {
            log.info("主动从本地获取配置数据, dataId:" + cacheData.getDataId() + ", group:" + cacheData.getGroup());

            String content = FileUtils.getFileContent(filePath);
            return content;
        }
        // 判断是否变更，没有变更，返回null
        if (!filePath.equals(cacheData.getLocalConfigInfoFile())
                || existFiles.get(filePath) != cacheData.getLocalConfigInfoVersion()) {
            String content = FileUtils.getFileContent(filePath);
            cacheData.setLocalConfigInfoFile(filePath);
            cacheData.setLocalConfigInfoVersion(existFiles.get(filePath));
            cacheData.setUseLocalConfigInfo(true);

            if (log.isInfoEnabled()) {
                log.info("本地配置数据发生变化, dataId:" + cacheData.getDataId() + ", group:" + cacheData.getGroup());
            }

            return content;
        }
        else {
            cacheData.setUseLocalConfigInfo(true);

            if (log.isInfoEnabled()) {
                log.debug("本地配置数据没有发生变化, dataId:" + cacheData.getDataId() + ", group:" + cacheData.getGroup());
            }

            return null;
        }
    }


    String getFilePath(String dataId, String group) {
        StringBuilder filePathBuilder = new StringBuilder();
        filePathBuilder.append(rootPath).append("/").append(Constants.BASE_DIR).append("/").append(group).append("/")
            .append(dataId);
        File file = new File(filePathBuilder.toString());
        return file.getAbsolutePath();
    }


    public synchronized void start(String rootPath) {
        if (this.isRun) {
            return;
        }
        this.rootPath = rootPath;
        this.isRun = true;
        if (this.singleExecutor == null || singleExecutor.isTerminated()) {
            singleExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        initDataDir(rootPath);
        startCheckLocalDir(rootPath);
    }


    private void initDataDir(String rootPath) {
        try {
            File flie = new File(rootPath);
            flie.mkdir();
        }
        catch (Exception e) {
        }
    }


    public synchronized void stop() {
        if (!this.isRun) {
            return;
        }
        this.isRun = false;
        this.singleExecutor.shutdownNow();
        this.singleExecutor = null;
    }


    private void startCheckLocalDir(final String filePath) {
        final WatchService watcher = FileSystem.getDefault().newWatchService();

        Path path = new Path(new File(filePath));
        // 注册事件
        watcher.register(path, true, StandardWatchEventKind.ENTRY_CREATE, StandardWatchEventKind.ENTRY_DELETE,
            StandardWatchEventKind.ENTRY_MODIFY);
        // 第一次运行，主动check
        checkAtFirst(watcher);
        singleExecutor.execute(new Runnable() {
            public void run() {
                log.debug(">>>>>>已经开始监控目录<<<<<<");
                // 无限循环等待事件
                while (isRun) {
                    // 凭证
                    WatchKey key;
                    try {
                        key = watcher.take();
                    }
                    catch (InterruptedException x) {
                        continue;
                    }
                    // reset，如果无效，跳出循环,无效可能是监听的目录被删除
                    if (!processEvents(key)) {
                        log.error("reset unvalid,监控服务失效");
                        break;
                    }
                }
                log.debug(">>>>>>退出监控目录<<<<<<");
                watcher.close();

            }

        });
    }


    private void checkAtFirst(final WatchService watcher) {
        watcher.check();
        WatchKey key = null;
        while ((key = watcher.poll()) != null) {
            processEvents(key);
        }
    }


    /**
     * 处理触发的事件
     * 
     * @param key
     * @return
     */
    @SuppressWarnings({ "unchecked" })
    private boolean processEvents(WatchKey key) {
        /**
         * 获取事件集合
         */
        for (WatchEvent<?> event : key.pollEvents()) {
            // 事件的类型
            // WatchEvent.Kind<?> kind = event.kind();

            // 通过context方法得到发生事件的path
            WatchEvent<Path> ev = (WatchEvent<Path>) event;
            Path eventPath = ev.context();

            String realPath = eventPath.getAbsolutePath();
            if (ev.kind() == StandardWatchEventKind.ENTRY_CREATE || ev.kind() == StandardWatchEventKind.ENTRY_MODIFY) {

                String grandpaDir = null;
                try {
                    grandpaDir = FileUtils.getGrandpaDir(realPath);
                }
                catch (Exception e1) {

                }
                if (!Constants.BASE_DIR.equals(grandpaDir)) {
                    log.error("无效的文件进入监控目录: " + realPath);
                    continue;
                }
                existFiles.put(realPath, System.currentTimeMillis());
                if (log.isInfoEnabled()) {
                    log.info(realPath + "文件被添加或更新");
                }
            }
            else if (ev.kind() == StandardWatchEventKind.ENTRY_DELETE) {
                String grandpaDir = null;
                try {
                    grandpaDir = FileUtils.getGrandpaDir(realPath);
                }
                catch (Exception e1) {

                }
                if (Constants.BASE_DIR.equals(grandpaDir)) {
                    // 删除的是文件
                    existFiles.remove(realPath);
                    if (log.isInfoEnabled()) {
                        log.info(realPath + "文件被被删除");
                    }
                }
                else {
                    // 删除的是目录
                    Set<String> keySet = new HashSet<String>(existFiles.keySet());
                    for (String filePath : keySet) {
                        if (filePath.startsWith(realPath)) {
                            existFiles.remove(filePath);
                            if (log.isInfoEnabled()) {
                                log.info(filePath + "文件被删除");
                            }
                        }
                    }

                }
            }
        }
        return key.reset();
    }
}
