package org.huwtl.penfold.readstore

object Filters {
  val empty = Filters(Nil)
}

case class Filters(filters: List[Filter]) {
  def get(name: String): Option[Filter] = filters.find(_.key == name)

  def keys = filters.map(_.key)
}
