CREATE TABLE IF NOT EXISTS users (
                                     id         INT AUTO_INCREMENT PRIMARY KEY,
                                     username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(10)  NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    CHECK (role IN ('USER', 'ADMIN'))
    );

CREATE TABLE IF NOT EXISTS rooms (
                                     id           INT AUTO_INCREMENT PRIMARY KEY,
                                     name         VARCHAR(100) NOT NULL,
    code         VARCHAR(8)   UNIQUE,
    owner_id     INT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_private   BOOLEAN      NOT NULL DEFAULT FALSE,
    canvas_w     INT          NOT NULL DEFAULT 100,
    canvas_h     INT          NOT NULL DEFAULT 100,
    canvas_state BINARY VARYING,
    expires_at   TIMESTAMP    NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    CHECK (canvas_w > 0 AND canvas_w <= 500),
    CHECK (canvas_h > 0 AND canvas_h <= 500)
    );

CREATE TABLE IF NOT EXISTS saved_works (
                                           id         INT AUTO_INCREMENT PRIMARY KEY,
                                           owner_id   INT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title      VARCHAR(100) NOT NULL,
    is_public  BOOLEAN      NOT NULL DEFAULT FALSE,
    image_data BINARY VARYING NOT NULL,
    canvas_w   INT          NOT NULL,
    canvas_h   INT          NOT NULL,
    saved_at   TIMESTAMP    NOT NULL DEFAULT NOW()
    );