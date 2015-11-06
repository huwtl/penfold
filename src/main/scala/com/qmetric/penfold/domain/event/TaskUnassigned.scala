package com.qmetric.penfold.domain.event

import com.qmetric.penfold.domain.model.{AggregateId, AggregateVersion}
import org.joda.time.DateTime
import com.qmetric.penfold.domain.model.patch.Patch

case class TaskUnassigned(aggregateId: AggregateId,
                          aggregateVersion: AggregateVersion,
                          created: DateTime,
                          reason: Option[String],
                          payloadUpdate: Option[Patch]) extends TaskEvent