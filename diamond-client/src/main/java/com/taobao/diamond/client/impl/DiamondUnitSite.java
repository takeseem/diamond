/** Copyright 2013-2023 步步高商城. */
package com.taobao.diamond.client.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author <a href="mailto:takeseem@gmail.com">杨浩</a>
 * @since 0.1.0
 */
public class DiamondUnitSite {
	private static Map<String, DiamondEnv> envs = new HashMap<>();

	public static DiamondEnv getDiamondUnitEnv(String unitName) {
		if (unitName == null) unitName = "";
		DiamondEnv env = envs.get(unitName);
		if (env == null) {
			env = new DiamondEnv(unitName);
			envs.put(unitName, env);
		}
		return env;
	}

}
