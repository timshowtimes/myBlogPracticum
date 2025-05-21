CREATE TABLE if NOT EXISTS post
(
    id          serial primary key,
    title       varchar(255)        not null,
    tags        TEXT,
    text        TEXT,
    image       BYTEA,
    created_at  TIMESTAMP DEFAULT NOW(),
    likes_count integer   default 0 not null
);

CREATE TABLE if NOT EXISTS comment
(
    id         SERIAL PRIMARY KEY,
    text       TEXT    NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    post_id    INTEGER NOT NULL,
    CONSTRAINT fk_post
        FOREIGN KEY (post_id)
            REFERENCES post (id)
            ON DELETE CASCADE
);