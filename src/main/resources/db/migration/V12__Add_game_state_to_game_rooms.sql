ALTER TABLE game_rooms
    ADD COLUMN game_state VARCHAR(30);

UPDATE game_rooms
SET game_state = 'WAITING'
WHERE game_state IS NULL;

ALTER TABLE game_rooms
    ALTER COLUMN game_state SET NOT NULL;