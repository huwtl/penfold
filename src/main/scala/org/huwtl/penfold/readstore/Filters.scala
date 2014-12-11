package org.huwtl.penfold.readstore

object Filters {
  val empty = Filters(Nil)
}

case class Filters(all: List[Filter]) {
  def get(name: String): Option[Filter] = all.find(_.key == name)

  def keys = all.map(_.key)
}
