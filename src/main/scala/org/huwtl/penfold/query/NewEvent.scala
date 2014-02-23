package org.huwtl.penfold.query

import org.huwtl.penfold.domain.model.Id
import org.huwtl.penfold.domain.event.Event

case class NewEvent(id: Id, event: Event)