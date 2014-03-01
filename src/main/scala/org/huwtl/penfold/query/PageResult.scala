package org.huwtl.penfold.query

case class PageResult(pageNumber: Int, jobs: List[JobRecord], previousExists: Boolean, nextExists: Boolean) {
  val previousPageNumber = if (previousExists) pageNumber - 1 else pageNumber
  val nextPageNumber = if (nextExists) pageNumber + 1 else pageNumber
}
