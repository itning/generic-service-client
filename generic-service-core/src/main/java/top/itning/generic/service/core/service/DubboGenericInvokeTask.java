package top.itning.generic.service.core.service;

import com.alibaba.dubbo.rpc.RpcContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.remoting.Constants;
import org.apache.dubbo.rpc.service.GenericService;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.CollectionUtils;
import top.itning.generic.service.common.websocket.event.WebSocketSendMessageEvent;
import top.itning.generic.service.common.websocket.type.WebSocketMessageType;
import top.itning.generic.service.core.bo.DubboGenericRequestBO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static top.itning.generic.service.common.util.JsonUtils.GSON_INSTANCE;
import static top.itning.generic.service.common.util.JsonUtils.GSON_INSTANCE_WITH_PRETTY_PRINT;

/**
 * Dubbo泛化调用任务
 *
 * @author itning
 * @since 2021/1/3 15:22
 */
@SuppressWarnings("deprecation")
@Slf4j
public class DubboGenericInvokeTask implements Runnable {

    private static final String MDC_TRADE_ID = "INNER_TRACE_ID";

    private final ApplicationEventPublisher applicationEventPublisher;
    private final DubboGenericRequestBO dubboGenericRequestBO;
    private final ApplicationConfig applicationConfig;

    public DubboGenericInvokeTask(ApplicationEventPublisher applicationEventPublisher, ApplicationConfig applicationConfig, DubboGenericRequestBO dubboGenericRequestBO) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.applicationConfig = applicationConfig;
        this.dubboGenericRequestBO = dubboGenericRequestBO;
    }

    @Override
    public void run() {
        ReferenceConfig<GenericService> reference = null;
        try {
            putTrace();
            reference = new ReferenceConfig<>();
            // 弱类型接口名
            reference.setInterface(dubboGenericRequestBO.getInterfaceName());
            // 声明为泛化接口
            reference.setGeneric(true);

            reference.setApplication(applicationConfig);

            reference.setGroup(dubboGenericRequestBO.getGroup());

            reference.setVersion(dubboGenericRequestBO.getVersion());

            reference.setCheck(false);

            reference.setUrl(dubboGenericRequestBO.getUrl());

            reference.setRetries(0);

            reference.setTimeout(5000);

            reference.setParameters(Collections.singletonMap(Constants.RECONNECT_KEY, "false"));

            GenericService genericService = reference.get();

            List<String> parameterTypeList = new ArrayList<>();
            List<Object> argList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(dubboGenericRequestBO.getParams())) {
                dubboGenericRequestBO.getParams().forEach(item -> {
                    String key = item.keySet().toArray(new String[]{})[0];
                    parameterTypeList.add(key);
                    argList.add(item.get(key));
                });
            }

            Object result = genericService.$invoke(dubboGenericRequestBO.getMethod(), parameterTypeList.toArray(new String[]{}), argList.toArray());

            if (null == result) {
                log.info("Result Is Null");
                sendMessage("调用成功：入参[" + GSON_INSTANCE.toJson(dubboGenericRequestBO) + "]\n");
                return;
            }

            log.info("Result Type：{} Value:{}", result.getClass().getName(), result);

            String jsonString = GSON_INSTANCE_WITH_PRETTY_PRINT.toJson(result);

            sendMessage("调用成功：TraceID[" + MDC.get(MDC_TRADE_ID) + "] 入参[" + GSON_INSTANCE.toJson(dubboGenericRequestBO) + "]\n>>>\n");
            sendMessage(WebSocketMessageType.JSON, jsonString);
        } catch (Throwable e) {
            log.warn("Invoke Error：", e);
            sendMessage("调用失败：入参[" + GSON_INSTANCE.toJson(dubboGenericRequestBO) + "]\n Msg：" + e.getMessage());
        } finally {
            if (null != reference) {
                reference.destroy();
            }
            sendMessage("执行完成");
            MDC.remove(MDC_TRADE_ID);
        }
    }

    private void sendMessage(String message) {
        sendMessage(WebSocketMessageType.TEXT, message);
    }

    private void sendMessage(WebSocketMessageType type, String message) {
        applicationEventPublisher.publishEvent(new WebSocketSendMessageEvent(dubboGenericRequestBO.getToken(), dubboGenericRequestBO.getEcho(), type, message));
    }

    private void putTrace() {
        long currentTime = System.currentTimeMillis();
        long timeStamp = currentTime % 1000000L;
        int randomNumber = ThreadLocalRandom.current().nextInt(1000000);
        String traceId = "TB" + Long.toHexString(timeStamp * 10000 + randomNumber).toUpperCase();
        RpcContext.getContext().setAttachment(MDC_TRADE_ID, traceId);
        MDC.put(MDC_TRADE_ID, traceId);
    }
}