package com.epam.xm.recommendations.domain;

import java.util.Collection;
import java.util.Set;

/**
 * A simple {@link SymbolValidator} backed by an immutable {@link Set}.
 *
 * <p>The immutable set guarantees thread-safety and constant-time membership checks, which is
 * sufficient for a small, fixed list of supported symbols configured at startup.
 */
public record SetBasedSymbolValidator(Set<String> supportedSymbols) implements SymbolValidator {

    /**
     * Creates a validator from an arbitrary collection by defensively copying it into an immutable
     * set.
     *
     * @param supportedSymbols source collection of supported tickers
     */
    public SetBasedSymbolValidator(Collection<String> supportedSymbols) {
        this(Set.copyOf(supportedSymbols));
    }

    @Override
    public boolean isSupported(String symbol) {
        return supportedSymbols.contains(symbol);
    }

    @Override
    public Collection<String> getSupportedSymbols() {
        return supportedSymbols;
    }
}
