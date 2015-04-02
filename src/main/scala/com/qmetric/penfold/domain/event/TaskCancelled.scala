package com.qmetric.penfold.domain.event

import com.qmetric.penfold.domain.model.patch.Patch
import com.qmetric.penfold.domain.model.{AggregateId, AggregateVersion, User}
import org.joda.time.DateTime

case class TaskCancelled(aggregateId: AggregateId,
                      aggregateVersion: AggregateVersion,
                      created: DateTime,
                      user: Option[User],
                      reason: Option[String],
                      payloadUpdate: Option[Patch]) extends TaskEvent