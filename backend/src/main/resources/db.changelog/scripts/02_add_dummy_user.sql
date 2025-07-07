-- liquibase formatted sql
-- changeset arek_bednarz:dummy_user failOnError:true

INSERT INTO users (name, email, password, role)
VALUES ('NoobUser', 'noobuser@email.com', '$2a$10$ONuFi8rhjcHQJB1XBIHrS.i20FpoUPqvoO8aee7M0Nq0ww2LfZhqq', 'USER');

