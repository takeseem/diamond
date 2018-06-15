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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.taobao.diamond.common.Constants;
import com.taobao.diamond.domain.ConfigInfo;
import com.taobao.diamond.domain.ConfigInfoEx;
import com.taobao.diamond.domain.Page;
import com.taobao.diamond.server.exception.ConfigServiceException;
import com.taobao.diamond.server.service.AdminService;
import com.taobao.diamond.server.service.ConfigService;
import com.taobao.diamond.server.utils.DiamondUtils;
import com.taobao.diamond.server.utils.GlobalCounter;
import com.taobao.diamond.utils.JSONUtils;


/**
 * 管理控制器
 * 
 * @author boyan
 * @date 2010-5-6
 */
@Controller
@RequestMapping("/admin.do")
public class AdminController {

    private static final Log log = LogFactory.getLog(AdminController.class);

    @Autowired
    private AdminService adminService;

    @Autowired
    private ConfigService configService;


    @RequestMapping(params = "method=postConfig", method = RequestMethod.POST)
    public String postConfig(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("dataId") String dataId, @RequestParam("group") String group,
            @RequestParam("content") String content, ModelMap modelMap) {
        response.setCharacterEncoding("GBK");

        boolean checkSuccess = true;
        String errorMessage = "参数错误";
        if (StringUtils.isBlank(dataId) || DiamondUtils.hasInvalidChar(dataId.trim())) {
            checkSuccess = false;
            errorMessage = "无效的DataId";
        }
        if (StringUtils.isBlank(group) || DiamondUtils.hasInvalidChar(group.trim())) {
            checkSuccess = false;
            errorMessage = "无效的分组";
        }
        if (StringUtils.isBlank(content)) {
            checkSuccess = false;
            errorMessage = "无效的内容";
        }
        if (!checkSuccess) {
            modelMap.addAttribute("message", errorMessage);
            return "/admin/config/new";
        }

        dataId = dataId.trim();
        group = group.trim();

        this.configService.addConfigInfo(dataId, group, content);

        modelMap.addAttribute("message", "提交成功!");
        return listConfig(request, response, dataId, group, 1, 20, modelMap);
    }


    @RequestMapping(params = "method=deleteConfig", method = RequestMethod.GET)
    public String deleteConfig(HttpServletRequest request, HttpServletResponse response, @RequestParam("id") long id,
            ModelMap modelMap) {
        // 删除数据
        this.configService.removeConfigInfo(id);
        modelMap.addAttribute("message", "删除成功!");
        return "/admin/config/list";
    }


    @RequestMapping(params = "method=upload", method = RequestMethod.POST)
    public String upload(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("dataId") String dataId, @RequestParam("group") String group,
            @RequestParam("contentFile") MultipartFile contentFile, ModelMap modelMap) {
        response.setCharacterEncoding("GBK");

        boolean checkSuccess = true;
        String errorMessage = "参数错误";
        if (StringUtils.isBlank(dataId) || DiamondUtils.hasInvalidChar(dataId.trim())) {
            checkSuccess = false;
            errorMessage = "无效的DataId";
        }
        if (StringUtils.isBlank(group) || DiamondUtils.hasInvalidChar(group.trim())) {
            checkSuccess = false;
            errorMessage = "无效的分组";
        }
        String content = getContentFromFile(contentFile);
        if (StringUtils.isBlank(content)) {
            checkSuccess = false;
            errorMessage = "无效的内容";
        }
        if (!checkSuccess) {
            modelMap.addAttribute("message", errorMessage);
            return "/admin/config/upload";
        }

        this.configService.addConfigInfo(dataId, group, content);
        modelMap.addAttribute("message", "提交成功!");
        return listConfig(request, response, dataId, group, 1, 20, modelMap);
    }


    @RequestMapping(params = "method=reupload", method = RequestMethod.POST)
    public String reupload(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("dataId") String dataId, @RequestParam("group") String group,
            @RequestParam("contentFile") MultipartFile contentFile, ModelMap modelMap) {
        response.setCharacterEncoding("GBK");

        boolean checkSuccess = true;
        String errorMessage = "参数错误";
        String content = getContentFromFile(contentFile);
        ConfigInfo configInfo = new ConfigInfo(dataId, group, content);
        if (StringUtils.isBlank(dataId) || DiamondUtils.hasInvalidChar(dataId.trim())) {
            checkSuccess = false;
            errorMessage = "无效的DataId";
        }
        if (StringUtils.isBlank(group) || DiamondUtils.hasInvalidChar(group.trim())) {
            checkSuccess = false;
            errorMessage = "无效的分组";
        }
        if (StringUtils.isBlank(content)) {
            checkSuccess = false;
            errorMessage = "无效的内容";
        }
        if (!checkSuccess) {
            modelMap.addAttribute("message", errorMessage);
            modelMap.addAttribute("configInfo", configInfo);
            return "/admin/config/edit";
        }

        this.configService.updateConfigInfo(dataId, group, content);

        modelMap.addAttribute("message", "更新成功!");
        return listConfig(request, response, dataId, group, 1, 20, modelMap);
    }


    private String getContentFromFile(MultipartFile contentFile) {
        try {
            String charset = Constants.ENCODE;
            final String content = new String(contentFile.getBytes(), charset);
            return content;
        }
        catch (Exception e) {
            throw new ConfigServiceException(e);
        }
    }


    @RequestMapping(params = "method=updateConfig", method = RequestMethod.POST)
    public String updateConfig(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("dataId") String dataId, @RequestParam("group") String group,
            @RequestParam("content") String content, ModelMap modelMap) {
        response.setCharacterEncoding("GBK");

        ConfigInfo configInfo = new ConfigInfo(dataId, group, content);
        boolean checkSuccess = true;
        String errorMessage = "参数错误";
        if (StringUtils.isBlank(dataId) || DiamondUtils.hasInvalidChar(dataId.trim())) {
            checkSuccess = false;
            errorMessage = "无效的DataId";
        }
        if (StringUtils.isBlank(group) || DiamondUtils.hasInvalidChar(group.trim())) {
            checkSuccess = false;
            errorMessage = "无效的分组";
        }
        if (StringUtils.isBlank(content)) {
            checkSuccess = false;
            errorMessage = "无效的内容";
        }
        if (!checkSuccess) {
            modelMap.addAttribute("message", errorMessage);
            modelMap.addAttribute("configInfo", configInfo);
            return "/admin/config/edit";
        }

        this.configService.updateConfigInfo(dataId, group, content);

        modelMap.addAttribute("message", "提交成功!");
        return listConfig(request, response, dataId, group, 1, 20, modelMap);
    }


    @RequestMapping(params = "method=listConfig", method = RequestMethod.GET)
    public String listConfig(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("dataId") String dataId, @RequestParam("group") String group,
            @RequestParam("pageNo") int pageNo, @RequestParam("pageSize") int pageSize, ModelMap modelMap) {
        Page<ConfigInfo> page = this.configService.findConfigInfo(pageNo, pageSize, group, dataId);

        String accept = request.getHeader("Accept");
        if (accept != null && accept.indexOf("application/json") >= 0) {
            try {
                String json = JSONUtils.serializeObject(page);
                modelMap.addAttribute("pageJson", json);
            }
            catch (Exception e) {
                log.error("序列化page对象出错", e);
            }
            return "/admin/config/list_json";
        }
        else {
            modelMap.addAttribute("dataId", dataId);
            modelMap.addAttribute("group", group);
            modelMap.addAttribute("page", page);
            return "/admin/config/list";
        }
    }


    @RequestMapping(params = "method=listConfigLike", method = RequestMethod.GET)
    public String listConfigLike(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("dataId") String dataId, @RequestParam("group") String group,
            @RequestParam("pageNo") int pageNo, @RequestParam("pageSize") int pageSize, ModelMap modelMap) {
        if (StringUtils.isBlank(dataId) && StringUtils.isBlank(group)) {
            modelMap.addAttribute("message", "模糊查询请至少设置一个查询参数");
            return "/admin/config/list";
        }
        Page<ConfigInfo> page = this.configService.findConfigInfoLike(pageNo, pageSize, group, dataId);

        String accept = request.getHeader("Accept");
        if (accept != null && accept.indexOf("application/json") >= 0) {
            try {
                String json = JSONUtils.serializeObject(page);
                modelMap.addAttribute("pageJson", json);
            }
            catch (Exception e) {
                log.error("序列化page对象出错", e);
            }
            return "/admin/config/list_json";
        }
        else {
            modelMap.addAttribute("page", page);
            modelMap.addAttribute("dataId", dataId);
            modelMap.addAttribute("group", group);
            modelMap.addAttribute("method", "listConfigLike");
            return "/admin/config/list";
        }
    }


    @RequestMapping(params = "method=detailConfig", method = RequestMethod.GET)
    public String getConfigInfo(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("dataId") String dataId, @RequestParam("group") String group, ModelMap modelMap) {
        dataId = dataId.trim();
        group = group.trim();
        ConfigInfo configInfo = this.configService.findConfigInfo(dataId, group);
        modelMap.addAttribute("configInfo", configInfo);
        return "/admin/config/edit";
    }


    // =========================== 批量处理 ============================== //

    @RequestMapping(params = "method=batchQuery", method = RequestMethod.POST)
    public String batchQuery(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("dataIds") String dataIds, @RequestParam("group") String group, ModelMap modelMap) {

        response.setCharacterEncoding("GBK");

        // 这里抛出的异常, 会产生一个500错误, 返回给sdk, sdk会将500错误记录到日志中
        if (StringUtils.isBlank(dataIds)) {
            throw new IllegalArgumentException("批量查询, dataIds不能为空");
        }
        // group对批量操作的每一条数据都相同, 不需要在for循环里面进行判断
        if (StringUtils.isBlank(group)) {
            throw new IllegalArgumentException("批量查询, group不能为空或者包含非法字符");
        }

        // 分解dataId
        String[] dataIdArray = dataIds.split(Constants.WORD_SEPARATOR);
        group = group.trim();

        List<ConfigInfoEx> configInfoExList = new ArrayList<ConfigInfoEx>();
        for (String dataId : dataIdArray) {
            ConfigInfoEx configInfoEx = new ConfigInfoEx();
            configInfoEx.setDataId(dataId);
            configInfoEx.setGroup(group);
            configInfoExList.add(configInfoEx);
            try {
                if (StringUtils.isBlank(dataId)) {
                    configInfoEx.setStatus(Constants.BATCH_QUERY_NONEXISTS);
                    configInfoEx.setMessage("dataId is blank");
                    continue;
                }

                // 查询数据库
                ConfigInfo configInfo = this.configService.findConfigInfo(dataId, group);
                if (configInfo == null) {
                    // 没有异常, 说明查询成功, 但数据不存在, 设置不存在的状态码
                    configInfoEx.setStatus(Constants.BATCH_QUERY_NONEXISTS);
                    configInfoEx.setMessage("query data does not exist");
                }
                else {
                    // 没有异常, 说明查询成功, 而且数据存在, 设置存在的状态码
                    String content = configInfo.getContent();
                    configInfoEx.setContent(content);
                    configInfoEx.setStatus(Constants.BATCH_QUERY_EXISTS);
                    configInfoEx.setMessage("query success");
                }
            }
            catch (Exception e) {
                log.error("批量查询, 在查询这个dataId时出错, dataId=" + dataId + ",group=" + group, e);
                // 出现异常, 设置异常状态码
                configInfoEx.setStatus(Constants.BATCH_OP_ERROR);
                configInfoEx.setMessage("query error: " + e.getMessage());
            }
        }

        String json = null;
        try {
            json = JSONUtils.serializeObject(configInfoExList);
        }
        catch (Exception e) {
            log.error("批量查询结果序列化出错, json=" + json, e);
        }
        modelMap.addAttribute("json", json);

        return "/admin/config/batch_result";
    }


    @RequestMapping(params = "method=batchAddOrUpdate", method = RequestMethod.POST)
    public String batchAddOrUpdate(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("allDataIdAndContent") String allDataIdAndContent, @RequestParam("group") String group,
            ModelMap modelMap) {

        response.setCharacterEncoding("GBK");

        // 这里抛出的异常, 会产生一个500错误, 返回给sdk, sdk会将500错误记录到日志中
        if (StringUtils.isBlank(allDataIdAndContent)) {
            throw new IllegalArgumentException("批量写, allDataIdAndContent不能为空");
        }
        // group对批量操作的每一条数据都相同, 不需要在for循环里面进行判断
        if (StringUtils.isBlank(group) || DiamondUtils.hasInvalidChar(group)) {
            throw new IllegalArgumentException("批量写, group不能为空或者包含非法字符");
        }

        String[] dataIdAndContentArray = allDataIdAndContent.split(Constants.LINE_SEPARATOR);
        group = group.trim();

        List<ConfigInfoEx> configInfoExList = new ArrayList<ConfigInfoEx>();
        for (String dataIdAndContent : dataIdAndContentArray) {
            String dataId = dataIdAndContent.substring(0, dataIdAndContent.indexOf(Constants.WORD_SEPARATOR));
            String content = dataIdAndContent.substring(dataIdAndContent.indexOf(Constants.WORD_SEPARATOR) + 1);
            ConfigInfoEx configInfoEx = new ConfigInfoEx();
            configInfoEx.setDataId(dataId);
            configInfoEx.setGroup(group);
            configInfoEx.setContent(content);

            try {
                // 判断dataId是否包含非法字符
                if (StringUtils.isBlank(dataId) || DiamondUtils.hasInvalidChar(dataId)) {
                    // 这里抛出的异常, 会在下面catch, 然后设置状态, 保证一个dataId的异常不会影响其他dataId
                    throw new IllegalArgumentException("批量写, dataId不能包含非法字符");
                }
                // 判断内容是否为空
                if (StringUtils.isBlank(content)) {
                    throw new IllegalArgumentException("批量写, 内容不能为空");
                }

                // 查询数据库
                ConfigInfo configInfo = this.configService.findConfigInfo(dataId, group);
                if (configInfo == null) {
                    // 数据不存在, 新增
                    this.configService.addConfigInfo(dataId, group, content);
                    // 新增成功, 设置状态码
                    configInfoEx.setStatus(Constants.BATCH_ADD_SUCCESS);
                    configInfoEx.setMessage("add success");
                }
                else {
                    // 数据存在, 更新
                    this.configService.updateConfigInfo(dataId, group, content);
                    // 更新成功, 设置状态码
                    configInfoEx.setStatus(Constants.BATCH_UPDATE_SUCCESS);
                    configInfoEx.setMessage("update success");
                }
            }
            catch (Exception e) {
                log.error("批量写这条数据时出错, dataId=" + dataId + ",group=" + group + ",content=" + content, e);
                // 出现异常, 设置异常状态码
                configInfoEx.setStatus(Constants.BATCH_OP_ERROR);
                configInfoEx.setMessage("batch write error: " + e.getMessage());
            }
            configInfoExList.add(configInfoEx);
        }

        String json = null;
        try {
            json = JSONUtils.serializeObject(configInfoExList);
        }
        catch (Exception e) {
            log.error("批量写, 结果序列化出错, json=" + json, e);
        }
        modelMap.addAttribute("json", json);

        return "/admin/config/batch_result";
    }


    @RequestMapping(params = "method=listUser", method = RequestMethod.GET)
    public String listUser(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        Map<String, String> userMap = this.adminService.getAllUsers();
        modelMap.addAttribute("userMap", userMap);
        return "/admin/user/list";
    }


    @RequestMapping(params = "method=addUser", method = RequestMethod.POST)
    public String addUser(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("userName") String userName, @RequestParam("password") String password, ModelMap modelMap) {
        if (StringUtils.isBlank(userName) || DiamondUtils.hasInvalidChar(userName.trim())) {
            modelMap.addAttribute("message", "无效的用户名");
            return listUser(request, response, modelMap);
        }
        if (StringUtils.isBlank(password) || DiamondUtils.hasInvalidChar(password.trim())) {
            modelMap.addAttribute("message", "无效的密码");
            return "/admin/user/new";
        }
        if (this.adminService.addUser(userName, password))
            modelMap.addAttribute("message", "添加成功!");
        else
            modelMap.addAttribute("message", "添加失败!");
        return listUser(request, response, modelMap);
    }


    @RequestMapping(params = "method=deleteUser", method = RequestMethod.GET)
    public String deleteUser(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("userName") String userName, ModelMap modelMap) {
        if (StringUtils.isBlank(userName) || DiamondUtils.hasInvalidChar(userName.trim())) {
            modelMap.addAttribute("message", "无效的用户名");
            return listUser(request, response, modelMap);
        }
        if (this.adminService.removeUser(userName)) {
            modelMap.addAttribute("message", "删除成功!");
        }
        else {
            modelMap.addAttribute("message", "删除失败!");
        }
        return listUser(request, response, modelMap);
    }


    @RequestMapping(params = "method=changePassword", method = RequestMethod.GET)
    public String changePassword(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("userName") String userName, @RequestParam("password") String password, ModelMap modelMap) {

        userName = userName.trim();
        password = password.trim();

        if (StringUtils.isBlank(userName) || DiamondUtils.hasInvalidChar(userName.trim())) {
            modelMap.addAttribute("message", "无效的用户名");
            return listUser(request, response, modelMap);
        }
        if (StringUtils.isBlank(password) || DiamondUtils.hasInvalidChar(password.trim())) {
            modelMap.addAttribute("message", "无效的新密码");
            return listUser(request, response, modelMap);
        }
        if (this.adminService.updatePassword(userName, password)) {
            modelMap.addAttribute("message", "更改成功,下次登录请用新密码！");
        }
        else {
            modelMap.addAttribute("message", "更改失败!");
        }
        return listUser(request, response, modelMap);
    }


    @RequestMapping(params = "method=setRefuseRequestCount", method = RequestMethod.POST)
    public String setRefuseRequestCount(@RequestParam("count") long count, ModelMap modelMap) {
        if (count <= 0) {
            modelMap.addAttribute("message", "非法的计数");
            return "/admin/count";
        }
        GlobalCounter.getCounter().set(count);
        modelMap.addAttribute("message", "设置成功!");
        return getRefuseRequestCount(modelMap);
    }


    @RequestMapping(params = "method=getRefuseRequestCount", method = RequestMethod.GET)
    public String getRefuseRequestCount(ModelMap modelMap) {
        modelMap.addAttribute("count", GlobalCounter.getCounter().get());
        return "/admin/count";
    }


    /**
     * 重新文件加载用户信息
     * 
     * @param modelMap
     * @return
     */
    @RequestMapping(params = "method=reloadUser", method = RequestMethod.GET)
    public String reloadUser(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        this.adminService.loadUsers();
        modelMap.addAttribute("message", "加载成功!");
        return listUser(request, response, modelMap);
    }


    public AdminService getAdminService() {
        return adminService;
    }


    public void setAdminService(AdminService adminService) {
        this.adminService = adminService;
    }


    public ConfigService getConfigService() {
        return configService;
    }


    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

}
