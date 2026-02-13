package com.epam.xm.recommendations.domain;

import java.util.Collection;

/**
 * Defines the contract for validating whether a crypto symbol is supported by the system.
 * Implementations may rely on in-memory sets, configuration, or persistence.
 */
public interface SymbolValidator {
    /**
     * Checks if a given symbol is supported.
     *
     * @param symbol the coin ticker to validate
     * @return {@code true} when symbol is supported; {@code false} otherwise
     */
    boolean isSupported(String symbol);

    /**
     * Returns the immutable collection of supported symbols.
     *
     * @return collection of supported tickers
     */
    Collection<String> getSupportedSymbols();
}
