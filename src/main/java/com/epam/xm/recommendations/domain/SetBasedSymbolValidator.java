package com.epam.xm.recommendations.domain;

import java.util.Collection;
import java.util.Set;

public record SetBasedSymbolValidator(Set<String> supportedSymbols) implements SymbolValidator {

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
