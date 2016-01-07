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


public class PageContextResult<T> {

    // 总记录数
    private long totalCounts = 0;
    // 总页数
    private long totalPages = 1;
    // 当前是哪页
    private long currentPage = 1;
    // 偏移位置
    private long offset = 0;
    // 偏移长度
    private long length = 1;
    // 每页记录数
    private long sizeOfPerPage = 10;
    // 记录数据集
    private List<T> diamondData;
    // 数据集的大小
    private long originalDataSize = 0;

    private int statusCode; // 状态码

    // 是否成功
    private boolean isSuccess = false;
    // 状态信息
    private String statusMsg;


    public PageContextResult() {

    }


    public PageContextResult(long totalCounts, long sizeOfPerPage) {
        this.totalCounts = totalCounts;
        this.sizeOfPerPage = sizeOfPerPage;
    }


    public void operation() {
        // =========检验sizeOfPerPage合法性
        if (totalCounts < 0) {
            totalCounts = 0;
        }

        // =========检验sizeOfPerPage合法性
        if (sizeOfPerPage <= 0)
            sizeOfPerPage = 1;

        // =========计算总页数
        // 如果总记录数能被每页大小整除，则总页数为(总记录数 /每页大小)
        // 否则总页数为(总记录数 /每页大小+1)
        if (totalCounts % sizeOfPerPage == 0) {
            totalPages = totalCounts / sizeOfPerPage;
        }
        else
            totalPages = totalCounts / sizeOfPerPage + 1;

        // =========纠正总页数
        if (totalPages <= 1)
            totalPages = 1;

        // =========检验currentPage合法性
        if (currentPage <= 1)
            currentPage = 1;
        else if (currentPage > totalPages)
            currentPage = totalPages;

        // =========计算偏移位置
        offset = (currentPage - 1) * sizeOfPerPage;

        // =========检验offset合法性
        if (offset < 0)
            offset = 0;

        // =========计算长度
        if (currentPage < totalPages)
            length = sizeOfPerPage;
        else
            length = totalCounts - (currentPage - 1) * sizeOfPerPage;
    }


    // setter , getter
    public long getTotalCounts() {
        return totalCounts;
    }


    public void setTotalCounts(long totalCounts) {
        this.totalCounts = totalCounts;
    }


    public long getTotalPages() {
        return totalPages;
    }


    public long getCurrentPage() {
        return currentPage;
    }


    public void setCurrentPage(long currentPage) {
        this.currentPage = currentPage;
    }


    public long getOffset() {
        return offset;
    }


    public long getLength() {
        return length;
    }


    public int getStatusCode() {
        return statusCode;
    }


    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }


    public long getSizeOfPerPage() {
        return sizeOfPerPage;
    }


    public void setTotalPages(long totalPages) {
        this.totalPages = totalPages;
    }


    public void setOffset(long offset) {
        this.offset = offset;
    }


    public void setLength(long length) {
        this.length = length;
    }


    public void setSizeOfPerPage(long sizeOfPerPage) {
        this.sizeOfPerPage = sizeOfPerPage;
    }


    public List<T> getDiamondData() {
        return diamondData;
    }


    public void setDiamondData(List<T> diamondData) {
        this.diamondData = diamondData;
    }


    public long getOriginalDataSize() {
        return originalDataSize;
    }


    public void setOriginalDataSize(long originalDataSize) {
        this.originalDataSize = originalDataSize;
    }


    public boolean isSuccess() {
        return isSuccess;
    }


    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }


    public String getStatusMsg() {
        return statusMsg;
    }


    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }


    @Override
    public String toString() {
        return "[totalCounts=" + totalCounts + ",totalPages=" + totalPages + ",currentPage=" + currentPage + ",offset="
                + offset + ",length=" + length + ",sizeOfPerPage=" + sizeOfPerPage + ",diamondData=" + diamondData
                + ",isSuccess=" + isSuccess + ",statusMsg=" + statusMsg + "]";
    }
}
