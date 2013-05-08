package com.hlewis.eventfire.app.store.redis

import com.redis.RedisClient
import com.hlewis.eventfire.app.store.JobStoreFactory

trait RedisJobStoreFactory extends JobStoreFactory {
  override def initJobStore() = {
    new RedisJobStore(new RedisClient("localhost", 6379))
  }
}
