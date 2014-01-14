/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.server.listener;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.taobao.diamond.server.utils.SessionHolder;


/**
 * 授权验证
 * 
 * @author boyan
 * @date 2010-5-5
 */
public class AuthorizationFilter implements Filter {

    public void destroy() {

    }


    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession session = httpRequest.getSession();
        SessionHolder.setSession(session);
        try {
            // 判断是否登录，没有就跳转到登录页面
            if (session.getAttribute("user") == null)
                ((HttpServletResponse) response).sendRedirect(httpRequest.getContextPath() + "/jsp/login.jsp");
            else
                chain.doFilter(httpRequest, response);
        }
        finally {
            SessionHolder.invalidate();
        }
    }


    public void init(FilterConfig filterConfig) throws ServletException {

    }

}
