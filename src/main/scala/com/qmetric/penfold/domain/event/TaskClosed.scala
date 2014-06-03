package com.qmetric.penfold.domain.event

import com.qmetric.penfold.domain.model.{User, AggregateId, AggregateVersion}
import org.joda.time.DateTime

case class TaskClosed(aggregateId: AggregateId,
                      aggregateVersion: AggregateVersion,
                      created: DateTime,
                      concluder: Option[User] = None,
                      conclusionType: Option[String] = None) extends TaskEvent