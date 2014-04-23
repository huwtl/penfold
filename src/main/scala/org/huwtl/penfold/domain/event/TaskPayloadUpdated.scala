package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.{Payload, AggregateId, AggregateVersion}
import org.joda.time.DateTime

case class TaskPayloadUpdated(aggregateId: AggregateId,
                              aggregateVersion: AggregateVersion,
                              created: DateTime,
                              payload: Payload,
                              updateType: Option[String],
                              score: Option[Long]) extends TaskEvent
