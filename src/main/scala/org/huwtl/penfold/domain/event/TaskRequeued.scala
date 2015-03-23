package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.{User, AggregateId, AggregateVersion}
import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.patch.Patch

case class TaskRequeued(aggregateId: AggregateId,
                        aggregateVersion: AggregateVersion,
                        created: DateTime,
                        reason: Option[String],
                        assignee: Option[User],
                        payloadUpdate: Option[Patch],
                        score: Option[Long]) extends TaskEvent
