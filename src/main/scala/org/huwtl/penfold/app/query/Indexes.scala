package org.huwtl.penfold.app.query

import org.huwtl.penfold.query.Filters
import org.huwtl.penfold.domain.model.{Status, QueueName}

case class Indexes(all: List[Index], keyFactory: RedisKeyFactory) {
  def keyFor(filters: Filters, queue: QueueName, status: Status): Option[String] = {
    keyFor(filters).map(k => keyFactory.indexQueueKey(queue, status, k))
  }

  def keyFor(filters: Filters): Option[String] = {
    def buildKeyName(index: Index, filters: Filters) = {
      val indexValues = for {
        field <- index.fields
        filter <- filters.get(field.key)
      } yield filter.value

      keyFactory.allJobsIndexKey(index, indexValues).toLowerCase
    }

    val suitableIndex = all.find(_.suitableFor(filters))
    suitableIndex.map(index => buildKeyName(index, filters))
  }
}
