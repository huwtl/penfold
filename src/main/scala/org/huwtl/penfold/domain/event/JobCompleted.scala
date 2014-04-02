package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.{QueueId, AggregateId, Version}

case class JobCompleted(aggregateId: AggregateId, aggregateVersion: Version, queue: QueueId) extends Event
