CREATE TABLE tasks
(
  id varchar(36) PRIMARY KEY,
  data json NOT NULL
);

CREATE UNIQUE INDEX tasks_id_idx ON tasks ((data->>'id'));

CREATE TABLE trackers
(
  id varchar PRIMARY KEY,
  last_event_id bigint NOT NULL
);

CREATE OR REPLACE FUNCTION json_intext(text) RETURNS json AS $$
SELECT json_in($1::cstring);
$$ LANGUAGE SQL IMMUTABLE;

CREATE CAST (text AS json) WITH FUNCTION json_intext(text) AS IMPLICIT;