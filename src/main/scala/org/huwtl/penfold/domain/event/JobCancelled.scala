package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.{QueueId, Version, AggregateId}
import org.joda.time.DateTime

case class JobCancelled(aggregateId: AggregateId,
                        aggregateVersion: Version,
                        created: DateTime,
                        queues: List[QueueId]) extends Event
