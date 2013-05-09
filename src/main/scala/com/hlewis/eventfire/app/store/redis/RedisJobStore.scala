package com.hlewis.eventfire.app.store.redis

import com.redis.RedisClient
import com.hlewis.eventfire.domain.{Job, JobStore}

class RedisJobStore(client: RedisClient) extends JobStore {
  def retrievePending() = ???

  override def add(job: Job) = {
    client.hset("test", job.header.reference, job.body.data.get("data").get)
  }

  override def update(job: Job) = ???

  override def remove(job: Job) = ???
}
