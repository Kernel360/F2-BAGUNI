CREATE TABLE baguni_db.link_stats
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    date             DATE          NOT NULL,
    url              VARCHAR(2048) NOT NULL,
    view_count       BIGINT        NOT NULL DEFAULT 0,
    bookmarked_count BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT UC_DATE_URL UNIQUE (date, url(200))
);