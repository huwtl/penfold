package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.{User, AggregateId, AggregateVersion}
import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.patch.Patch

case class TaskClosed(aggregateId: AggregateId,
                      aggregateVersion: AggregateVersion,
                      created: DateTime,
                      concluder: Option[User],
                      conclusionType: Option[String],
                      assignee: Option[User],
                      payloadUpdate: Option[Patch]) extends TaskEvent