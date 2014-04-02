package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.{QueueId, AggregateId, Version}

case class JobTriggered(aggregateId: AggregateId, aggregateVersion: Version, queues: List[QueueId]) extends Event
