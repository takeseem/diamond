/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.SpringLayout.Constraints;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Constants {
	public static final Log log = LogFactory.getLog(Constants.class);
	
    public static String DEFAULT_GROUP = "DEFAULT_GROUP";
    
    public static final String BASE_DIR = "config-data";

    public static String DEFAULT_DOMAINNAME = "a.b.c";

    public static String DAILY_DOMAINNAME = "d.e.f";

    public static int DEFAULT_PORT = 8080;

    public static final String NULL = "";

    public static final String DATAID = "dataId";

    public static final String GROUP = "group";

    public static final String LAST_MODIFIED = "Last-Modified";

    public static final String ACCEPT_ENCODING = "Accept-Encoding";

    public static final String CONTENT_ENCODING = "Content-Encoding";

    public static final String PROBE_MODIFY_REQUEST = "Probe-Modify-Request";

    public static final String PROBE_MODIFY_RESPONSE = "Probe-Modify-Response";

    public static final String PROBE_MODIFY_RESPONSE_NEW = "Probe-Modify-Response-New";

    public static final String CONTENT_MD5 = "Content-MD5";

    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";

    public static final String SPACING_INTERVAL = "client-spacing-interval";

    public static final int POLLING_INTERVAL_TIME = 15;// 秒

    public static final int ONCE_TIMEOUT = 2000;// 毫秒

    public static final int CONN_TIMEOUT = 2000;// 毫秒

    public static final int RECV_WAIT_TIMEOUT = ONCE_TIMEOUT * 5;// 毫秒

    /** 获取数据的URI地址，如果不带ip，那么轮换使用ServerAddress中的地址请求 */
    public static String HTTP_URI_FILE = "/url";

    /** 获取ServerAddress的配置uri */
    public static String CONFIG_HTTP_URI_FILE = HTTP_URI_FILE;
    
    public static String HTTP_URI_LOGIN = HTTP_URI_FILE;

    public static final String ENCODE = "UTF-8";

    public static final String LINE_SEPARATOR = Character.toString((char) 1);

    public static final String WORD_SEPARATOR = Character.toString((char) 2);

    public static final String DEFAULT_USERNAME = "xxx";

    public static final String DEFAULT_PASSWORD = "xxx";
    
    /*
     * 批量操作时, 单条数据的状态码
     */
    // 发生异常
    public static final int BATCH_OP_ERROR = -1;
    // 查询成功, 数据存在
    public static final int BATCH_QUERY_EXISTS = 1;
    // 查询成功, 数据不存在
    public static final int BATCH_QUERY_NONEXISTS = 2;
    // 新增成功
    public static final int BATCH_ADD_SUCCESS = 3;
    // 更新成功
    public static final int BATCH_UPDATE_SUCCESS = 4;

    /** FIXME: yanghao, 获取配置，本地 快照 服务地址？ */
	public static String GETCONFIG_LOCAL_SNAPSHOT_SERVER;

	/**
	 * 如果field的值未发生改变，那么使用fromField字段的值
	 * @param old Map&lt;字段名, 久值&gt;
	 * @param field 要改变的字段
	 * @param fromField 提取值的字段
	 */
	private static void setValue(Map<String, Object> old, String field, String fromField) {
		if (old.containsKey(field)) return; //改变了值，直接跳出
		
		try {
			Constants.class.getDeclaredField(field).set(Constants.class,
					Constants.class.getDeclaredField(fromField).get(Constants.class));
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new IllegalArgumentException("设置：" + field + ", 从：" + fromField, e);
		}
	}
	/** 配置优先级别：-D &gt; env &gt; diamond.properties  */
	public static void init() {
		File diamondFile = new File(System.getProperty("user.home"), "diamond/ServerAddress");
		if (!diamondFile.exists()) {
			diamondFile.getParentFile().mkdirs();
			try (OutputStream out = new FileOutputStream(diamondFile)) {
				out.write("localhost".getBytes());
			} catch (IOException e) {
				throw new IllegalStateException(diamondFile.toString(), e);
			}
		}
		List<Field> fields = new ArrayList<>();
		for (Field field : Constants.class.getDeclaredFields()) {
			if (Modifier.isPublic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
				fields.add(field);
			}
		}
		
		Properties props = new Properties();
		{
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			if (cl == null) cl = Constants.class.getClassLoader();
			try (InputStream in = cl.getResourceAsStream("diamond.properties")) {
				if (in != null) props.load(in);
			} catch (IOException e) {
				log.warn("load diamond.properties", e);
			}
		}
		props.putAll(System.getenv());
		props.putAll(System.getProperties());
		
		Map<String, Object> old = new HashMap<>(); 
		try {
			for (Field field : fields) {
				if (!props.containsKey(field.getName())) continue;
				old.put(field.getName(), field.get(Constants.class));
				
				String value = props.getProperty(field.getName());
				Class<?> clazz = field.getType();
				if (String.class.equals(clazz)) {
					field.set(Constraints.class, value);
				} else if (int.class.equals(clazz)) {
					if (value != null) {
						field.set(Constraints.class, Integer.parseInt(value));
					}
				} else {
					throw new IllegalArgumentException(field + " 设置 " + value + " 还未支持");
				}
			}
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
		
		setValue(old, "CONFIG_HTTP_URI_FILE", "HTTP_URI_FILE");
		setValue(old, "HTTP_URI_LOGIN", "HTTP_URI_FILE");
		setValue(old, "DAILY_DOMAINNAME", "DEFAULT_DOMAINNAME");
	}
	
	static {
		init();
	}
}
