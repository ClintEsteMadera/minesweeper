CREATE TABLE games (
	id uuid NOT NULL,
	username varchar(255) NOT NULL,
	rows_count int4 NOT NULL,
	columns_count int4 NOT NULL,
	mines_count int4 NOT NULL,
	outcome varchar(255),
	created timestamp without time zone NOT NULL,
	modified timestamp without time zone,
	cells jsonb NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE users (
	id uuid NOT NULL,
	username varchar(255) NOT NULL,
	PRIMARY KEY (id)
);

INSERT INTO users (id, username) VALUES('82d79ab0-d586-11e8-9bc3-0242ac130002', 'jchiocchio@gmail.com');
