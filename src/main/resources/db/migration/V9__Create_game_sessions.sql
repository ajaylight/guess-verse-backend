CREATE TABLE game_sessions(

                              id UUID PRIMARY KEY,

                              room_id UUID NOT NULL,

                              arena VARCHAR(30),

                              difficulty VARCHAR(30),

                              state VARCHAR(30),

                              total_questions INT,

                              current_question INT,

                              started_at TIMESTAMP,

                              ended_at TIMESTAMP

);