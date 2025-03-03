ALTER TABLE baguni_db.link
    ADD COLUMN created_at TIMESTAMP NOT NULL, # defaults to 0
    ADD COLUMN updated_at TIMESTAMP NOT NULL;