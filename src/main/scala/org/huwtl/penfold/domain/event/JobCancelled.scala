package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.{QueueId, Version, AggregateId}

case class JobCancelled(aggregateId: AggregateId, aggregateVersion: Version, queues: List[QueueId]) extends Event
