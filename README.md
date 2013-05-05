### Overview

WORK IN PROGRESS

Event-fire service, for scheduling execution of jobs:

1) Service is started on a server somewhere (runs completely independently from your own application)

2) A job for future execution is passed to the service by your own application

3) Job is persisted in a datastore

5) Service consumes jobs from datastore that are pending execution (based on trigger date)

6) Service exposes job for your own application to consume

8) Your own application executes the job

* Support for single date or repeated cron based job scheduling
 
* Ability to add, update or remove stored jobs
