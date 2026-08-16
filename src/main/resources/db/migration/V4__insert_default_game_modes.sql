INSERT INTO game_modes
(
    name,
    description,
    round_time_seconds,
    max_players,
    hints_enabled,
    active
)
VALUES
    (
        'Classic',
        'Standard GuessVerse experience',
        60,
        8,
        TRUE,
        TRUE
    ),
    (
        'Blitz',
        'Fast-paced gameplay',
        30,
        8,
        TRUE,
        TRUE
    ),
    (
        'Hardcore',
        'No hints allowed',
        45,
        8,
        FALSE,
        TRUE
    ),
    (
        'Practice',
        'Single-player practice mode',
        60,
        1,
        TRUE,
        TRUE
    );