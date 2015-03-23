package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.{AggregateId, AggregateVersion}
import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.patch.Patch

case class TaskUnassigned(aggregateId: AggregateId,
                          aggregateVersion: AggregateVersion,
                          created: DateTime,
                          reason: Option[String],
                          payloadUpdate: Option[Patch]) extends TaskEvent