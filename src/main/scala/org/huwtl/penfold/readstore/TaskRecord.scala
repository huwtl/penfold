package org.huwtl.penfold.readstore

import org.huwtl.penfold.domain.model._
import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.model.Payload

case class TaskRecord(id: AggregateId,
                     created: DateTime,
                     queueBinding: QueueBinding,
                     status: Status,
                     triggerDate: DateTime,
                     sort: Long,
                     payload: Payload)
