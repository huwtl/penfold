package org.huwtl.penfold.app.store.redis

import org.specs2.mutable.Specification
import org.huwtl.penfold.app.RedisConnectionPool
import com.redis.RedisClientPool

class RedisConnectionPoolFactoryTest extends Specification {
  "create redis connection pool" in {
    val factory = new RedisConnectionPoolFactory
    factory.create(RedisConnectionPool("host", 6379, 0, Some("secret"), 15)) must beAnInstanceOf[RedisClientPool]
  }
}
