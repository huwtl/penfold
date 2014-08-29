package com.qmetric.penfold.app.readstore.mongodb

import com.qmetric.penfold.readstore.Filters

case class Indexes(private val customIndexes: List[Index]) {
  private val statusField = IndexField("status", "status")

  private val sortIndexFields = List(IndexField("sort", "sort"), IndexField("_id", "_id"))

  private val queueIndexFields = List(IndexField("queue", "queue"), statusField)

  private val queueIndex = Index(None, queueIndexFields ::: sortIndexFields)

  private val statusIndex = Index(None, statusField :: sortIndexFields)

  val all = queueIndex :: statusIndex :: augmentCustomIndexes

  private val filterIndexFieldLookup = (for {
    index <- all
    field <- index.excludingSortFields
  } yield (field.alias, field.path)).toMap

  def buildQueryPlan(filters: Filters) = {
    suitableIndex(filters) match {
      case Some(suitableIndex) => {
        val restrictionFields = suitableIndex.excludingSortFields
          .filter(idx => filters.keys.contains(idx.alias))
          .map(field => {
            val filter = filters.get(field.alias).get
            RestrictionField(field.path, filter)
          })

        val sortFields = suitableIndex.fields
          .filter(field => sortIndexFields.contains(field) || filters.keys.contains(field.alias))
          .map(field => SortField(field.path))

        QueryPlan(restrictionFields, sortFields)
      }
      case None => {
        val restrictionFields = filters.all.map(f => RestrictionField(filterIndexFieldLookup.getOrElse(f.key, f.key), f))
        val sortFields = sortIndexFields.map(field => SortField(field.path))
        QueryPlan(restrictionFields, sortFields)
      }
    }
  }

  def suitableIndex(filters: Filters) = {
    all.find(_.suitableFor(filters))
  }

  private def augmentCustomIndexes = {
    val customIndexesWithSort = customIndexes.map(idx => Index(idx.name, idx.singleKeyFields ::: sortIndexFields ::: idx.multiKeyFields))

    val customIndexesWithQueueAndSort = customIndexesWithSort.map(idx => Index(idx.name.map(n => s"queue_$n"), queueIndexFields ::: idx.fields))

    customIndexesWithSort ::: customIndexesWithQueueAndSort
  }
}
