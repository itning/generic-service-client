package top.itning.generic.service.common.websocket;

import lombok.Getter;

/**
 * @author itning
 * @since 2020/10/26 11:07
 */
public enum WebSocketMessageType {
    /**
     * 文本类型
     */
    TEXT((byte) 0),
    /**
     * JSON类型
     */
    JSON((byte) 1),

    /**
     * nexus下载取消token
     */
    NEXUS_DOWNLOAD_CANCEL_TOKEN((byte) 2),

    /**
     * nexus下载进度
     */
    NEXUS_DOWNLOAD_PROGRESS((byte) 3),

    /**
     * nexus下载完成
     */
    NEXUS_DOWNLOAD_SUCCESS((byte) 4),

    /**
     * nexus下载失败
     */
    NEXUS_DOWNLOAD_FAILED((byte) 5),
    ;

    @Getter
    private final byte id;

    WebSocketMessageType(byte id) {
        this.id = id;
    }
}
