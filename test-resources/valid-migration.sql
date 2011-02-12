
-- create user table
CREATE TABLE user (login VARCHAR(255) NOT NULL PRIMARY KEY);

-- add initial user
INSERT INTO user (login) VALUES ('admin');

-- add passwd column
ALTER TABLE user ADD passwd VARCHAR(255);
UPDATE user SET passwd='secret';
ALTER TABLE user ALTER COLUMN passwd VARCHAR(255) NOT NULL;

