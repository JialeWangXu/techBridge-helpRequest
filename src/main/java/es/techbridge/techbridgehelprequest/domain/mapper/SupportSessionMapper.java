package es.techbridge.techbridgehelprequest.domain.mapper;

import es.techbridge.techbridgehelprequest.domain.model.supportsession.SupportSession;
import es.techbridge.techbridgehelprequest.infrastructure.postgresql.entities.SupportSessionEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SupportSessionMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "helpRequest", ignore = true)
    void updateEntityFromDto(SupportSession dto, @MappingTarget SupportSessionEntity entity);
}
