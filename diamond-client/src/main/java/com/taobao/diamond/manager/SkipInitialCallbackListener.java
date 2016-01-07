/** Copyright 2013-2023 步步高商城. */
package com.taobao.diamond.manager;


/**
 * 
 * @author <a href="mailto:takeseem@gmail.com">杨浩</a>
 * @since 0.1.0
 */
public abstract class SkipInitialCallbackListener implements ManagerListener {
	private String first;
	
    public SkipInitialCallbackListener(String data) {
    	first = data;
    }

    @Override
    public void receiveConfigInfo(String configInfo) {
    	if (first == configInfo) return;
    	if (first == null || !first.equals(configInfo)) {
    		receiveConfigInfo0(configInfo);
    	}
    }
    
    //FIXME: 必须实现
    public abstract void receiveConfigInfo0(String data);
}
