package top.itning.generic.service.core.constant;

import lombok.Getter;

/**
 * @author itning
 * @since 2020/10/26 11:07
 */
public enum WebSocketMessageType {
    /**
     * 文本类型
     */
    TEXT(0),
    /**
     * JSON类型
     */
    JSON(1);

    @Getter
    private final int id;

    WebSocketMessageType(int id) {
        this.id = id;
    }
}
