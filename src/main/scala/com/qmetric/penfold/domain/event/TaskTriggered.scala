package com.qmetric.penfold.domain.event

import com.qmetric.penfold.domain.model.{QueueId, AggregateId, AggregateVersion}
import org.joda.time.DateTime

case class TaskTriggered(aggregateId: AggregateId,
                         aggregateVersion: AggregateVersion,
                         created: DateTime) extends TaskEvent
