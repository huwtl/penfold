package org.huwtl.penfold.app.query

import org.huwtl.penfold.domain.model.{Status, QueueName, AggregateId}
import org.huwtl.penfold.app.support.ListCombiner
import org.huwtl.penfold.app.support.json.JsonPathExtractor

class RedisKeyFactory(jsonExtractor: JsonPathExtractor) {
  def eventTrackerKey(name: String) = s"tracker:$name"

  def indexEventTrackerKey(index: Index) = s"${eventTrackerKey(s"index:${index.name}")}"

  def jobKey(aggregateId: AggregateId) = s"job:${aggregateId.value}"

  def statusKey(status: Status) = s"${status.name}"

  def queueKey(queue: QueueName, status: Status) = s"${queue.value}:${status.name}"

  def indexKeyPrefix(index: Index) = s"index:${index.name}"

  def indexJobKey(index: Index, aggregateId: AggregateId) = s"${indexKeyPrefix(index)}:${jobKey(aggregateId)}"

  def allJobsIndexKey(index: Index, indexValues: List[String]) = s"${indexKeyPrefix(index)}:${indexValues mkString ":"}"

  def allJobsIndexKeys(index: Index, payloadJson: String) = {
    val allIndexValueCombinations = indexValueCombinations(index, payloadJson)

    allIndexValueCombinations.map(indexValueCombination =>
      allJobsIndexKey(index, indexValueCombination)
    )
  }

  def indexQueueKey(queue: QueueName, status: Status, allJobsIndexKey: String) = s"${allJobsIndexKey}:${queueKey(queue, status)}"

  private def indexValueCombinations(index: Index, payloadJson: String) = {
    val indexValues: List[List[String]] = for {
      definition <- index.fields
    } yield jsonExtractor.extract(payloadJson, definition.path)

    ListCombiner.combine[String](indexValues)
  }
}
