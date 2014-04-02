package org.huwtl.penfold.query

object PageResult {
  val empty = PageResult(0, List(), previousExists = false, nextExists = false)
}

case class PageResult(pageNumber: Int, jobs: List[JobRecord], previousExists: Boolean, nextExists: Boolean) {
  val previousPageNumber = if (previousExists) pageNumber - 1 else pageNumber
  val nextPageNumber = if (nextExists) pageNumber + 1 else pageNumber
  val isEmpty = jobs.isEmpty
}
