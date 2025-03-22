package com.kyn.spring_backend.modules.user.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;

import com.kyn.spring_backend.modules.user.entity.UserAuthEntity;
import com.kyn.spring_backend.modules.user.entity.UserInfoEntity;

public class EntityDtoUtil {

    // UserInfoEntity <-> UserInfoDto 변환
    public static UserInfoDto entityToDto(UserInfoEntity entity, List<UserAuthEntity> auths) {
        UserInfoDto dto = new UserInfoDto();
        BeanUtils.copyProperties(entity, dto);
        dto.setId(entity.get_id().toString());

        if (auths != null && !auths.isEmpty()) {
            List<UserAuthDto> authDtos = auths.stream()
                    .map(EntityDtoUtil::authEntityToDto)
                    .collect(Collectors.toList());
            dto.setUserAuths(authDtos);
        }

        return dto;
    }

    public static UserInfoEntity dtoToEntity(UserInfoDto dto) {
        UserInfoEntity entity = new UserInfoEntity();
        BeanUtils.copyProperties(dto, entity);

        if (dto.getId() != null && !dto.getId().isEmpty()) {
            entity.set_id(new ObjectId(dto.getId()));
        }

        return entity;
    }

    // UserAuthEntity <-> UserAuthDto 변환
    public static UserAuthDto authEntityToDto(UserAuthEntity entity) {
        UserAuthDto dto = new UserAuthDto();
        BeanUtils.copyProperties(entity, dto);
        dto.setId(entity.get_id().toString());
        dto.setUserObjectId(entity.getUserObjectId().toString());

        return dto;
    }

    public static UserAuthEntity authDtoToEntity(UserAuthDto dto) {
        UserAuthEntity entity = new UserAuthEntity();
        BeanUtils.copyProperties(dto, entity);

        if (dto.getId() != null && !dto.getId().isEmpty()) {
            entity.set_id(new ObjectId(dto.getId()));
        }

        if (dto.getUserObjectId() != null && !dto.getUserObjectId().isEmpty()) {
            entity.setUserObjectId(new ObjectId(dto.getUserObjectId()));
        }

        return entity;
    }
}