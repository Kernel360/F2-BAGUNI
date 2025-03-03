ALTER TABLE baguni_db.user
    ADD id_token char(36);

UPDATE baguni_db.user
SET id_token = uuid();

ALTER TABLE baguni_db.user
    MODIFY id_token char(36) NOT NULL;

ALTER TABLE baguni_db.user
    ADD CONSTRAINT UKhsxhy38v8pbjsyvvql9y962ie UNIQUE (id_token);