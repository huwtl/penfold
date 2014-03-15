package org.huwtl.penfold.query

import org.huwtl.penfold.domain.model.{QueueName, Status, AggregateId}

trait QueryRepository {
  def retrieveBy(id: AggregateId): Option[JobRecord]

  def retrieveBy(filters: Filters, pageRequest: PageRequest): PageResult
  
  def retrieveBy(queueName: QueueName, status: Status, pageRequest: PageRequest, filters: Filters = Filters.empty): PageResult

  def retrieveWithPendingTrigger: Stream[JobRecordReference]
}
