package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.{User, AggregateId, AggregateVersion}
import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.patch.Patch

case class TaskStarted(aggregateId: AggregateId,
                       aggregateVersion: AggregateVersion,
                       created: DateTime,
                       assignee: Option[User],
                       payloadUpdate: Option[Patch]) extends TaskEvent
