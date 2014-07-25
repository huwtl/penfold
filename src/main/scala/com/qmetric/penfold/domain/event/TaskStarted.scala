package com.qmetric.penfold.domain.event

import com.qmetric.penfold.domain.model.{User, AggregateId, AggregateVersion}
import org.joda.time.DateTime
import com.qmetric.penfold.domain.model.patch.Patch

case class TaskStarted(aggregateId: AggregateId,
                       aggregateVersion: AggregateVersion,
                       created: DateTime,
                       assignee: Option[User],
                       payloadUpdate: Option[Patch]) extends TaskEvent
