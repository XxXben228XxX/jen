-- V2__add_event_type_and_location_to_event.sql

ALTER TABLE event
    ADD event_type NVARCHAR(255);

ALTER TABLE event
    ADD location NVARCHAR(255);