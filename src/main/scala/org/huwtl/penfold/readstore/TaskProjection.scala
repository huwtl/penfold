package org.huwtl.penfold.readstore

import org.huwtl.penfold.domain.model._
import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.model.Payload

case class TaskProjection(id: AggregateId,
                          version: AggregateVersion,
                          created: DateTime,
                          queue: QueueId,
                          status: Status,
                          statusLastModified: DateTime,
                          previousStatus: Option[PreviousStatus],
                          attempts: Int = 0,
                          assignee: Option[User],
                          triggerDate: DateTime,
                          score: Long,
                          sort: Long,
                          payload: Payload,
                          rescheduleReason: Option[String] = None,
                          cancelReason: Option[String] = None,
                          closeReason: Option[String] = None)

case class PreviousStatus(status: Status, statusLastModified: DateTime)