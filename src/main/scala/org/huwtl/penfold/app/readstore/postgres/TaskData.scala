package org.huwtl.penfold.app.readstore.postgres

import org.huwtl.penfold.domain.model._
import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.readstore.{TaskRecordReference, PreviousStatus, TaskRecord}

case class TaskData(id: AggregateId,
                    version: AggregateVersion,
                    created: DateTime,
                    queue: QueueId,
                    status: Status,
                    statusLastModified: DateTime,
                    previousStatus: Option[PreviousStatus],
                    triggerDate: DateTime,
                    assignee: Option[User],
                    score: Long,
                    sort: Long,
                    payload: Payload,
                    rescheduleType: Option[String] = None,
                    conclusionType: Option[String] = None) {
  def toTaskRecord = {
    TaskRecord(id, version, created, QueueBinding(queue), status, statusLastModified, previousStatus, assignee, triggerDate, score, sort, payload, rescheduleType, conclusionType)
  }

  def toTaskRecordReference = {
    TaskRecordReference(id, version)
  }
}