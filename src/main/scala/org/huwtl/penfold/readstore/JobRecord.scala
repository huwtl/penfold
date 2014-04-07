package org.huwtl.penfold.readstore

import org.huwtl.penfold.domain.model._
import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.model.Payload

case class JobRecord(id: AggregateId,
                     created: DateTime,
                     binding: Binding,
                     status: Status,
                     triggerDate: DateTime,
                     payload: Payload)
