package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.patch.Patch
import org.huwtl.penfold.domain.model.{AggregateId, AggregateVersion, User}
import org.joda.time.DateTime

case class TaskCancelled(aggregateId: AggregateId,
                      aggregateVersion: AggregateVersion,
                      created: DateTime,
                      user: Option[User],
                      reason: Option[String],
                      payloadUpdate: Option[Patch]) extends TaskEvent