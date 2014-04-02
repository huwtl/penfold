package org.huwtl.penfold.app.query.redis

import org.huwtl.penfold.domain.model._

case class JobSearchRecord(queues: List[QueueId],
                           status: Status,
                           payload: Payload)
