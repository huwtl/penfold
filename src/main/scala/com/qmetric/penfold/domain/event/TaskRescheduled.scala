package com.qmetric.penfold.domain.event

import com.qmetric.penfold.domain.model.{Assignee, AggregateId, AggregateVersion}
import org.joda.time.DateTime

case class TaskRescheduled(aggregateId: AggregateId,
                           aggregateVersion: AggregateVersion,
                           created: DateTime,
                           triggerDate: DateTime,
                           assignee: Option[Assignee] = None,
                           rescheduleType: Option[String] = None) extends TaskEvent
