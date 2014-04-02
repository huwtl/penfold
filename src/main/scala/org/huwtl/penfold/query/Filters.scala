package org.huwtl.penfold.query

object Filters {
  val empty = Filters(Nil)
}

case class Filters(private val filters: List[Filter]) {
  val all = filters.filter(!_.value.isEmpty)

  def get(name: String): Option[Filter] = all.find(_.key.toLowerCase == name.toLowerCase)

  def keys = all.map(_.key)
}
