-- V3__Alter_Refresh_Tokens_Expiry_Date_Column.sql

ALTER TABLE refresh_tokens
ALTER COLUMN expiry_date DATETIMEOFFSET NOT NULL;