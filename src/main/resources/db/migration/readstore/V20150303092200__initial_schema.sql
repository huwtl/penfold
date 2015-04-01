CREATE TABLE tasks
(
  id varchar(36) PRIMARY KEY,
  data json NOT NULL
);

CREATE UNIQUE INDEX tasks_id_idx ON tasks((data->>'id'));
CREATE INDEX tasks_version_idx ON tasks(cast(data->>'version' AS BIGINT));
CREATE INDEX tasks_status_idx ON tasks((data->>'status'));
CREATE INDEX tasks_queue_idx ON tasks((data->>'queue'));
CREATE INDEX tasks_assignee_idx ON tasks((data->>'assignee'));
CREATE INDEX tasks_created_idx ON tasks (cast(data->>'created' AS BIGINT));
CREATE INDEX tasks_attempts_idx ON tasks (cast(data->>'attempts' AS BIGINT));
CREATE INDEX tasks_status_modified_idx ON tasks (cast(data->>'statusLastModified' AS BIGINT));
CREATE INDEX tasks_trigger_idx ON tasks (cast(data->>'triggerDate' AS BIGINT));
CREATE INDEX tasks_score_idx ON tasks (cast(data->>'score' AS BIGINT));
CREATE INDEX tasks_sort_idx ON tasks (cast(data->>'sort' AS BIGINT));

CREATE TABLE archived
(
  id varchar(36) PRIMARY KEY,
  data json NOT NULL
);