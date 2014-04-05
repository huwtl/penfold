package org.huwtl.penfold.app.store

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.huwtl.penfold.app.support.json.EventSerializer
import org.huwtl.penfold.app.{RedisConnectionPool, JdbcConnectionPool}
import org.huwtl.penfold.app.store.jdbc.{JdbcDomainEventQueryService, JdbcEventStore, JdbcConnectionPoolFactory, JdbcDatabaseInitialiser}
import org.huwtl.penfold.app.store.redis.{RedisDomainEventQueryService, RedisEventStore, RedisConnectionPoolFactory}

class EventStoreConfigurationProviderTest extends Specification with Mockito {
  val eventSerializer = mock[EventSerializer]

  "provide jdbc event store configuration" in {
    val poolConfig = mock[JdbcConnectionPool]
    val databaseInitialiser = mock[JdbcDatabaseInitialiser]
    val poolFactory = mock[JdbcConnectionPoolFactory]

    val config = new EventStoreConfigurationProvider(eventSerializer, Left(poolConfig), databaseInitialiser, poolFactory).get()

    config.domainEventStore must beAnInstanceOf[JdbcEventStore]
    config.domainEventQueryService must beAnInstanceOf[JdbcDomainEventQueryService]
  }

  "provide redis event store configuration" in {
    val poolConfig = mock[RedisConnectionPool]
    val poolFactory = mock[RedisConnectionPoolFactory]

    val config = new EventStoreConfigurationProvider(eventSerializer, Right(poolConfig), redisConnectionPoolFactory = poolFactory).get()

    config.domainEventStore must beAnInstanceOf[RedisEventStore]
    config.domainEventQueryService must beAnInstanceOf[RedisDomainEventQueryService]
  }
}
