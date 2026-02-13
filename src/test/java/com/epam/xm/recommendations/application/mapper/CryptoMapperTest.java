package com.epam.xm.recommendations.application.mapper;

import com.epam.xm.recommendations.application.dto.CryptoRangeDto;
import com.epam.xm.recommendations.application.dto.CryptoStatsDto;
import com.epam.xm.recommendations.domain.CryptoStats;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CryptoMapperTest {

    private final CryptoMapper mapper = Mappers.getMapper(CryptoMapper.class);

    @Test
    void shouldMapToDto() {
        CryptoStats stats = new CryptoStats(
                "BTC",
                new BigDecimal("40000"),
                new BigDecimal("42000"),
                new BigDecimal("38000"),
                new BigDecimal("45000"),
                new BigDecimal("0.1842")
        );

        CryptoStatsDto dto = mapper.toDto(stats);

        assertEquals(stats.symbol(), dto.symbol());
        assertEquals(stats.oldestPrice(), dto.oldestPrice());
        assertEquals(stats.newestPrice(), dto.newestPrice());
        assertEquals(stats.minPrice(), dto.minPrice());
        assertEquals(stats.maxPrice(), dto.maxPrice());
        assertEquals(stats.normalizedRange(), dto.normalizedRange());
    }

    @Test
    void shouldReturnNullWhenMappingNull() {
        assertEquals(null, mapper.toDto(null));
        assertEquals(null, mapper.toRangeDto(null));
    }

    @Test
    void shouldMapToRangeDto() {
        CryptoStats stats = new CryptoStats(
                "BTC",
                new BigDecimal("40000"),
                new BigDecimal("42000"),
                new BigDecimal("38000"),
                new BigDecimal("45000"),
                new BigDecimal("0.1842")
        );

        CryptoRangeDto dto = mapper.toRangeDto(stats);

        assertEquals(stats.symbol(), dto.symbol());
        assertEquals(stats.normalizedRange(), dto.normalizedRange());
    }
}
