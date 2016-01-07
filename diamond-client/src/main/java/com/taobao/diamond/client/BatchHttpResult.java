/** Copyright 2013-2023 步步高商城. */
package com.taobao.diamond.client;

import java.util.List;

import com.taobao.diamond.domain.ConfigInfoEx;

/**
 * 
 * @author <a href="mailto:takeseem@gmail.com">杨浩</a>
 * @since 0.1.0
 */
public class BatchHttpResult {
	private boolean success = true;
	private String statusCode;
	private List<ConfigInfoEx> result;
	
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	public List<ConfigInfoEx> getResult() {
		return result;
	}

	public void addResult(String dataId, String group, String content) {
		result.add(new ConfigInfoEx(dataId, group, content));
	}
}
