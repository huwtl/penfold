package org.huwtl.penfold.app.readstore.postgres

import org.huwtl.penfold.domain.model._
import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.readstore.{TaskProjectionReference, TaskProjection}

case class TaskData(id: AggregateId,
                    version: AggregateVersion,
                    created: Long,
                    queue: QueueId,
                    status: Status,
                    statusLastModified: Long,
                    previousStatus: Option[PreviousStatus],
                    attempts: Int = 0,
                    triggerDate: Long,
                    assignee: Option[User],
                    score: Long,
                    sort: Long,
                    payload: Payload,
                    rescheduleReason: Option[String] = None,
                    closeReason: Option[String] = None) {
  def toTaskProjection = {
    TaskProjection(id, version, new DateTime(created), queue, status, new DateTime(statusLastModified), previousStatus.map(_.toPreviousStatusProjection), attempts, assignee, new DateTime(triggerDate), score, sort, payload, rescheduleReason, closeReason)
  }

  def toTaskProjectionReference = {
    TaskProjectionReference(id, version)
  }
}

case class PreviousStatus(status: Status, statusLastModified: Long) {
  def toPreviousStatusProjection = org.huwtl.penfold.readstore.PreviousStatus(status, new DateTime(statusLastModified))
}