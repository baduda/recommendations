package com.epam.xm.recommendations.application.mapper;

import com.epam.xm.recommendations.application.dto.CryptoRangeDto;
import com.epam.xm.recommendations.application.dto.CryptoStatsDto;
import com.epam.xm.recommendations.domain.CryptoStats;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
/**
 * MapStruct mapper translating domain objects to API DTOs.
 *
 * <p>Mapping is kept trivial (field-to-field) to preserve domain-calculated values such as
 * normalized range without re-computation on the edges.
 */
public interface CryptoMapper {
    /**
     * Converts full stats to DTO for /stats endpoint.
     *
     * @param stats domain stats
     * @return API DTO
     */
    CryptoStatsDto toDto(CryptoStats stats);

    /**
     * Converts stats to a compact volatility DTO used for range-sorted endpoints.
     *
     * @param stats domain stats
     * @return API DTO with symbol and normalized range
     */
    CryptoRangeDto toRangeDto(CryptoStats stats);
}
