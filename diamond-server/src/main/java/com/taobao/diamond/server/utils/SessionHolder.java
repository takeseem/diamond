/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.server.utils;

import javax.servlet.http.HttpSession;


/**
 * 通过ThreadLocal传递HttpSession
 * 
 * @author boyan
 * @date 2010-5-26
 */
public class SessionHolder {
    private static ThreadLocal<HttpSession> sessionThreadLocal = new ThreadLocal<HttpSession>() {

        @Override
        protected HttpSession initialValue() {
            return null;
        }

    };


    public static void invalidate() {
        sessionThreadLocal.remove();
    }


    public static void setSession(HttpSession session) {
        sessionThreadLocal.set(session);
    }


    public static HttpSession getSession() {
        return sessionThreadLocal.get();
    }
}
