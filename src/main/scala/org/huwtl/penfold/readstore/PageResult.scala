package org.huwtl.penfold.readstore

object PageResult {
  val empty = PageResult(Nil, previousPage = None, nextPage = None)
}

case class PageResult(entries: List[TaskRecord], previousPage: Option[PageReference], nextPage: Option[PageReference]) {
  val isEmpty = entries.isEmpty
  val previousExists = previousPage.isDefined
  val nextExists = nextPage.isDefined
}
