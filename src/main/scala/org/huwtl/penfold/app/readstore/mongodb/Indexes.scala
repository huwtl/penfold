package org.huwtl.penfold.app.readstore.mongodb

import org.huwtl.penfold.readstore.{Filter, Filters}

case class Indexes(private val customIndexes: List[Index]) {
  private val statusField = IndexField("status", "status")

  private val sortIndexFields = List(IndexField("sort", "sort"), IndexField("_id", "_id"))

  private val queueIndexFields = List(IndexField("queue", "queue"), statusField)

  private val queueIndex = Index(queueIndexFields ::: sortIndexFields)

  private val statusIndex = Index(statusField :: sortIndexFields)

  val all = queueIndex :: statusIndex :: augmentCustomIndexes

  def transformForSuitableIndex(filters: Filters) = {
    customIndexes.find(_.suitableFor(filters)) match {
      case Some(suitableIndex) => Filters(suitableIndex.fields.map(field => Filter(field.path, filters.get(field.alias).get.values)))
      case None => filters
    }
  }

  def suitableIndex(filters: Filters) = {
    all.find(index => {
      val indexWithoutSortFields = Index(index.fields.filterNot(sortIndexFields.contains))
      indexWithoutSortFields.suitableFor(filters)
    })
  }

  private def augmentCustomIndexes = {
    val customIndexesWithSort = customIndexes.map(idx => Index(idx.singleKeyFields ::: sortIndexFields ::: idx.multiKeyFields))

    val customIndexesWithQueueAndSort = customIndexesWithSort.map(idx => Index(queueIndexFields ::: idx.fields))

    customIndexesWithSort ::: customIndexesWithQueueAndSort
  }
}
