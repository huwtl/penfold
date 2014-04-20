package org.huwtl.penfold.app.readstore.mongodb

import org.huwtl.penfold.readstore.{Filter, Filters}

case class Indexes(private val customIndexes: List[Index]) {
  private val statusField = IndexField("status", "status")

  private val sortIndexFields = List(IndexField("sort", "sort"), IndexField("_id", "_id"))

  private val idVersionIndex = Index(List(IndexField("_id", "_id"), IndexField("version", "version")))

  private val queueIndex = Index(List(IndexField("queue", "queue"), statusField) ::: sortIndexFields)

  private val statusIndex = Index(statusField :: sortIndexFields)

  val all = idVersionIndex :: queueIndex :: statusIndex :: augmentCustomIndexes

  def transformForSuitableIndex(filters: Filters) = {
    all.find(_.suitableFor(filters)) match {
      case Some(suitableIndex) => Filters(suitableIndex.fields.map(field => Filter(field.path, filters.get(field.alias).get.values)))
      case None => filters
    }
  }

  private def augmentCustomIndexes = {
    val augmentedCustomIndexes = for {
      customIndex <- customIndexes
      enhancement <- List(queueIndex)
    } yield Index(enhancement.fields ::: customIndex.fields ::: sortIndexFields)

    customIndexes ::: augmentedCustomIndexes
  }
}
