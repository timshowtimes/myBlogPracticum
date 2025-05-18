create table if not exists post(
    id serial primary key,
    title varchar(255) not null,
    tags TEXT,
    text TEXT,
    image BYTEA,
    created_at TIMESTAMP DEFAULT NOW()
);
