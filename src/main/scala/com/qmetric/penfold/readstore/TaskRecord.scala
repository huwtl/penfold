package com.qmetric.penfold.readstore

import com.qmetric.penfold.domain.model._
import org.joda.time.DateTime
import com.qmetric.penfold.domain.model.AggregateId
import com.qmetric.penfold.domain.model.Payload

case class TaskRecord(id: AggregateId,
                      version: AggregateVersion,
                      created: DateTime,
                      queueBinding: QueueBinding,
                      status: Status,
                      statusLastModified: DateTime,
                      previousStatus: Option[PreviousStatus],
                      assignee: Option[Assignee],
                      triggerDate: DateTime,
                      score: Long,
                      sort: Long,
                      payload: Payload,
                      rescheduleType: Option[String] = None,
                      conclusionType: Option[String] = None)

case class PreviousStatus(status: Status, statusLastModified: DateTime)