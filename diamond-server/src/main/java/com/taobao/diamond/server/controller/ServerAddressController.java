package com.taobao.diamond.server.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;

/**
 * Created with IntelliJ IDEA.
 * User: gaozhenlong
 * Date: 17-5-18
 * Time: 10:58 p.m.
 * To change this template use File | Settings | File Templates.
 */
@Controller
public class ServerAddressController {

//    @Value("${diamond.server.addr}")
    @Value("#{config['diamond.server.addr']}")
    private String diamondServerAddr;


    @PostConstruct
    public void init() {
        System.out.println("diamond.server.addr - " + diamondServerAddr);
    }


    @RequestMapping("url")
    @ResponseBody
    public String url() {
//        return "127.0.0.1";
        return diamondServerAddr;
    }

}
