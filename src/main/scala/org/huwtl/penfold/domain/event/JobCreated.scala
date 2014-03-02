package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.{Payload, QueueName, Version, AggregateId}
import org.joda.time.DateTime

case class JobCreated(aggregateId: AggregateId,
                      aggregateVersion: Version,
                      queueName: QueueName,
                      created: DateTime,
                      triggerDate: DateTime,
                      payload: Payload) extends Event
