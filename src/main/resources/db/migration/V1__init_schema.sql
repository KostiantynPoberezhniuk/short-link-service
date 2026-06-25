CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL
);

CREATE TABLE short_links (
    id           BIGSERIAL PRIMARY KEY,
    short_code   VARCHAR(8)    NOT NULL UNIQUE,
    original_url VARCHAR(2048) NOT NULL,
    created_at   TIMESTAMPTZ   NOT NULL,
    expires_at   TIMESTAMPTZ   NOT NULL,
    visit_count  BIGINT        NOT NULL DEFAULT 0,
    user_id      BIGINT        NOT NULL REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_short_links_user_id ON short_links (user_id);
CREATE INDEX idx_short_links_short_code ON short_links (short_code);
