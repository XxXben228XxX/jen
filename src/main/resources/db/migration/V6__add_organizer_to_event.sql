-- V1__add_organizer_to_event.sql (або V2__add_organizer_to_event.sql)

ALTER TABLE event
    ADD organizer NVARCHAR(255); -- Використовуй NVARCHAR для рядків у SQL Server