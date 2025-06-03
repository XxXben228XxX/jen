-- V1__initial_schema.sql

-- Таблиця для місць проведення (Venue)
CREATE TABLE venue (
                       id BIGINT PRIMARY KEY IDENTITY(1,1),
                       name NVARCHAR(255),
                       address NVARCHAR(255)
);

-- Таблиця для подій (Event)
CREATE TABLE event (
                       id NVARCHAR(255) PRIMARY KEY,
                       name NVARCHAR(255),
                       description NVARCHAR(MAX),
                       event_date DATETIME2,
                       venue_id BIGINT,
                       created_date DATETIME2,
                       last_modified_date DATETIME2,
                       created_by NVARCHAR(255),
                       last_modified_by NVARCHAR(255),
                       FOREIGN KEY (venue_id) REFERENCES venue(id)
);

-- Таблиця для користувачів (Users)
CREATE TABLE users (
                       id BIGINT PRIMARY KEY IDENTITY(1,1),
                       username NVARCHAR(255) NOT NULL UNIQUE,
                       password NVARCHAR(255) NOT NULL
);

-- Таблиця для ролей (Roles)
CREATE TABLE roles (
                       id BIGINT PRIMARY KEY IDENTITY(1,1),
                       name NVARCHAR(255) NOT NULL UNIQUE
);

-- Таблиця для зв'язку користувачів та ролей (User-Roles Many-to-Many)
CREATE TABLE [user_roles] (
                              user_id BIGINT NOT NULL,
                              role_id BIGINT NOT NULL,
                              PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
    );