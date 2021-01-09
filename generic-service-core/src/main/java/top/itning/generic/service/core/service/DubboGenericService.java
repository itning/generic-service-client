package top.itning.generic.service.core.service;


import top.itning.generic.service.core.bo.DubboGenericRequestBO;

/**
 * Dubbo泛化调用服务
 *
 * @author itning
 * @since 2020/10/19 15:22
 */
public interface DubboGenericService {
    /**
     * 执行泛化调用
     *
     * @param dubboGenericRequestBO 泛化调用请求参数
     */
    void invoke(DubboGenericRequestBO dubboGenericRequestBO);
}
