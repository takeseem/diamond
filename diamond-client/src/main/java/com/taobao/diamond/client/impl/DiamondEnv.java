/** Copyright 2013-2023 步步高商城. */
package com.taobao.diamond.client.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.diamond.client.BatchHttpResult;
import com.taobao.diamond.common.Constants;
import com.taobao.diamond.manager.DiamondManager;
import com.taobao.diamond.manager.ManagerListener;
import com.taobao.diamond.manager.impl.DefaultDiamondManager;

/**
 * diamond环境，处理到diamond服务器通讯的?
 * @author <a href="mailto:takeseem@gmail.com">杨浩</a>
 * @since 0.1.0
 */
public class DiamondEnv implements AutoCloseable {
	protected final Log logger = LogFactory.getLog(getClass());
	private Map<String, DiamondManager> dmMap = new HashMap<>();
	
	private DiamondManager getDiamondManager(String dataId, String group) {
		String key = dataId + "#_#" + group;
		DiamondManager dm = dmMap.get(key);
		if (dm == null) {
			dm = new DefaultDiamondManager(group, dataId, new ArrayList<ManagerListener>());
			dmMap.put(key, dm);
		}
		return dm;
	}
	
	public DiamondEnv(String unitName) {
	}
	
	@Override
	public void close() {
		for (DiamondManager dm : dmMap.values()) {
			dm.close();
		}
		dmMap.clear();
	}

	public String getConfig(String dataId, String group, Object gETCONFIG_LOCAL_SNAPSHOT_SERVER, long timeout) throws IOException {
		if (logger.isDebugEnabled()) logger.debug("dataId=" + dataId + ", " + group + ", " + gETCONFIG_LOCAL_SNAPSHOT_SERVER + ", timeout=" + timeout);
		DiamondManager dm = getDiamondManager(dataId, group);
		return dm.getAvailableConfigureInfomation(timeout);
	}

	public List<ManagerListener> getListeners(String dataId, String group) {
		return getDiamondManager(dataId, group).getManagerListeners();
	}

	public void removeListener(String dataId, String group, ManagerListener listener) {
		getListeners(dataId, group).remove(listener);
	}
	
	public void addListeners(String dataId, String group, List<? extends ManagerListener> handlers) {
		getListeners(dataId, group).addAll(handlers);
	}

	public BatchHttpResult batchQuery(List<String> dataIds, String group, int timeout) {
		if (logger.isDebugEnabled()) logger.debug("dataId=" + dataIds + ", " + group + ", timeout=" + timeout);
		BatchHttpResult result = new BatchHttpResult();
		for (String dataId : dataIds) {
			try {
				result.addResult(dataId, group,
						getConfig(dataId, group, Constants.GETCONFIG_LOCAL_SNAPSHOT_SERVER, timeout));
			} catch (IOException ex) {
				result.setSuccess(false);
				result.setStatusCode(ex.toString());
				break;
			}
		}
		return result;
	}

}
