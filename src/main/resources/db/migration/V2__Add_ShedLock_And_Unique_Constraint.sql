ALTER TABLE crypto_prices ADD CONSTRAINT uq_crypto_prices_symbol_timestamp UNIQUE (symbol, price_timestamp);

CREATE TABLE shedlock (
    name VARCHAR(64) NOT NULL,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);
