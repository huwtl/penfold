package com.qmetric.penfold.app.readstore.postgres

import com.qmetric.penfold.domain.model._
import org.joda.time.DateTime
import com.qmetric.penfold.domain.model.AggregateId
import com.qmetric.penfold.domain.model.Payload
import com.qmetric.penfold.readstore.{TaskProjectionReference, TaskProjection}

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
                    cancelReason: Option[String] = None,
                    closeReason: Option[String] = None,
                    closeResultType: Option[CloseResultType] = None) {
  def toTaskProjection = {
    TaskProjection(
      id,
      version,
      new DateTime(created),
      queue,
      status,
      new DateTime(statusLastModified),
      previousStatus.map(_.toPreviousStatusProjection),
      attempts,
      assignee,
      new DateTime(triggerDate),
      score,
      sort,
      payload,
      rescheduleReason,
      cancelReason,
      closeReason,
      closeResultType
    )
  }

  def toTaskProjectionReference = {
    TaskProjectionReference(id, version)
  }
}

case class PreviousStatus(status: Status, statusLastModified: Long) {
  def toPreviousStatusProjection = com.qmetric.penfold.readstore.PreviousStatus(status, new DateTime(statusLastModified))
}