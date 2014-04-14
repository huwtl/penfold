package org.huwtl.penfold.readstore

import org.huwtl.penfold.domain.model._

case class TaskSearchRecord(queues: List[QueueId],
                           status: Status,
                           payload: Payload)
