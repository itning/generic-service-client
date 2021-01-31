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
    TEXT(0),
    /**
     * JSON类型
     */
    JSON(1),

    /**
     * nexus下载取消token
     */
    NEXUS_DOWNLOAD_CANCEL_TOKEN(2),

    /**
     * nexus下载进度
     */
    NEXUS_DOWNLOAD_PROGRESS(3),

    /**
     * nexus下载完成
     */
    NEXUS_DOWNLOAD_SUCCESS(4),

    /**
     * nexus下载失败
     */
    NEXUS_DOWNLOAD_FAILED(5),
    ;

    @Getter
    private final int id;

    WebSocketMessageType(int id) {
        this.id = id;
    }
}
