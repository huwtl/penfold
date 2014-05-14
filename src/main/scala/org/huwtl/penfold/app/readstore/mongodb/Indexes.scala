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
    suitableIndex(filters) match {
      case Some(suitableIndex) => Filters(suitableIndex.excludingSortFields.map(field => {
        Filter(field.path, filters.get(field.alias).get.values)
      }))
      case None => filters
    }
  }

  def suitableIndex(filters: Filters) = {
    all.find(index => Index(index.excludingSortFields).suitableFor(filters))
  }

  private def augmentCustomIndexes = {
    val customIndexesWithSort = customIndexes.map(idx => Index(idx.singleKeyFields ::: sortIndexFields ::: idx.multiKeyFields))

    val customIndexesWithQueueAndSort = customIndexesWithSort.map(idx => Index(queueIndexFields ::: idx.fields))

    customIndexesWithSort ::: customIndexesWithQueueAndSort
  }
}
