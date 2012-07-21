### Overview

WORK IN PROGRESS

Scheduling future job execution with  [RabbitMQ](http://www.rabbitmq.com/) - sort of. RabbitMQ wasn't designed for scheduling future job execution (although what it was designed for, it does very well). I'm hoping to utilise [Redis](http://redis.io/) in combination with RabbitMQ to achieve this.

The rabbit-scheduler service, written in [Scala](http://www.scala-lang.org/) with [Scalatra](http://www.scalatra.org/):

1) rabbit-scheduler is started on a server somewhere (runs completely independently from your own application)

2) A job for future execution is added to a RabbitMQ exchange by your own application

3) rabbit-scheduler service consumes job

4) rabbit-scheduler stores job in a Redis datastore

5) rabbit-scheduler pops next job from Redis that is pending execution (based on trigger date)

6) rabbit-scheduler adds job to a pending-job RabbitMQ exchange

7) Your own application consumes the pending job from the RabbitMQ exchange

8) Your own application executes the job

* Support for single date or repeated cron based job scheduling
* Pretty Webapp interface UI for administering scheduled jobs
* Restful API for administering scheduled jobs
* Ability to add, update or remove scheduled jobs via messages on RabbitMQ exchanges
