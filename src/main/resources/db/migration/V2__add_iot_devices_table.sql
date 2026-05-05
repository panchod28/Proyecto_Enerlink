CREATE TABLE IF NOT EXISTS iot_device (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(255),
    device_name VARCHAR(255),
    device_type VARCHAR(50),
    current_reading DOUBLE PRECISION,
    unit VARCHAR(50),
    location VARCHAR(255),
    status VARCHAR(50),
    timestamp BIGINT,
    user_id BIGINT,
    CONSTRAINT fk_iot_device_user FOREIGN KEY (user_id) REFERENCES users(id)
);
