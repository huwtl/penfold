package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model._
import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.AggregateId

trait TaskConcludedEvent extends TaskEvent {
  val aggregateId: AggregateId
  val aggregateVersion: AggregateVersion
  val created: DateTime
  val concluder: Option[User]
  val conclusionType: Option[String]
}