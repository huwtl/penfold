package org.huwtl.penfold.readstore

import org.huwtl.penfold.domain.model._

case class JobSearchRecord(queues: List[QueueId],
                           status: Status,
                           payload: Payload)
