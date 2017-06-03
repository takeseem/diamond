/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.io.watch;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.taobao.diamond.io.Path;
import com.taobao.diamond.io.watch.util.PathNode;


/**
 * WatchKey，表示一个注册的的凭证
 * 
 * @author boyan
 * @date 2010-5-4
 */
public class WatchKey {

    private volatile boolean valid;

    private final PathNode root;

    private List<WatchEvent<?>> changedEvents;

    private final Set<WatchEvent.Kind<?>> filterSet = new HashSet<WatchEvent.Kind<?>>();

    private final WatchService watcher;


    public WatchKey(final Path path, final WatchService watcher, boolean fireCreatedEventOnIndex,
            WatchEvent.Kind<?>... events) {
        valid = true;
        this.watcher = watcher;
        // 建立内存索引
        this.root = new PathNode(path, true);
        if (events != null) {
            for (WatchEvent.Kind<?> event : events) {
                filterSet.add(event);
            }
        }
        LinkedList<WatchEvent<?>> changedEvents = new LinkedList<WatchEvent<?>>();
        index(this.root, fireCreatedEventOnIndex, changedEvents);
        this.changedEvents = changedEvents;
    }


    /**
     * 索引目录
     * 
     * @param node
     */
    private void index(PathNode node, boolean fireCreatedEventOnIndex, LinkedList<WatchEvent<?>> changedEvents) {
        File file = node.getPath().getFile();
        if (!file.isDirectory()) {
            return;
        }
        File[] subFiles = file.listFiles();
        if (subFiles != null) {
            for (File subFile : subFiles) {
                PathNode subNode = new PathNode(new Path(subFile), false);
                if (fireCreatedEventOnIndex) {
                    changedEvents.add(new WatchEvent<Path>(StandardWatchEventKind.ENTRY_CREATE, 1, subNode.getPath()));
                }
                node.addChild(subNode);
                if (subNode.getPath().isDirectory()) {
                    index(subNode, fireCreatedEventOnIndex, changedEvents);
                }
            }
        }
    }


    public void cancel() {
        this.valid = false;
    }


    @Override
    public String toString() {
        return "WatchKey [root=" + root + ", valid=" + valid + "]";
    }


    public boolean isValid() {
        return valid && root != null;
    }


    public List<WatchEvent<?>> pollEvents() {
        if (changedEvents != null) {
            List<WatchEvent<?>> result = changedEvents;
            changedEvents = null;
            return result;
        }
        return null;
    }


    /**
     * 检测是否有变化
     * 
     * @return
     */
    boolean check() {
        if (this.changedEvents != null && this.changedEvents.size() > 0)
            return true;
        if (!this.valid)
            return false;
        List<WatchEvent<?>> list = new LinkedList<WatchEvent<?>>();
        if (check(root, list)) {
            this.changedEvents = list;
            return true;
        }
        else {
            return false;
        }
    }


    private boolean check(PathNode node, List<WatchEvent<?>> changedEvents) {
        Path nodePath = node.getPath();
        File nodeNewFile = new File(nodePath.getAbsolutePath());
        if (nodePath != null) {
            if (node.isRoot()) {
                if (!nodeNewFile.exists())
                    return fireOnRootDeleted(changedEvents, nodeNewFile);
                else {
                    return checkNodeChildren(node, changedEvents, nodeNewFile);
                }
            }
            else {
                return checkNodeChildren(node, changedEvents, nodeNewFile);
            }
        }
        else
            throw new IllegalStateException("PathNode没有path");
    }


    private boolean checkNodeChildren(PathNode node, List<WatchEvent<?>> changedEvents, File nodeNewFile) {
        boolean changed = false;
        Iterator<PathNode> it = node.getChildren().iterator();
        // 用于判断是否有新增文件或者目录的现有名称集合
        Set<String> childNameSet = new HashSet<String>();
        while (it.hasNext()) {
            PathNode child = it.next();
            Path childPath = child.getPath();
            childNameSet.add(childPath.getName());
            File childNewFile = new File(childPath.getAbsolutePath());
            // 1、判断文件是否还存在
            if (!childNewFile.exists() && filterSet.contains(StandardWatchEventKind.ENTRY_DELETE)) {
                changed = true;
                changedEvents.add(new WatchEvent<Path>(StandardWatchEventKind.ENTRY_DELETE, 1, childPath));
                it.remove();// 移除节点
            }
            // 2、如果是文件，判断是否被修改
            if (childPath.isFile()) {
                if (checkFile(changedEvents, child, childNewFile) && !changed) {
                    changed = true;
                }

            }
            // 3、递归检测目录
            if (childPath.isDirectory()) {
                if (check(child, changedEvents) && !changed) {
                    changed = true;
                }
            }
        }

        // 查看是否有新增文件
        File[] newChildFiles = nodeNewFile.listFiles();
        if(newChildFiles!=null)
        for (File newChildFile : newChildFiles) {
            if (!childNameSet.contains(newChildFile.getName())
                    && filterSet.contains(StandardWatchEventKind.ENTRY_CREATE)) {
                changed = true;
                Path newChildPath = new Path(newChildFile);
                changedEvents.add(new WatchEvent<Path>(StandardWatchEventKind.ENTRY_CREATE, 1, newChildPath));
                PathNode newSubNode = new PathNode(newChildPath, false);
                node.addChild(newSubNode);// 新增子节点
                // 如果是目录，递归调用
                if (newChildFile.isDirectory()) {
                    checkNodeChildren(newSubNode, changedEvents, newChildFile);
                }
            }
        }
        return changed;
    }


    private boolean checkFile(List<WatchEvent<?>> changedEvents, PathNode child, File childNewFile) {
        boolean changed = false;
        // 查看文件是否被修改
        if (childNewFile.lastModified() != child.lastModified()
                && filterSet.contains(StandardWatchEventKind.ENTRY_MODIFY)) {
            changed = true;
            Path newChildPath = new Path(childNewFile);
            changedEvents.add(new WatchEvent<Path>(StandardWatchEventKind.ENTRY_MODIFY, 1, newChildPath));
            child.setPath(newChildPath);// 更新path
        }
        return changed;
    }


    private boolean fireOnRootDeleted(List<WatchEvent<?>> changedEvents, File nodeNewFile) {
        this.valid = false;
        if (filterSet.contains(StandardWatchEventKind.ENTRY_DELETE)) {
            changedEvents.add(new WatchEvent<Path>(StandardWatchEventKind.ENTRY_DELETE, 1, new Path(nodeNewFile)));
            return true;
        }
        return false;
    }


    public boolean reset() {
        if (!valid)
            return false;
        if (root == null)
            return false;
        return this.watcher.resetKey(this);
    }
}
