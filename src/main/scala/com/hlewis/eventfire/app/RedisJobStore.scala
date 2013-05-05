package com.hlewis.eventfire.app

import com.redis.RedisClient
import com.hlewis.eventfire.domain.{Job, JobStore}

class RedisJobStore(client: RedisClient) extends JobStore {

  override def add(job: Job) = {
    println(job.body.data.get("data"))
    client.hset("test", job.header.reference, job.body.data.get("data").get)
  }

}
