# sql-migrate

Sql-migrate is a simple database migration tool for clojure. Migrations are
described in an SQL file, where each comment is the name of a migration, and 
following lines before next comment is the migration script. Scripts are run
in order, excluding those which have been executed before. Names of executed
scripts are saved in a table called migrations.

## Usage

To install with leiningen, add following dependency to your project.clj:

	[sql-migrate <version>]

Create a migrations table:

	CREATE TABLE migrations (name VARCHAR(255) NOT NULL PRIMARY KEY);

Describe your migrations in an sql file:

	-- create user table
	CREATE TABLE user (login VARCHAR(255) NOT NULL PRIMARY KEY);
	
	-- add initial user
	INSERT INTO user (login) VALUES ('admin');
	
	-- add passwd column
	ALTER TABLE user ADD passwd VARCHAR(255);
	UPDATE user SET passwd='secret';
	ALTER TABLE user ALTER COLUMN passwd VARCHAR(255) NOT NULL;

Execute the migrations:

	(sql-migrate.core/migrate "migrations.sql")

## License

Copyright (C) 2011 Santtu Lintervo

Distributed under the Eclipse Public License, the same as Clojure.
