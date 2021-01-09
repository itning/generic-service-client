package top.itning.generic.service.core.service.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ApplicationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.itning.generic.service.core.controller.ProgressWebSocket;
import top.itning.generic.service.core.service.DubboGenericInvokeTask;
import top.itning.generic.service.core.service.DubboGenericService;
import top.itning.generic.service.core.bo.DubboGenericRequestBO;

import java.util.concurrent.*;

/**
 * dubbo 泛化调用实现
 *
 * @author itning
 * @see org.apache.dubbo.common.utils.CompatibleTypeUtils 转换规则
 * @since 2020/10/19 15:24
 */
@Slf4j
@Service
public class DubboGenericServiceImpl implements DubboGenericService {

    private static final ExecutorService EXECUTOR_SERVICE;

    static {
        int availableProcessors = Runtime.getRuntime().availableProcessors() * 2;
        EXECUTOR_SERVICE = new ThreadPoolExecutor(availableProcessors, availableProcessors,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(100),
                new ThreadFactoryBuilder().setNameFormat("dubbo-generic-thread-%d").build(),
                new ThreadPoolExecutor.AbortPolicy());
        Runtime.getRuntime().addShutdownHook(new Thread(EXECUTOR_SERVICE::shutdown));
    }

    private final ApplicationConfig applicationConfig;

    @Autowired
    public DubboGenericServiceImpl(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @Override
    public void invoke(DubboGenericRequestBO dubboGenericRequestBO) {
        try {
            EXECUTOR_SERVICE.submit(new DubboGenericInvokeTask(applicationConfig, dubboGenericRequestBO));
        } catch (RejectedExecutionException e) {
            log.warn("Thread Pool Is Full", e);
            ProgressWebSocket.sendMessage(dubboGenericRequestBO.getToken(), dubboGenericRequestBO.getEcho(), "线程池满了，请稍后再试！");
        }
    }
}
