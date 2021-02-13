package top.itning.generic.service.common.websocket.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author itning
 * @since 2021/2/13 22:37
 */
public class WebSocketReceiveMessageEvent extends ApplicationEvent {

    @Getter
    private final String receiveMessage;

    public WebSocketReceiveMessageEvent(String message) {
        super(message);
        this.receiveMessage = message;
    }

    public WebSocketReceiveMessageEvent(ByteBuffer message) {
        this(StandardCharsets.UTF_8.decode(message).toString());
    }
}
