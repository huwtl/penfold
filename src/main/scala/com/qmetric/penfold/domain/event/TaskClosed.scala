package com.qmetric.penfold.domain.event

import com.qmetric.penfold.domain.model.{CloseResultType, User, AggregateId, AggregateVersion}
import org.joda.time.DateTime
import com.qmetric.penfold.domain.model.patch.Patch

case class TaskClosed(aggregateId: AggregateId,
                      aggregateVersion: AggregateVersion,
                      created: DateTime,
                      user: Option[User],
                      reason: Option[String],
                      resultType: Option[CloseResultType],
                      payloadUpdate: Option[Patch]) extends TaskEvent