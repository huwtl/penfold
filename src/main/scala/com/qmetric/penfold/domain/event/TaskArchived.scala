package com.qmetric.penfold.domain.event

import com.qmetric.penfold.domain.model.{AggregateId, AggregateVersion}
import org.joda.time.DateTime

case class TaskArchived(aggregateId: AggregateId,
                        aggregateVersion: AggregateVersion,
                        created: DateTime) extends TaskEvent
