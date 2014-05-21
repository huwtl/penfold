package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.{Assignee, AggregateId, AggregateVersion}
import org.joda.time.DateTime

case class TaskStarted(aggregateId: AggregateId,
                       aggregateVersion: AggregateVersion,
                       created: DateTime,
                       assignee: Option[Assignee]) extends TaskEvent
