-- liquibase formatted sql
-- changeset arek_bednarz:initial_tables failOnError:true

CREATE TABLE users (
   id SERIAL PRIMARY KEY,
   name VARCHAR(100) NOT NULL,
   email VARCHAR(100) UNIQUE NOT NULL,
   password VARCHAR(255) NOT NULL,
   role CHAR(5) NOT NULL
);

CREATE TABLE token (
   id SERIAL PRIMARY KEY ,
   value VARCHAR(255) NOT NULL,
   type VARCHAR(50),
   revoked BOOLEAN,
   expired BOOLEAN,
   user_id BIGINT REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE movies (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    genre VARCHAR(100),
    available BOOLEAN DEFAULT TRUE
);

CREATE TABLE rentals (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    movie_id INTEGER NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    rented_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP NOT NULL,
    returned_at TIMESTAMP
);

INSERT INTO users (name, email, password, role)
VALUES ('Admin', 'admin@email.com', '$2a$10$ONuFi8rhjcHQJB1XBIHrS.i20FpoUPqvoO8aee7M0Nq0ww2LfZhqq', 'ADMIN');

CREATE INDEX idx_rentals_user_id ON rentals(user_id);
CREATE INDEX idx_rentals_returned_at ON rentals(returned_at);
CREATE INDEX idx_user_returned ON rentals(user_id, returned_at);

