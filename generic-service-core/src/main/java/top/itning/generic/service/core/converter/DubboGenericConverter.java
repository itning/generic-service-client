package top.itning.generic.service.core.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import top.itning.generic.service.core.dto.DubboGenericRequestDTO;
import top.itning.generic.service.core.bo.DubboGenericRequestBO;

/**
 * @author itning
 * @since 2020/10/19 16:16
 */
@Mapper
public interface DubboGenericConverter {

    DubboGenericConverter INSTANCE = Mappers.getMapper(DubboGenericConverter.class);

    @Mappings({})
    DubboGenericRequestBO toBO(DubboGenericRequestDTO dto);
}
