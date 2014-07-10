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
    val indexes = Indexes(List(Index(None, List(customIndexField)), Index(None, List(customMultiIndexField)), Index(None, List(customIndexField, customMultiIndexField))))

    indexes.all must containTheSameElementsAs(List(
      Index(None, List(queueField, statusField, sortField, sortIdField)),
      Index(None, List(statusField, sortField, sortIdField)),
      Index(None, List(customIndexField, sortField, sortIdField)),
      Index(None, List(sortField, sortIdField, customMultiIndexField)),
      Index(None, List(customIndexField, sortField, sortIdField, customMultiIndexField)),
      Index(None, List(queueField, statusField, customIndexField, sortField, sortIdField)),
      Index(None, List(queueField, statusField, sortField, sortIdField, customMultiIndexField)),
      Index(None, List(queueField, statusField, customIndexField, sortField, sortIdField, customMultiIndexField))
    ))
  }

  "retrieve suitable index for filters" in {
    val expectedIndexWithSort = Index(None, List(customIndexField, sortField, sortIdField, customMultiIndexField))
    val expectedIndexWithStatusQueue = Index(None, List(queueField, statusField, customIndexField, sortField, sortIdField, customMultiIndexField))

    val indexes = new Indexes(List(Index(None, List(customIndexField, customMultiIndexField))))

    indexes.suitableIndex(Filters(List(filter("f1"), filter("f2")))) must beEqualTo(Some(expectedIndexWithSort))
    indexes.suitableIndex(Filters(List(filter("f2"), filter("f1")))) must beEqualTo(Some(expectedIndexWithSort))
    indexes.suitableIndex(Filters(List(filter("status"), filter("f2"), filter("f1"), filter("queue")))) must beEqualTo(Some(expectedIndexWithStatusQueue))
    indexes.suitableIndex(Filters(List(filter("f2"), filter("f1"), filter("queue")))) must beNone
    indexes.suitableIndex(Filters(List(filter("f2")))) must beNone
    indexes.suitableIndex(Filters(List(filter("f1")))) must beEqualTo(Some(expectedIndexWithSort))
    indexes.suitableIndex(Filters(List(filter("f3")))) must beNone
    indexes.suitableIndex(Filters(Nil)) must beNone
  }

  "transform filters into a query plan by resolving index aliases and index field order" in {
    val indexes = new Indexes(List(Index(None, List(customIndexField, customMultiIndexField))))
    val expectedQueryPlan = QueryPlan(
      List(restriction("queue"), restriction("status"), restriction("payload.idx1"), restriction("payload.idxs1")),
      List(sortField("queue"), sortField("status"), sortField("payload.idx1"), sortField("sort"), sortField("_id"), sortField("payload.idxs1"))
    )
    val expectedQueryPlan2 = QueryPlan(
      List(restriction("queue"), restriction("status"), restriction("payload.idx1")),
      List(sortField("queue"), sortField("status"), sortField("payload.idx1"), sortField("sort"), sortField("_id"))
    )
    val expectedQueryPlan3 = QueryPlan(
      List(restriction("queue"), restriction("status"), restriction("f3")),
      List(sortField("sort"), sortField("_id"))
    )

    indexes.buildQueryPlan(Filters(List(filter("queue"), filter("status"), filter("f1"), filter("f2")))) must beEqualTo(expectedQueryPlan)
    indexes.buildQueryPlan(Filters(List(filter("queue"), filter("status"), filter("f1")))) must beEqualTo(expectedQueryPlan2)
    indexes.buildQueryPlan(Filters(List(filter("queue"), filter("status"), filter("f3")))) must beEqualTo(expectedQueryPlan3)
  }

  private def filter(key: String) = Filter(key, None)

  private def restriction(key: String) = RestrictionField(key, Set(None))

  private def sortField(key: String) = SortField(key)
}
