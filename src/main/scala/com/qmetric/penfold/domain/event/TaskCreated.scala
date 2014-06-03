package com.qmetric.penfold.domain.event

import com.qmetric.penfold.domain.model._
import org.joda.time.DateTime
import com.qmetric.penfold.domain.model.AggregateId
import com.qmetric.penfold.domain.model.Payload

case class TaskCreated(aggregateId: AggregateId,
                       aggregateVersion: AggregateVersion,
                       created: DateTime,
                       queueBinding: QueueBinding,
                       triggerDate: DateTime,
                       payload: Payload,
                       score: Long) extends TaskCreatedEvent