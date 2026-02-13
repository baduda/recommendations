CREATE TABLE crypto_prices (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(10) NOT NULL,
    price NUMERIC(20, 8) NOT NULL CHECK (price > 0),
    price_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_crypto_prices_symbol_timestamp 
ON crypto_prices (symbol, price_timestamp DESC);
