package com.epam.xm.recommendations.domain;

import java.util.Collection;
import java.util.Set;

public class SetBasedSymbolValidator implements SymbolValidator {
    private final Set<String> supportedSymbols;

    public SetBasedSymbolValidator(Collection<String> supportedSymbols) {
        this.supportedSymbols = Set.copyOf(supportedSymbols);
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
