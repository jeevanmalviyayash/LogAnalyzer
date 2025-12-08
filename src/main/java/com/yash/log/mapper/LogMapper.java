package com.yash.log.mapper;

import com.yash.log.dto.LogDTO;
import com.yash.log.entity.Log;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface LogMapper {

    LogDTO toDto(Log entity);
    Log toEntity(LogDTO dto);
}