package org.huwtl.penfold.command

import org.huwtl.penfold.domain.model.AggregateId

trait CommandHandler[C <: Command] {
  def handle(command: C): AggregateId
}

trait Command