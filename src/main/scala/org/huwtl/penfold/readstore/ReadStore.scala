package org.huwtl.penfold.readstore

import org.huwtl.penfold.domain.model.Status
import org.huwtl.penfold.domain.model.QueueId
import org.huwtl.penfold.domain.model.AggregateId

trait ReadStore {
  def checkConnectivity: Either[Boolean, Exception]

  def retrieveBy(id: AggregateId): Option[JobRecord]

  def retrieveBy(filters: Filters, pageRequest: PageRequest): PageResult
  
  def retrieveByQueue(queueId: QueueId, status: Status, pageRequest: PageRequest, filters: Filters = Filters.empty): PageResult

  def retrieveJobsToTrigger: Iterator[JobRecordReference]
}
