ALTER TABLE game_modes
    ADD COLUMN arena_type VARCHAR(50);

ALTER TABLE game_modes
    ADD COLUMN difficulty VARCHAR(50);

UPDATE game_modes
SET arena_type = 'LOGO',
    difficulty = 'EASY'
WHERE arena_type IS NULL;

ALTER TABLE game_modes
    ALTER COLUMN arena_type SET NOT NULL;

ALTER TABLE game_modes
    ALTER COLUMN difficulty SET NOT NULL;