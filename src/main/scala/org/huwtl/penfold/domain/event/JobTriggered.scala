package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.{QueueId, AggregateId, AggregateVersion}
import org.joda.time.DateTime

case class JobTriggered(aggregateId: AggregateId,
                        aggregateVersion: AggregateVersion,
                        created: DateTime,
                        queues: List[QueueId]) extends JobEvent
