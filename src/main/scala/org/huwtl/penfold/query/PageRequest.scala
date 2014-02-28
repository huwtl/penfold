package org.huwtl.penfold.query

case class PageRequest(pageNumber: Int, pageSize: Int) {
  val start = pageNumber * pageSize
  val end = start + pageSize
  val firstPage = pageNumber == 0
}
