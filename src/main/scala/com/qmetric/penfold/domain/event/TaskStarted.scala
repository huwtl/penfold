package com.qmetric.penfold.domain.event

import com.qmetric.penfold.domain.model.{Assignee, AggregateId, AggregateVersion}
import org.joda.time.DateTime

case class TaskStarted(aggregateId: AggregateId,
                       aggregateVersion: AggregateVersion,
                       created: DateTime,
                       assignee: Option[Assignee]) extends TaskEvent
