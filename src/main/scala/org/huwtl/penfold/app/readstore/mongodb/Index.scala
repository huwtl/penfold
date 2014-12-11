package org.huwtl.penfold.app.readstore.mongodb

import org.huwtl.penfold.readstore.Filters

case class Index(name: Option[String], fields: List[IndexField]) {
  private val sortIndexFields = List(IndexField("sort", "sort"), IndexField("_id", "_id"))

  def suitableFor(filters: Filters) = {
    val filterKeys = filters.keys

    val consecutivelyMatchedIndexFields = excludingSortFields.map(_.alias).takeWhile(filterKeys.contains)

    consecutivelyMatchedIndexFields.nonEmpty && consecutivelyMatchedIndexFields.size == filterKeys.size
  }

  val singleKeyFields = fields.filterNot(_.multiKey)
  val multiKeyFields = fields.filter(_.multiKey)
  val excludingSortFields = fields.filterNot(sortIndexFields.contains)
}
