CREATE TABLE events
(
  id SERIAL,
  type VARCHAR(64) NOT NULL,
  aggregate_id VARCHAR(64) NOT NULL,
  aggregate_version INT NOT NULL,
  aggregate_type VARCHAR(64) NOT NULL,
  created TIMESTAMP NOT NULL,
  data TEXT,
  PRIMARY KEY (id),
  UNIQUE (aggregate_id, aggregate_version)
);

CREATE INDEX events_created_idx ON events (created);
CREATE INDEX events_type_idx ON events (type);
CREATE INDEX events_agg_id_idx ON events (aggregate_id);
CREATE INDEX events_agg_type_idx ON events (aggregate_type);