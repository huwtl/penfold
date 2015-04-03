CREATE TABLE tasks
(
  id varchar(36) PRIMARY KEY,
  data json NOT NULL
);

CREATE UNIQUE INDEX tasks_id_idx ON tasks((data->>'id'));
CREATE INDEX tasks_status_idx ON tasks((data->>'status'));
CREATE INDEX tasks_queue_idx ON tasks((data->>'queue'));
CREATE INDEX tasks_created_idx ON tasks (cast(data->>'created' AS BIGINT));
CREATE INDEX tasks_attempts_idx ON tasks (cast(data->>'attempts' AS BIGINT));
CREATE INDEX tasks_status_modified_idx ON tasks (cast(data->>'statusLastModified' AS BIGINT));
CREATE INDEX tasks_close_result_idx ON tasks((data->>'closeResultType'));
CREATE INDEX tasks_trigger_idx ON tasks (cast(data->>'triggerDate' AS BIGINT));
CREATE INDEX tasks_sort_id_idx ON tasks (cast(data->>'sort' AS BIGINT), id);
CREATE INDEX tasks_sort_id_asc_desc_idx ON tasks (cast(data->>'sort' AS BIGINT), id DESC);

CREATE TABLE archived
(
  id varchar(36) PRIMARY KEY,
  data json NOT NULL
);