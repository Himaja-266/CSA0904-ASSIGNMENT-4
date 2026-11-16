CREATE DATABASE IF NOT EXISTS event_registration;
USE event_registration;

CREATE TABLE IF NOT EXISTS students (
    student_id BIGINT NOT NULL PRIMARY KEY,
    student_name VARCHAR(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS events (
    event_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    event_name VARCHAR(160) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS registrations (
    registration_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    registered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_student_event UNIQUE (student_id, event_id),
    CONSTRAINT fk_registration_student FOREIGN KEY (student_id) REFERENCES students(student_id),
    CONSTRAINT fk_registration_event FOREIGN KEY (event_id) REFERENCES events(event_id)
);

INSERT INTO students (student_id, student_name) VALUES
    (1001, 'Avery Patel'),
    (1002, 'Jordan Kim')
ON DUPLICATE KEY UPDATE student_name = VALUES(student_name);

INSERT INTO events (event_id, event_name) VALUES
    (1, 'Java Conference'),
    (2, 'Data Science Workshop'),
    (3, 'Design Symposium')
ON DUPLICATE KEY UPDATE event_name = VALUES(event_name);

INSERT INTO registrations (student_id, event_id) VALUES
    (1001, 1),
    (1002, 2)
ON DUPLICATE KEY UPDATE student_id = VALUES(student_id);
