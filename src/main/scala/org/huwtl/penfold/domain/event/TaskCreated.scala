package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model._
import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.model.Payload

case class TaskCreated(aggregateId: AggregateId,
                       aggregateVersion: AggregateVersion,
                       created: DateTime,
                       queueBinding: QueueBinding,
                       triggerDate: DateTime,
                       payload: Payload,
                       score: Long) extends TaskCreatedEvent