package org.jlw.websocket_demo.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jlw.websocket_demo.domain.ConnectInfo;
import org.jlw.websocket_demo.utils.WebsocketUtil;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

@ServerEndpoint("/websocket/{sid}")
@Component
public class WebSocketServer {
    static Log log = LogFactory.getLog(WebSocketServer.class);
    /**
     * 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的
     */
    private static int onlineCount = 0;
    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象
     */
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<WebSocketServer>();
    //    /**
//     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
//     */
    private Session session;
    //    /**
//     * 接收sid
//     */
    private String sid = "";
    /**
     * 存储链接信息
     */
    public static List<ConnectInfo> connects = new ArrayList<ConnectInfo>();


    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        AtomicBoolean b = new AtomicBoolean(true);
        this.session = session;
//        验证用户是否已注册
        connects.forEach((c) -> {
            if (c.getUserId().equals(sid)) {
                b.set(false);
            }
        });
        if (b.get()) {
            //加入set中
            webSocketSet.add(this);
            //在线数加1
            addOnlineCount();
            //获取客户端ip
            InetSocketAddress remoteAddress = WebsocketUtil.getRemoteAddress(session);
            //保存链接信息
            ConnectInfo connectInfo = new ConnectInfo();
            connectInfo.setUserId(sid);
            connectInfo.setUserIp(remoteAddress);
            connectInfo.setWebSocketServer(this);
            connects.add(connectInfo);
            log.info("有新窗口开始监听:" + sid + ",当前在线人数为" + getOnlineCount());
            this.sid = sid;
            try {
                sendMessage(this, "连接成功");
            } catch (IOException e) {
                log.error("websocket IO异常");
            }
        } else {
            try {
                sendMessage(this, "链接失败，用户已注册");
            } catch (IOException e) {
                log.error("websocket IO异常");
            }
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        //从set中删除
        webSocketSet.remove(this);
        //从链接信息中删除
        String id = this.sid;
        /**java*/
//        Iterator<ConnectInfo> iterator = connects.iterator();
//        while(iterator.hasNext()){
//            ConnectInfo connectInfo = iterator.next();
//            if (connectInfo.getUserId().equals(id)){
//               iterator.remove();
//            }
//        }
        /**java8*/
        connects.removeIf(c -> c.getUserId().equals(id));
        //在线数减1
        subOnlineCount();
        log.info("id为" + id + "的连接已关闭！当前在线链接为" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message) {
//        log.info("收到来自窗口" + sid + "的信息:" + message);
        //群发消息
        /*for (WebSocketServer item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }
//    /**
//     * 实现服务器主动推送(测试)
//     */
//    public void sendMessage(String message) throws IOException {
//        this.session.getBasicRemote().sendText(message);
//    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(WebSocketServer webSocketServer, String message) throws IOException {
        webSocketServer.session.getBasicRemote().sendText(message);
    }

    /**
     * 根据当前系统注册的链接定向推送
     *
     * @param message
     * @param userId
     * @throws IOException
     */
    public static Boolean sendMessageByUserId(String message, String userId) throws IOException {
        Boolean result = false;
        if (!Objects.isNull(userId)) {
            WebSocketServer server = WebSocketServer.getWebSocketServerByUserId(userId);
            if (!Objects.isNull(server)) {
                server.sendMessage(server, message);
                result = true;
            }
        }
        return result;
    }

    /**
     * 由服务端群发消息
     *
     * @param msg
     * @throws IOException
     */
    public static Integer sendAllMsg(String msg) throws IOException {
        int sum = 0;
        for (WebSocketServer item : webSocketSet
        ) {
            item.sendMessage(item, msg);
            sum++;
        }
        return sum;
    }

    /**
     * 获取在线数量
     *
     * @return
     */
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    /**
     * 通过userId获取指定WebSocketServer
     *
     * @param userId
     * @return
     */
    public static synchronized WebSocketServer getWebSocketServerByUserId(String userId) {
        for (ConnectInfo c : connects
        ) {
            if (userId.equals(c.getUserId())) {
                return c.getWebSocketServer();
            }
        }
        return null;
    }

    /**
     * 获取所有已注册链接
     *
     * @return
     */
    public static synchronized List<ConnectInfo> getOnlineDetails() {
        return connects;
    }

    /**
     * 添加在线数量
     */
    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    /**
     * 减少在线数量
     */
    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }

}
