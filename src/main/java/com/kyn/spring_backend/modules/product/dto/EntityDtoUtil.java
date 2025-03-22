package com.kyn.spring_backend.modules.product.dto;

import org.springframework.beans.BeanUtils;

import com.kyn.spring_backend.modules.product.entity.ProductBasEntity;

public class EntityDtoUtil {

    public static ProductBasDto entityToDto(ProductBasEntity entity) {
        ProductBasDto dto = new ProductBasDto();
        BeanUtils.copyProperties(entity, dto);
        dto.set_id(entity.get_id().toString());
        dto.setRegrId(entity.getRegrId());
        dto.setRegDt(entity.getRegDt());
        dto.setAmdrId(entity.getAmdrId());
        dto.setAmdDt(entity.getAmdDt());
        return dto;
    }

    public static ProductBasEntity dtoToEntity(ProductBasDto dto) {
        ProductBasEntity entity = new ProductBasEntity();
        BeanUtils.copyProperties(dto, entity);

        return entity;
    }
}
