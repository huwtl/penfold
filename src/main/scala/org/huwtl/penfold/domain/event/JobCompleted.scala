package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.{AggregateId, Version}

case class JobCompleted(aggregateId: AggregateId, aggregateVersion: Version) extends Event
