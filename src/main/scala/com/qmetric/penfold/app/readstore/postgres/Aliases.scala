package com.qmetric.penfold.app.readstore.postgres

case class Alias(value: String)

case class Path(value: String)

object Aliases {
  def empty = Aliases(Map.empty)
}

case class Aliases(aliases: Map[Alias, Path])
{
  def path(alias: Alias) = aliases.getOrElse(alias, Path(alias.value))
}