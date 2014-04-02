package org.huwtl.penfold.app.query.redis

import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.app.support.ListCombiner
import org.huwtl.penfold.app.support.json.JsonPathExtractor

class RedisKeyFactory(jsonExtractor: JsonPathExtractor) {
  def eventTrackerKey(name: String) = s"tracker:$name"

  def indexEventTrackerKey(index: Index) = s"${eventTrackerKey(s"index:${index.name}")}"

  def jobKey(aggregateId: AggregateId) = s"job:${aggregateId.value}"

  def indexedJobKey(index: Index, aggregateId: AggregateId) = s"${indexKeyPrefix(index)}:${jobKey(aggregateId)}"

  def indexKey(index: Index, indexValues: List[String]) = s"${indexKeyPrefix(index)}:${indexValues mkString ":"}"

  def indexKeys(index: Index, searchJson: String): List[String] = {
    val combinations = indexValueCombinations(index, searchJson)
    combinations.map(combination => indexKey(index, combination))
  }
  
  private def indexKeyPrefix(index: Index) = s"index:${index.name}"

  private def indexValueCombinations(index: Index, searchJson: String) = {
    val indexValues: List[List[String]] = for {
      definition <- index.fields
    } yield jsonExtractor.extract(searchJson, definition.path)

    ListCombiner.combine[String](indexValues)
  }
}
