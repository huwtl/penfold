package org.huwtl.penfold.app.readstore.redis

import org.huwtl.penfold.domain.model._

case class JobSearchRecord(queues: List[QueueId],
                           status: Status,
                           payload: Payload)
