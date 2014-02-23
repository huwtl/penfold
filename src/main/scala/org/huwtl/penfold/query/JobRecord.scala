package org.huwtl.penfold.query

import org.huwtl.penfold.domain.model.{Payload, Status, JobType, Id}
import org.joda.time.DateTime

case class JobRecord(id: Id,
                     created: DateTime,
                     jobType: JobType,
                     status: Status,
                     triggerDate: DateTime,
                     payload: Payload)
