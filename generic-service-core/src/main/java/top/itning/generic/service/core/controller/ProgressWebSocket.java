package top.itning.generic.service.core.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.itning.generic.service.common.websocket.event.WebSocketReceiveMessageEvent;
import top.itning.generic.service.common.websocket.event.WebSocketSendMessageEvent;
import top.itning.generic.service.common.websocket.type.WebSocketMessageType;

import javax.annotation.Nonnull;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static top.itning.generic.service.common.util.JsonUtils.GSON_INSTANCE;

/**
 * 日志输出
 *
 * @author itning
 */
@Component
@ServerEndpoint(value = "/p")
public final class ProgressWebSocket implements ApplicationEventPublisherAware, ApplicationListener<WebSocketSendMessageEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ProgressWebSocket.class);

    /**
     * 存放Session
     */
    private static final Map<String, Session> SESSION_MAP = new ConcurrentHashMap<>(16);

    private static ApplicationEventPublisher applicationEventPublisher;

    public static void sendMessage(String msg) {
        clearUnOpenSessionMap();
        SESSION_MAP.forEach((k, v) -> {
            try {
                v.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                logger.warn(e.getMessage());
            }
        });
    }

    private static void sendMessage(String token, String echo, String msg) {
        sendMessage(token, echo, WebSocketMessageType.TEXT, msg);
    }

    private static void sendMessage(String token, String echo, WebSocketMessageType type, String msg) {
        Session session = SESSION_MAP.get(token);
        if (null == session) {
            return;
        }
        if (!session.isOpen()) {
            SESSION_MAP.remove(token);
            return;
        }
        try {
            byte[] echoBytes = echo.getBytes(StandardCharsets.UTF_8);
            byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
            ByteBuffer byteBuffer = ByteBuffer.allocate(1 + echoBytes.length + msgBytes.length);
            byteBuffer.put(type.getId());
            byteBuffer.put(echoBytes);
            byteBuffer.put(msgBytes);
            byteBuffer.flip();
            session.getBasicRemote().sendBinary(byteBuffer);
        } catch (IOException e) {
            logger.warn(e.getMessage());
        } catch (Exception e) {
            logger.error("Send Message Catch Exception", e);
        }
    }

    /**
     * 清理SessionMap
     */
    @Scheduled(fixedDelay = 5000)
    private static void clearUnOpenSessionMap() {
        try {
            List<String> needCleanTokenList = new ArrayList<>();
            for (Map.Entry<String, Session> entry : SESSION_MAP.entrySet()) {
                if (!entry.getValue().isOpen()) {
                    needCleanTokenList.add(entry.getKey());
                }
            }
            if (!needCleanTokenList.isEmpty()) {
                logger.info("Clear UnOpen Session Size：{} List：{}", needCleanTokenList.size(), GSON_INSTANCE.toJson(needCleanTokenList));
                needCleanTokenList.forEach(SESSION_MAP::remove);
            }
        } catch (Exception e) {
            logger.error("Clear UnOpen Session Error", e);
        }
    }


    @OnOpen
    public void onOpen(Session session) {
        List<String> token = session.getRequestParameterMap().getOrDefault("token", Collections.emptyList());
        if (token.isEmpty()) {
            try {
                session.close();
            } catch (IOException e) {
                logger.warn("Close Session Error", e);
            }
            return;
        }
        SESSION_MAP.put(token.get(0), session);
    }

    @OnClose
    public void onClose(CloseReason closeReason, Session session) {
        logger.debug("On Close Session Id: {} Close Reason: {}", session.getId(), CloseReason.CloseCodes.getCloseCode(closeReason.getCloseCode().getCode()));
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        logger.debug("onMessage {}", message);
        //回复用户
        session.getBasicRemote().sendText("收到消息 ");
    }

    @OnMessage
    public void onMessage(ByteBuffer message) throws IOException {
        logger.debug("onMessage {}", message);
        applicationEventPublisher.publishEvent(new WebSocketReceiveMessageEvent(message));
    }

    @OnError
    public void onError(Session session, Throwable error) {
        SESSION_MAP.remove(session.getId());
        logger.warn("onError ", error);
    }

    @Override
    public void onApplicationEvent(@Nonnull WebSocketSendMessageEvent event) {
        sendMessage(event.getToken(), event.getEcho(), event.getType(), event.getMsg());
    }

    @Override
    public void setApplicationEventPublisher(@Nonnull ApplicationEventPublisher applicationEventPublisher) {
        ProgressWebSocket.applicationEventPublisher = applicationEventPublisher;
    }
}
