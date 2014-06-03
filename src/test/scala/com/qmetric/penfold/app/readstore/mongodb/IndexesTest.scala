package com.qmetric.penfold.app.readstore.mongodb

import org.specs2.mutable.Specification
import com.qmetric.penfold.readstore.{Filter, Filters}

class IndexesTest extends Specification {
  val queueField = IndexField("queue", "queue")
  val statusField = IndexField("status", "status")
  val sortField = IndexField("sort", "sort")
  val sortIdField = IndexField("_id", "_id")
  val customIndexField = IndexField("f1", "payload.idx1")
  val customMultiIndexField = IndexField("f2", "payload.idxs1", multiKey = true)

  "create indexes" in {
    val indexes = Indexes(List(Index(List(customIndexField)), Index(List(customMultiIndexField)), Index(List(customIndexField, customMultiIndexField))))

    indexes.all must containTheSameElementsAs(List(
      Index(List(queueField, statusField, sortField, sortIdField)),
      Index(List(statusField, sortField, sortIdField)),
      Index(List(customIndexField, sortField, sortIdField)),
      Index(List(sortField, sortIdField, customMultiIndexField)),
      Index(List(customIndexField, sortField, sortIdField, customMultiIndexField)),
      Index(List(queueField, statusField, customIndexField, sortField, sortIdField)),
      Index(List(queueField, statusField, sortField, sortIdField, customMultiIndexField)),
      Index(List(queueField, statusField, customIndexField, sortField, sortIdField, customMultiIndexField))
    ))
  }

  "retrieve suitable index for filter" in {
    val expectedIndexWithSort = Index(List(customIndexField, sortField, sortIdField, customMultiIndexField))
    val expectedIndexWithStatusQueue = Index(List(queueField, statusField, customIndexField, sortField, sortIdField, customMultiIndexField))

    val indexes = new Indexes(List(Index(List(customIndexField, customMultiIndexField))))

    indexes.suitableIndex(Filters(List(filter("f1"), filter("f2")))) must beEqualTo(Some(expectedIndexWithSort))
    indexes.suitableIndex(Filters(List(filter("f2"), filter("f1")))) must beEqualTo(Some(expectedIndexWithSort))
    indexes.suitableIndex(Filters(List(filter("status"), filter("f2"), filter("f1"), filter("queue")))) must beEqualTo(Some(expectedIndexWithStatusQueue))
    indexes.suitableIndex(Filters(List(filter("f2"), filter("f1"), filter("queue")))) must beNone
    indexes.suitableIndex(Filters(List(filter("f2")))) must beNone
    indexes.suitableIndex(Filters(List(filter("f1")))) must beNone
    indexes.suitableIndex(Filters(List(filter("f3")))) must beNone
    indexes.suitableIndex(Filters(Nil)) must beNone
  }

  "transform filters by resoving index aliases and index field order" in {
    val indexes = new Indexes(List(Index(List(customIndexField, customMultiIndexField))))
    val expectedTransformedFilters = Filters(List(filter("queue"), filter("status"), filter("payload.idx1"), filter("payload.idxs1")))

    indexes.transformForSuitableIndex(Filters(List(filter("queue"), filter("status"), filter("f1"), filter("f2")))) must beEqualTo(expectedTransformedFilters)
    indexes.transformForSuitableIndex(Filters(List(filter("queue"), filter("status"), filter("f1"), filter("f2")))) must beEqualTo(expectedTransformedFilters)
    indexes.transformForSuitableIndex(Filters(List(filter("queue"), filter("status"), filter("f1")))) must beEqualTo(Filters(List(filter("queue"), filter("status"), filter("f1"))))
    indexes.transformForSuitableIndex(Filters(List(filter("queue"), filter("status"), filter("f3")))) must beEqualTo(Filters(List(filter("queue"), filter("status"), filter("f3"))))
  }

  private def filter(key: String) = Filter(key, None)
}
