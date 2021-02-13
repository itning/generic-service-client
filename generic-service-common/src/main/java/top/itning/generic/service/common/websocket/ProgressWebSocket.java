package top.itning.generic.service.common.websocket;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

/**
 * 日志输出
 *
 * @author itning
 */
@Component
@ServerEndpoint(value = "/p")
public final class ProgressWebSocket {
    private static final Logger logger = LoggerFactory.getLogger(ProgressWebSocket.class);

    private static final Gson GSON_INSTANCE = new Gson();
    /**
     * 存放Session
     */
    private static final Map<String, Session> SESSION_MAP = new ConcurrentHashMap<>(16);

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

    public static void sendMessage(String token, String echo, String msg) {
        sendMessage(token, echo, WebSocketMessageType.TEXT, msg);
    }

    public static void sendMessage(String token, String echo, WebSocketMessageType type, String msg) {
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
    public void onClose() {
        logger.debug("on close");
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        logger.debug("onMessage {}", message);
        //回复用户
        session.getBasicRemote().sendText("收到消息 ");
    }

    @OnError
    public void onError(Session session, Throwable error) {
        SESSION_MAP.remove(session.getId());
        logger.warn("onError ", error);
    }
}
