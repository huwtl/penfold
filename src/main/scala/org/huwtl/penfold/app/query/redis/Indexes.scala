package org.huwtl.penfold.app.query.redis

import org.huwtl.penfold.query.Filters

case class Indexes(private val customIndexes: List[Index], keyFactory: RedisKeyFactory) {
  val queueIndex = Index("queue", List(IndexField("queue", "queues"), IndexField("status", "status")))

  val statusIndex = Index("status", List(IndexField("status", "status")))

  val all = queueIndex :: statusIndex :: augmentCustomIndexes

  def keyFor(filters: Filters): Option[String] = {
    def buildKeyName(index: Index, filters: Filters) = {
      val indexValues = for {
        field <- index.fields
        filter <- filters.get(field.key)
      } yield filter.value

      keyFactory.indexKey(index, indexValues).toLowerCase
    }

    val suitableIndex = all.find(_.suitableFor(filters))
    suitableIndex.map(index => buildKeyName(index, filters))
  }

  private def augmentCustomIndexes = {
    val enhancedCustomIndexes = for {
      customIndex <- customIndexes
      enhancement <- List(queueIndex, statusIndex)
    } yield Index(s"${customIndex.name}_${enhancement.name}", enhancement.fields ::: customIndex.fields)

    customIndexes ::: enhancedCustomIndexes
  }
}
