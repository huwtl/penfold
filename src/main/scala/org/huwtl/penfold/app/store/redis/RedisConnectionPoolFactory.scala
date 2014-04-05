package org.huwtl.penfold.app.store.redis

import org.huwtl.penfold.app.RedisConnectionPool
import com.redis.RedisClientPool

class RedisConnectionPoolFactory {
  def create(poolConfig: RedisConnectionPool) = {
    new RedisClientPool(
      poolConfig.host,
      poolConfig.port,
      poolConfig.poolSize,
      poolConfig.database,
      poolConfig.password)
  }
}
