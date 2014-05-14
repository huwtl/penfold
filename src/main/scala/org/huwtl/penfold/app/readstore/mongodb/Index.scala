package org.huwtl.penfold.app.readstore.mongodb

import org.huwtl.penfold.readstore.Filters

case class Index(fields: List[IndexField]) {
  private val sortIndexFields = List(IndexField("sort", "sort"), IndexField("_id", "_id"))

  def suitableFor(filters: Filters) = {
    filters.keys.toSet == fields.map(_.alias).toSet
  }

  val singleKeyFields = fields.filter(!_.multiKey)
  val multiKeyFields = fields.filter(_.multiKey)
  val excludingSortFields = fields.filterNot(sortIndexFields.contains)
}
