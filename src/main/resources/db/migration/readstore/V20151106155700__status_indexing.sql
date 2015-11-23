CREATE INDEX tasks_status_trigger_idx ON tasks((data->>'status'), (cast(data->>'triggerDate' AS BIGINT)));
CREATE INDEX tasks_status_status_modified_idx ON tasks((data->>'status'), (cast(data->>'statusLastModified' AS BIGINT)));
CREATE INDEX tasks_queue_status_sort_id_idx ON tasks ((data->>'queue'), (data->>'status'), cast(data->>'sort' AS BIGINT), id);
DROP INDEX tasks_status_idx;
DROP INDEX tasks_sort_id_idx;
DROP INDEX tasks_queue_idx;
