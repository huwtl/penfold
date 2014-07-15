package com.qmetric.penfold.domain.event

import com.qmetric.penfold.domain.model.{Assignee, AggregateId, AggregateVersion}
import org.joda.time.DateTime

case class TaskClosed(aggregateId: AggregateId,
                      aggregateVersion: AggregateVersion,
                      created: DateTime,
                      concluder: Option[Assignee] = None,
                      conclusionType: Option[String] = None) extends TaskEvent