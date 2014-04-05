package org.huwtl.penfold.app.store

import org.huwtl.penfold.domain.store.EventStore
import org.huwtl.penfold.query.DomainEventQueryService
import org.huwtl.penfold.app.store.jdbc.{JdbcDomainEventQueryService, JdbcEventStore, JdbcConnectionPoolFactory, JdbcDatabaseInitialiser}
import org.huwtl.penfold.app.store.redis.{RedisDomainEventQueryService, RedisEventStore, RedisConnectionPoolFactory}
import org.huwtl.penfold.app.{RedisConnectionPool, JdbcConnectionPool}
import org.huwtl.penfold.app.support.json.EventSerializer

class EventStoreConfigurationProvider(eventSerializer: EventSerializer,
                                      domainConnectionPoolConfig: Either[JdbcConnectionPool, RedisConnectionPool],
                                      jdbcDatabaseInitialiser: JdbcDatabaseInitialiser = new JdbcDatabaseInitialiser(),
                                      jdbcConnectionPoolFactory: JdbcConnectionPoolFactory = new JdbcConnectionPoolFactory(),
                                      redisConnectionPoolFactory: RedisConnectionPoolFactory = new RedisConnectionPoolFactory) {

  def get() = {
    val domainStoreConfig: (EventStore, DomainEventQueryService) = domainConnectionPoolConfig match {
      case Left(jdbcPool) => {
        val domainJdbcPool = jdbcDatabaseInitialiser.init(jdbcConnectionPoolFactory.create(jdbcPool))
        val eventStore = new JdbcEventStore(domainJdbcPool, eventSerializer)
        val eventQueryService = new JdbcDomainEventQueryService(domainJdbcPool, eventSerializer)
        (eventStore, eventQueryService)
      }
      case Right(redisPool) => {
        val domainRedisPool = redisConnectionPoolFactory.create(redisPool)
        val eventStore = new RedisEventStore(domainRedisPool, eventSerializer)
        val eventQueryService = new RedisDomainEventQueryService(domainRedisPool, eventSerializer)
        (eventStore, eventQueryService)
      }
    }

    EventStoreConfig(domainStoreConfig._1, domainStoreConfig._2)
  }

  case class EventStoreConfig(domainEventStore: EventStore, domainEventQueryService: DomainEventQueryService)

}
