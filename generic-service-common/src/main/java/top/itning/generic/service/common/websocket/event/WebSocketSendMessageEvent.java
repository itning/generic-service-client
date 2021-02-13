package top.itning.generic.service.common.websocket.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import top.itning.generic.service.common.websocket.type.WebSocketMessageType;

/**
 * @author itning
 * @since 2021/2/13 22:37
 */
public class WebSocketSendMessageEvent extends ApplicationEvent {
    @Getter
    private final String token;
    @Getter
    private final String echo;
    @Getter
    private final WebSocketMessageType type;
    @Getter
    private final String msg;

    public WebSocketSendMessageEvent(String token, String echo, String msg) {
        this(token, echo, WebSocketMessageType.TEXT, msg);
    }

    public WebSocketSendMessageEvent(String token, String echo, WebSocketMessageType type, String msg) {
        super(token);
        this.token = token;
        this.echo = echo;
        this.type = type;
        this.msg = msg;
    }
}