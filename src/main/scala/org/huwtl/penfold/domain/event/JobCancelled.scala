package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.{Version, AggregateId}

case class JobCancelled(aggregateId: AggregateId, aggregateVersion: Version) extends Event
