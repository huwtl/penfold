package com.qmetric.penfold.command

import com.qmetric.penfold.domain.model.AggregateId

class CommandDispatcher(handlers: Map[Class[_ <: Command], CommandHandler[_ <: Command]]) {
  def dispatch[T <: Command](command: T): AggregateId = {
    val applicableHandler = handlers.get(command.getClass).asInstanceOf[Option[CommandHandler[Command]]]

    require(applicableHandler.isDefined, s"Command $command not supported")

    applicableHandler.get.handle(command)
  }
}