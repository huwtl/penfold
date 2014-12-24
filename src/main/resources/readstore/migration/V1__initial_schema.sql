CREATE TABLE tasks
(
  data json NOT NULL
);

CREATE UNIQUE INDEX tasks_id_idx ON tasks ((data->>'id'));
