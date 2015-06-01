package com.qmetric.penfold.domain.event

import com.qmetric.penfold.domain.model.patch.Patch
import com.qmetric.penfold.domain.model.{User, AggregateId, AggregateVersion}
import org.joda.time.DateTime

case class TaskReassigned(aggregateId: AggregateId,
                          aggregateVersion: AggregateVersion,
                          created: DateTime,
                          assignee: User,
                          reassignType: Option[String],
                          payloadUpdate: Option[Patch]) extends TaskEvent