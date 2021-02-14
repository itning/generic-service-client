package top.itning.generic.service.core.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.itning.generic.service.common.model.RestModel;
import top.itning.generic.service.common.websocket.event.WebSocketReceiveMessageEvent;
import top.itning.generic.service.core.bo.DubboGenericRequestBO;
import top.itning.generic.service.core.converter.DubboGenericConverter;
import top.itning.generic.service.core.dto.DubboGenericRequestDTO;
import top.itning.generic.service.core.service.DubboGenericService;

import javax.annotation.Nonnull;

import static top.itning.generic.service.common.util.JsonUtils.GSON_INSTANCE;

/**
 * dubbo相关操作
 *
 * @author itning
 * @since 2020/10/19 16:21
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/dubbo")
public class DubboGenericInvokeController implements ApplicationListener<WebSocketReceiveMessageEvent> {

    private final DubboGenericService dubboGenericService;

    @Autowired
    public DubboGenericInvokeController(DubboGenericService dubboGenericService) {
        this.dubboGenericService = dubboGenericService;
    }

    /**
     * 泛化调用执行
     *
     * @return {@link DubboGenericRequestDTO#toString()}
     */
    @PostMapping("/invoke")
    public ResponseEntity<?> invokeMethod(@RequestBody @Validated DubboGenericRequestDTO requestParams) {

        DubboGenericRequestBO dubboGenericRequestBO = DubboGenericConverter.INSTANCE.toBO(requestParams);
        try {
            return ResponseEntity.ok(RestModel.success(dubboGenericService.invokeSynchronize(dubboGenericRequestBO)));
        } catch (Throwable e) {
            log.info("Invoke Catch Exception", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestModel.failed(e.getMessage()));
        }
    }

    @Override
    public void onApplicationEvent(@Nonnull WebSocketReceiveMessageEvent event) {
        DubboGenericRequestBO dubboGenericRequestBO = GSON_INSTANCE.fromJson(event.getReceiveMessage(), DubboGenericRequestBO.class);
        dubboGenericService.invoke(dubboGenericRequestBO);
    }
}
