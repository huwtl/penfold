package org.huwtl.penfold.readstore

import org.huwtl.penfold.domain.model.Status
import org.huwtl.penfold.domain.model.QueueId
import org.huwtl.penfold.domain.model.AggregateId

trait ReadStore {
  def checkConnectivity: Either[Boolean, Exception]

  def retrieveBy(id: AggregateId): Option[TaskRecord]

  def retrieveBy(filters: Filters, pageRequest: PageRequest): PageResult
  
  def retrieveByQueue(queueId: QueueId, status: Status, pageRequest: PageRequest, filters: Filters = Filters.empty): PageResult

  def retrieveTasksToTrigger: Iterator[TaskRecordReference]

  def retrieveTasksToArchive(timeoutAttributePath: String): Iterator[TaskRecordReference]
}
