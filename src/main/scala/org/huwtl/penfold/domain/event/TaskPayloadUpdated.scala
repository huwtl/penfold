package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.{AggregateId, AggregateVersion}
import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.patch.Patch

case class TaskPayloadUpdated(aggregateId: AggregateId,
                              aggregateVersion: AggregateVersion,
                              created: DateTime,
                              payloadUpdate: Patch,
                              updateType: Option[String],
                              score: Option[Long]) extends TaskEvent
