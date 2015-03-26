package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model._
import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.model.Payload

case class FutureTaskCreated(aggregateId: AggregateId,
                             aggregateVersion: AggregateVersion,
                             created: DateTime,
                             queue: QueueId,
                             triggerDate: DateTime,
                             payload: Payload,
                             score: Long) extends TaskCreatedEvent