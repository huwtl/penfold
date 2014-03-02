package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.{AggregateId, Version}

case class JobStarted(aggregateId: AggregateId, aggregateVersion: Version) extends Event
