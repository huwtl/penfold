package org.huwtl.penfold.readstore

object PageResult {
  val empty = PageResult(Nil, previousExists = false, nextExists = false)
}

case class PageResult(entries: List[JobRecord], previousExists: Boolean, nextExists: Boolean) {
  val isEmpty = entries.isEmpty
}
