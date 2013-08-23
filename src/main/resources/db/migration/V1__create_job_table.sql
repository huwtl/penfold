CREATE TABLE jobs
(
  id VARCHAR(32) NOT NULL,
  job_type VARCHAR(64) NOT NULL,
  status VARCHAR(64) NOT NULL,
  cron VARCHAR(64),
  trigger_date DATETIME,
  payload TEXT,
  PRIMARY KEY (id)
);

CREATE INDEX jobs_trigger_date_idx ON jobs (trigger_date);