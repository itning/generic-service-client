package top.itning.generic.service.core.config;

import ch.qos.logback.core.OutputStreamAppender;
import top.itning.generic.service.core.controller.LogWebSocket;


/**
 * WebSocket OutputStreamAppender
 *
 * @author itning
 */
public class WebSocketOutputStreamAppender<E> extends OutputStreamAppender<E> {
    @Override
    public void start() {
        setOutputStream(LogWebSocket.getOutputStream());
        super.start();
    }
}
