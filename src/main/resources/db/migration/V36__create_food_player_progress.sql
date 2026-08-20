CREATE TABLE food_player_progress (
                                      id BIGSERIAL PRIMARY KEY,

                                      player_id VARCHAR(255) NOT NULL,

                                      category VARCHAR(50) NOT NULL,

                                      highest_unlocked_level INTEGER NOT NULL DEFAULT 1,

                                      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                      CONSTRAINT uk_food_player_category
                                          UNIQUE (player_id, category)
);