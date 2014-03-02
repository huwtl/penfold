package org.huwtl.penfold.query

import org.huwtl.penfold.domain.model.{Payload, Status, QueueName, AggregateId}
import org.joda.time.DateTime

case class JobRecord(id: AggregateId,
                     created: DateTime,
                     queueName: QueueName,
                     status: Status,
                     triggerDate: DateTime,
                     payload: Payload)
