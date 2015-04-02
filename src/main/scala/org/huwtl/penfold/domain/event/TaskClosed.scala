package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.{CloseResultType, User, AggregateId, AggregateVersion}
import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.patch.Patch

case class TaskClosed(aggregateId: AggregateId,
                      aggregateVersion: AggregateVersion,
                      created: DateTime,
                      user: Option[User],
                      reason: Option[String],
                      resultType: Option[CloseResultType],
                      payloadUpdate: Option[Patch]) extends TaskEvent