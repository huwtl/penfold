package com.qmetric.penfold.app.readstore.mongodb

case class QueryPlan(restrictionFields: List[RestrictionField], sortFields: List[SortField])

case class RestrictionField(name: String, values: Set[Option[String]]) {
  val isMulti = values.size > 1
}

case class SortField(name: String)