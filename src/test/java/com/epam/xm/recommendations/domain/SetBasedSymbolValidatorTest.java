package com.epam.xm.recommendations.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SetBasedSymbolValidatorTest {

    @Test
    void shouldValidateCorrectly() {
        SetBasedSymbolValidator validator = new SetBasedSymbolValidator(Set.of("BTC", "ETH"));

        assertTrue(validator.isSupported("BTC"));
        assertTrue(validator.isSupported("ETH"));
        assertFalse(validator.isSupported("DOGE"));
    }

    @Test
    void shouldConstructFromCollection() {
        SetBasedSymbolValidator validator = new SetBasedSymbolValidator(List.of("BTC", "XRP"));
        assertTrue(validator.isSupported("BTC"));
        assertTrue(validator.isSupported("XRP"));
        assertEquals(2, validator.getSupportedSymbols().size());
    }

    @Test
    void shouldReturnSupportedSymbols() {
        Set<String> symbols = Set.of("BTC", "ETH");
        SetBasedSymbolValidator validator = new SetBasedSymbolValidator(symbols);
        assertEquals(symbols, validator.getSupportedSymbols());
    }
}
