package org.huwtl.penfold.query

import org.huwtl.penfold.domain.model.{QueueName, Status, AggregateId}

trait QueryRepository {
  def retrieveBy(id: AggregateId): Option[JobRecord]

  def retrieveBy(queueName: QueueName, status: Status, pageRequest: PageRequest): PageResult

  def retrieveWithPendingTrigger: Stream[JobRecordReference]
}
