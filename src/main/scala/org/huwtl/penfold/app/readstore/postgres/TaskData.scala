package org.huwtl.penfold.app.readstore.postgres

import org.huwtl.penfold.domain.model._
import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.readstore.{TaskRecordReference, TaskRecord}

case class TaskData(id: AggregateId,
                    version: AggregateVersion,
                    created: Long,
                    queue: QueueId,
                    status: Status,
                    statusLastModified: Long,
                    previousStatus: Option[PreviousStatus],
                    triggerDate: Long,
                    assignee: Option[User],
                    score: Long,
                    sort: Long,
                    payload: Payload,
                    rescheduleType: Option[String] = None,
                    conclusionType: Option[String] = None) {
  def toTaskRecord = {
    TaskRecord(id, version, new DateTime(created), QueueBinding(queue), status, new DateTime(statusLastModified), previousStatus.map(_.toPreviousStatusProjection), assignee, new DateTime(triggerDate), score, sort, payload, rescheduleType, conclusionType)
  }

  def toTaskRecordReference = {
    TaskRecordReference(id, version)
  }
}

case class PreviousStatus(status: Status, statusLastModified: Long) {
  def toPreviousStatusProjection = org.huwtl.penfold.readstore.PreviousStatus(status, new DateTime(statusLastModified))
}