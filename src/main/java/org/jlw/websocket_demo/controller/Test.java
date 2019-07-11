package org.jlw.websocket_demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class Test {

    @RequestMapping("/test")
    @ResponseBody
    public String test(){
        return "hello world!";
    }

    @RequestMapping("hello")
    public String hello(){
        return "hello";
    }
}
