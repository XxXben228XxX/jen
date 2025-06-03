-- V3__add_email_to_users.sql
ALTER TABLE users
    ADD email VARCHAR(50) NOT NULL UNIQUE;
-- Або NVARCHAR(50), якщо ви хочете Unicode
-- ALTER TABLE users
-- ADD email NVARCHAR(50) NOT NULL UNIQUE;