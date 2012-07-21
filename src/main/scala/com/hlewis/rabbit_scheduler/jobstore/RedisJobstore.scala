package com.hlewis.rabbit_scheduler.jobstore

import com.redis.RedisClient

class RedisJobstore (client: RedisClient = new RedisClient("localhost", 6379)) {

  def add(key: String, value: String) = {
    client.hset("test", key, value)
  }
}
