package com.qmetric.penfold.command

import com.qmetric.penfold.domain.model.AggregateId

trait CommandHandler[C <: Command] {
  def handle(command: C): AggregateId
}

trait Command