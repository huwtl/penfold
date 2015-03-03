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