package org.huwtl.penfold.query

import org.huwtl.penfold.domain.model.{Payload, Status, QueueName, Id}
import org.joda.time.DateTime

case class JobRecord(id: Id,
                     created: DateTime,
                     queueName: QueueName,
                     status: Status,
                     triggerDate: DateTime,
                     payload: Payload)
