package org.huwtl.penfold.app.store.postgres

import org.huwtl.penfold.domain.model.{AggregateId, AggregateRoot}
import org.huwtl.penfold.domain.store.DomainRepository

import scala.slick.driver.JdbcDriver.backend.Database

class PostgresTransactionalDomainRepository(database: Database, domainRepository: DomainRepository) extends DomainRepository {
  override def getById[T <: AggregateRoot](id: AggregateId): T = {
    database.withDynSession {
      domainRepository.getById[T](id)
    }
  }

  override def add(aggregateRoot: AggregateRoot): AggregateRoot = {
    database.withDynTransaction {
      domainRepository.add(aggregateRoot)
    }
  }
}
