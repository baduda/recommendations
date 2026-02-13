package com.epam.xm.recommendations.application.mapper;

import com.epam.xm.recommendations.application.dto.CryptoRangeDto;
import com.epam.xm.recommendations.application.dto.CryptoStatsDto;
import com.epam.xm.recommendations.domain.CryptoStats;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CryptoMapper {
    CryptoStatsDto toDto(CryptoStats stats);
    CryptoRangeDto toRangeDto(CryptoStats stats);
}
