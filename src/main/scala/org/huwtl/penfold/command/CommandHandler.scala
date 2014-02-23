package org.huwtl.penfold.command

trait CommandHandler[C <: Command] {
  def handle(command: C) : Unit
}

trait Command