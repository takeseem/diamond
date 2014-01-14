/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.io.watch.util;

import java.util.LinkedList;
import java.util.List;

import com.taobao.diamond.io.Path;


/**
 * 树形目录结构的节点，缓存lastModified
 * 
 * @author boyan
 * 
 */
public class PathNode {
    private List<PathNode> children;

    public Path path;

    private final boolean root;

    private long lastModified;


    public PathNode(Path path, boolean root) {
        super();
        this.children = new LinkedList<PathNode>();
        this.path = path;
        this.root = root;
        this.lastModified = path.lastModified();
    }


    public long lastModified() {
        return lastModified;
    }


    public boolean isRoot() {
        return root;
    }


    public List<PathNode> getChildren() {
        return children;
    }


    public void setPath(Path path) {
        this.path = path;
        this.lastModified = path.lastModified();
    }


    public Path getPath() {
        return path;
    }


    public void addChild(PathNode node) {
        this.children.add(node);
    }


    public void removeChild(PathNode node) {
        this.children.remove(node);
    }
}
