package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model.{User, AggregateVersion, AggregateId}
import org.joda.time.DateTime

case class TaskCancelled(aggregateId: AggregateId,
                         aggregateVersion: AggregateVersion,
                         created: DateTime,
                         concluder: Option[User] = None,
                         conclusionType: Option[String] = None) extends TaskConcludedEvent
