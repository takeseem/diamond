/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.server.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.taobao.diamond.server.service.AdminService;


/**
 * µÇÂ¼µÇ³ö¿ØÖÆÆ÷
 * 
 * @author boyan
 * @date 2010-5-6
 */
@Controller
@RequestMapping("/login.do")
public class LoginController {
    @Autowired
    private AdminService adminService;


    @RequestMapping(params = "method=login", method = RequestMethod.POST)
    public String login(HttpServletRequest request, @RequestParam("username") String username,
            @RequestParam("password") String password, ModelMap modelMap) {
        if (adminService.login(username, password)) {
            request.getSession().setAttribute("user", username);
            return "admin/admin";
        }
        else {
            modelMap.addAttribute("message", "µÇÂ¼Ê§°Ü£¬ÓÃ»§ÃûÃÜÂë²»Æ¥Åä");
            return "login";
        }
    }


    public AdminService getAdminService() {
        return adminService;
    }


    public void setAdminService(AdminService adminService) {
        this.adminService = adminService;
    }


    @RequestMapping(params = "method=logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return "login";
    }
}
