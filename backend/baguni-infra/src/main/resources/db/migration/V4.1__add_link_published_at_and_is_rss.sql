ALTER TABLE baguni_db.link
    ADD COLUMN is_rss       BOOLEAN NOT NULL DEFAULT 0,
    ADD COLUMN published_at TIMESTAMP;