package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.{User, AggregateId, AggregateVersion}
import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.patch.Patch

case class TaskRescheduled(aggregateId: AggregateId,
                           aggregateVersion: AggregateVersion,
                           created: DateTime,
                           triggerDate: DateTime,
                           assignee: Option[User],
                           rescheduleReason: Option[String],
                           payloadUpdate: Option[Patch],
                           score: Option[Long]) extends TaskEvent
