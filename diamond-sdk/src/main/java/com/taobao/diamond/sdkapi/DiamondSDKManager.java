/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.sdkapi;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.taobao.diamond.domain.BatchContextResult;
import com.taobao.diamond.domain.ConfigInfo;
import com.taobao.diamond.domain.ConfigInfoEx;
import com.taobao.diamond.domain.ContextResult;
import com.taobao.diamond.domain.DiamondSDKConf;
import com.taobao.diamond.domain.PageContextResult;

/**
 * 定义SDK对外开放的数据访问接口
 * 
 * @filename DiamondSDKManager.java
 * @author libinbin.pt
 * @datetime 2010-7-16 下午04:03:28
 * 
 *           {@link #exists(String, String, String)}
 */
public interface DiamondSDKManager {
	
	public Map<String, DiamondSDKConf> getDiamondSDKConfMaps();

	// /////////////////////////////////////////推送数据接口定义////////////////////////////////////////
	/**
	 * 使用指定的diamond来推送数据
	 * 
	 * @param dataId
	 * @param groupName
	 * @param context
	 * @param serverId
	 * @return ContextResult 单个对象
	 */
	public ContextResult pulish(String dataId, String groupName,
			String context, String serverId);

	// /////////////////////////////////////////推送修改后的数据接口定义////////////////////////////////////////
	/**
	 * 使用指定的diamond来推送修改后的数据,修改前先检查数据存在性
	 * 
	 * @param dataId
	 * @param groupName
	 * @param context
	 * @param serverId
	 * @return ContextResult 单个对象
	 */
	public ContextResult pulishAfterModified(String dataId, String groupName,
			String context, String serverId);

	// /////////////////////////////////////////模糊查询接口定义////////////////////////////////////////
	/**
	 * 根据指定的 dataId和组名到指定的diamond上查询数据列表 如果模式中包含符号'*',则会自动替换为'%'并使用[ like ]语句
	 * 如果模式中不包含符号'*'并且不为空串（包括" "）,则使用[ = ]语句
	 * 
	 * @param dataIdPattern
	 * @param groupNamePattern
	 * @param serverId
	 * @param currentPage
	 * @param sizeOfPerPage
	 * @return PageContextResult<ConfigInfo> 单个对象
	 * @throws SQLException
	 */
	public PageContextResult<ConfigInfo> queryBy(String dataIdPattern,
			String groupNamePattern, String serverId, long currentPage,
			long sizeOfPerPage);

	/**
	 * 根据指定的 dataId,组名和content到指定配置的diamond来查询数据列表 如果模式中包含符号'*',则会自动替换为'%'并使用[
	 * like ]语句 如果模式中不包含符号'*'并且不为空串（包括" "）,则使用[ = ]语句
	 * 
	 * @param dataIdPattern
	 * @param groupNamePattern
	 * @param contentPattern
	 * @param serverId
	 * @param currentPage
	 * @param sizeOfPerPage
	 * @return PageContextResult<ConfigInfo> 单个对象
	 * @throws SQLException
	 */
	public PageContextResult<ConfigInfo> queryBy(String dataIdPattern,
			String groupNamePattern, String contentPattern, String serverId,
			long currentPage, long sizeOfPerPage);

	// /////////////////////////////////////////精确查询接口定义////////////////////////////////////////
	/**
	 * 根据指定的dataId和组名到指定的diamond上查询数据列表
	 * 
	 * @param dataId
	 * @param groupName
	 * @param serverId
	 * @return ContextResult 单个对象
	 * @throws SQLException
	 */
	public ContextResult queryByDataIdAndGroupName(String dataId,
			String groupName, String serverId);

	// /////////////////////////////////////////移除信息接口定义////////////////////////////////////
	/**
	 * 移除特定服务器上id指定的配置信息
	 * 
	 * @param serverId
	 * @param id
	 * @return ContextResult 单个对象
	 */
	public ContextResult unpublish(String serverId, long id);
	
	
	/**
     * 批量查询
     * 
     * @param groupName
     * @param dataIds
     * @param serverId
     * @return
     */
    public BatchContextResult<ConfigInfoEx> batchQuery(String serverId, String groupName, List<String> dataIds);


    /**
     * 批量新增或更新
     * 
     * @param serverId
     * @param groupName
     * @param dataId2ContentMap
     *            key:dataId,value:content
     * @return
     */
    public BatchContextResult<ConfigInfoEx> batchAddOrUpdate(String serverId, String groupName,
            Map<String/* dataId */, String/* content */> dataId2ContentMap);

}
