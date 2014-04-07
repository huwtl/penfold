package org.huwtl.penfold.readstore

import org.huwtl.penfold.domain.event.Event

case class EventRecord(id: EventSequenceId, event: Event)