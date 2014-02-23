package org.huwtl.penfold.command

class CommandDispatcher(handlers: Map[Class[_ <: Command], CommandHandler[_ <: Command]]) {
  def dispatch[T <: Command](command: T): Unit = {
    val applicableHandler = handlers.get(command.getClass).asInstanceOf[Option[CommandHandler[Command]]]

    require(applicableHandler.isDefined, "Command type not supported")

    applicableHandler.get.handle(command)
  }
}