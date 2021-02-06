package org.jlw.websocket_demo.controller;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.*;

@Controller
public class WsMonitorController {

    /**
     * 指向管理页
     * @return
     */
    @RequestMapping("/wsMonitorIndex")
    public String toWsMonitorIndex(){
        return "wsmonitor";
    }

    /**
     * 获取全部在线客户端
     * @return
     */
    @RequestMapping("/getOnlineDetails")
    @ResponseBody
    public Map<String,Object> getOnlineDetails(){
        HashMap<String, Object> map = new HashMap<>(16);
        map.put("count", WebSocketServer.getOnlineCount());
        map.put("onlineDetails", WebSocketServer.getOnlineDetails());
        return map;
    }

    /**
     * 群发消息
     * @param msg
     * @return
     * @throws IOException
     */
    @RequestMapping("/senAllMsg")
    @ResponseBody
    public Boolean sendAll(String msg) throws IOException {
        Integer sum = WebSocketServer.sendAllMsg(msg);
        if (sum==WebSocketServer.getOnlineCount()){
            return true;
        }else {
            return false;
        }

    }

    /**
     * 根据userId推送单个窗口信息
     * @param userId
     * @param msg
     * @throws IOException
     */
    @RequestMapping("/sendMsg")
    @ResponseBody
    public Boolean sendMsg(String userId,String msg) throws IOException {
        return WebSocketServer.sendMessageByUserId(msg,userId);
    }
}
