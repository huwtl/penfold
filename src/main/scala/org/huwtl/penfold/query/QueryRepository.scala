package org.huwtl.penfold.query

import org.huwtl.penfold.domain.model.{JobType, Status, Id}

trait QueryRepository {
  def retrieveBy(id: Id): Option[JobRecord]

  def retrieveBy(status: Status): List[JobRecord]

  def retrieveBy(status: Status, jobType: JobType): List[JobRecord]

  def retrieveWithPendingTrigger: Stream[JobRecordReference]
}
