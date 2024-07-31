package com.sky.task;

import com.sky.websocket.WebSocketServer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

//@Component
public class WebSocketTask {

    @Resource
    WebSocketServer webSocketServer;

    @Scheduled(cron = "0/5 * * * * ?")
    public void senMsg2Client() {
        webSocketServer.sendToAllClient("Sever Message from " + LocalDateTime.now());
    }
}
