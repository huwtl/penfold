package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.{QueueId, AggregateId, Version}
import org.joda.time.DateTime

case class JobTriggered(aggregateId: AggregateId,
                        aggregateVersion: Version,
                        created: DateTime,
                        queues: List[QueueId]) extends Event
