CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255),
    phone_number VARCHAR(255)
);

CREATE TABLE user_categories (
    user_id BIGINT NOT NULL,
    subscribed_categories VARCHAR(255) NOT NULL,
    CONSTRAINT fk_user_categories_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE user_channels (
    user_id BIGINT NOT NULL,
    channels VARCHAR(255) NOT NULL,
    CONSTRAINT fk_user_channels_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE notification_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message VARCHAR(1000),
    category VARCHAR(255),
    channel VARCHAR(255),
    user_email VARCHAR(255),
    user_name VARCHAR(255),
    timestamp TIMESTAMP
);
