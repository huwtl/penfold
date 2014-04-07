package org.huwtl.penfold.app.readstore.redis

import org.specs2.mutable.Specification
import org.specs2.matcher.DataTables
import org.huwtl.penfold.readstore.{Filter, Filters}
import org.huwtl.penfold.app.support.json.JsonPathExtractor

class IndexesTest extends Specification with DataTables {
  val redisKeyFactory = new RedisKeyFactory(new JsonPathExtractor)

  "queue and status indexes added by default" in {
    val indexes = Indexes(Nil, redisKeyFactory)

    indexes.keyFor(Filters(List(Filter("status", "waitING")))).get must beEqualTo("index:status:waiting")
    indexes.keyFor(Filters(List(Filter("queue", "Q1"), Filter("status", "waitING")))).get must beEqualTo("index:queue:q1:waiting")
  }

  "augment custom indexes with queue and status" in {
    val indexes = Indexes(List(index("a", field("a"))), redisKeyFactory)

    "filters"                                                                     | "key"                           |
    List(Filter("a", "val1"))                                                     ! "index:a:val1"                  |
    List(Filter("a", "val1"), Filter("status", "waitING"))                        ! "index:a_status:waiting:val1"   |
    List(Filter("queue", "Q1"), Filter("a", "val1"), Filter("status", "waiting")) ! "index:a_queue:q1:waiting:val1" |> {
      (filters, key) => indexes.keyFor(Filters(filters)).get must beEqualTo(key)
    }
  }

  "create key for matching index" in {
    "indexes"                                                         | "filters"                                       | "key"                       |
    Nil                                                               ! Nil                                             ! None                        |
    List(index("a", field("a")))                                      ! List(Filter("b", "val2"))                       ! None                        |
    List(index("b", field("b")), index("ab", field("a"), field("b"))) ! List(Filter("b", "val2"), Filter("a", "val1"))  ! Some("index:ab:val1:val2")  |
    List(index("ab", field("a"), field("b")))                         ! List(Filter("b", "val2"), Filter("a", "val1"))  ! Some("index:ab:val1:val2")  |
    List(index("a", field("a")))                                      ! List(Filter("A", "vAL1"))                       ! Some("index:a:val1")        |
    List(index("a", field("a")))                                      ! List(Filter("a", "val1"))                       ! Some("index:a:val1")        |> {
      (indexes, filters, key) =>
        new Indexes(indexes, redisKeyFactory).keyFor(Filters(filters)) must beEqualTo(key)
    }
  }

  private def field(key: String) = IndexField(key, "")
  private def index(name: String, fields : IndexField*) = Index(name, fields.toList)
}
