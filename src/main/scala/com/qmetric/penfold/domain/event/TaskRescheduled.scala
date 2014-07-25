package com.qmetric.penfold.domain.event

import com.qmetric.penfold.domain.model.{User, AggregateId, AggregateVersion}
import org.joda.time.DateTime
import com.qmetric.penfold.domain.model.patch.Patch

case class TaskRescheduled(aggregateId: AggregateId,
                           aggregateVersion: AggregateVersion,
                           created: DateTime,
                           triggerDate: DateTime,
                           assignee: Option[User],
                           rescheduleType: Option[String],
                           payloadUpdate: Option[Patch],
                           score: Option[Long]) extends TaskEvent
