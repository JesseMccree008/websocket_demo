package org.jlw.websocket_demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WsMonitorController {
    @Autowired
    private WebSocketServer wss;

    @RequestMapping("/wsMonitorIndex")
    public String toWsMonitorIndex(){
        return "wsmonitor";
    }

    @RequestMapping("/getOnlineDetails")
    @ResponseBody
    public Map<String,Object> getOnlineDetails(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("count", wss.getCount());
        map.put("addrs", wss.getConnectData());
        return map;
    }

    @RequestMapping("/senAllMsg")
    @ResponseBody
    public Boolean sendAll(String msg) throws IOException {
        Integer ret = wss.sendAllMsg(msg);
        if (ret==wss.getCount()){
            return true;
        }else {
            return false;
        }
    }


}
