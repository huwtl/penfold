package org.huwtl.penfold.domain.event

import org.huwtl.penfold.domain.model._
import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.domain.model.Payload

case class JobCreated(aggregateId: AggregateId,
                      aggregateVersion: Version,
                      binding: Binding,
                      created: DateTime,
                      triggerDate: DateTime,
                      payload: Payload) extends Event
