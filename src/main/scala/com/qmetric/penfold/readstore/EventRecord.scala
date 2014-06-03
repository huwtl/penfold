package com.qmetric.penfold.readstore

import com.qmetric.penfold.domain.event.Event

case class EventRecord(id: EventSequenceId, event: Event)