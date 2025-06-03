-- V2__Create_Refresh_Tokens_Table.sql

CREATE TABLE refresh_tokens (
                                id BIGINT IDENTITY(1,1) PRIMARY KEY, -- Для SQL Server IDENTITY(1,1)
                                token NVARCHAR(255) NOT NULL UNIQUE,
                                expiry_date DATETIME2 NOT NULL, -- DATETIME2 для точного часу
                                user_id BIGINT NOT NULL,

                                CONSTRAINT fk_user_refresh_token
                                    FOREIGN KEY (user_id)
                                        REFERENCES users(id) -- Посилання на таблицю users, переконайтеся, що назва таблиці правильна (може бути "user" або "users")
                                        ON DELETE CASCADE -- Видалення refresh-токена, якщо користувач видаляється
);