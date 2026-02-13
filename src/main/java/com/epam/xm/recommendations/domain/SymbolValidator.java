package com.epam.xm.recommendations.domain;

import java.util.Collection;

public interface SymbolValidator {
    boolean isSupported(String symbol);
    Collection<String> getSupportedSymbols();
}
