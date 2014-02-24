package org.huwtl.penfold.query

import org.huwtl.penfold.domain.model.{QueueName, Status, Id}

trait QueryRepository {
  def retrieveBy(id: Id): Option[JobRecord]

  def retrieveBy(status: Status): List[JobRecord]

  def retrieveBy(status: Status, queueName: QueueName): List[JobRecord]

  def retrieveWithPendingTrigger: Stream[JobRecordReference]
}
