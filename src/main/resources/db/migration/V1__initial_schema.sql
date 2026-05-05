-- Initial schema migration for Enerlink
-- Tables created in dependency order: users -> energy_offer -> transactions

-- Table: users
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    rol VARCHAR(255) NOT NULL
);

-- Table: energy_offer
CREATE TABLE IF NOT EXISTS energy_offer (
    id BIGSERIAL PRIMARY KEY,
    producer_id BIGINT,
    kwh DOUBLE PRECISION,
    price DOUBLE PRECISION,
    available BOOLEAN DEFAULT TRUE,
    sale_type VARCHAR(50)
);

-- Table: transactions
CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    offer_id BIGINT,
    buyer_id BIGINT,
    seller_id BIGINT,
    kwh DOUBLE PRECISION,
    price DOUBLE PRECISION,
    timestamp TIMESTAMP,
    CONSTRAINT fk_transaction_offer FOREIGN KEY (offer_id) REFERENCES energy_offer(id),
    CONSTRAINT fk_transaction_buyer FOREIGN KEY (buyer_id) REFERENCES users(id),
    CONSTRAINT fk_transaction_seller FOREIGN KEY (seller_id) REFERENCES users(id)
);
