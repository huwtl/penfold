package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.{QueueId, AggregateId, Version}
import org.joda.time.DateTime

case class JobCompleted(aggregateId: AggregateId,
                        aggregateVersion: Version,
                        created: DateTime,
                        queue: QueueId) extends Event
