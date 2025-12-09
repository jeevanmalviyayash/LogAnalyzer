package com.yash.log.mapper;

import com.yash.log.dto.TicketDTO;
import com.yash.log.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring", uses = {LogMapper.class})
public interface TicketMapper {

    Ticket toEntity(TicketDTO dto);

    @Mapping(source = "ticketId", target = "ticketId")
    TicketDTO toDto(Ticket entity);

    @Mapping(target = "ticketId", ignore = true)
    Ticket updateEntityFromDto(TicketDTO dto, @MappingTarget Ticket entity);
}

