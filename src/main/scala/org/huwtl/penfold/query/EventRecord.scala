package org.huwtl.penfold.query

import org.huwtl.penfold.domain.event.Event

case class EventRecord(id: EventSequenceId, event: Event)