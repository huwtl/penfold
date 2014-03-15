package org.huwtl.penfold.app.query

import org.specs2.mutable.Specification
import org.specs2.matcher.DataTables
import org.huwtl.penfold.query.{Filter, Filters}
import org.huwtl.penfold.domain.model.{Status, QueueName}
import org.huwtl.penfold.app.support.json.JsonPathExtractor

class IndexesTest extends Specification with DataTables {

  val redisKeyFactory = new RedisKeyFactory(new JsonPathExtractor)

  "create key for matching index" in {
    "indexes"                                                         | "filters"                                       | "key"                       |
    Nil                                                               ! Nil                                             ! None                        |
    List(index("a", field("a")))                                      ! List(Filter("b", "val2"))                       ! None                        |
    List(index("b", field("b")), index("ab", field("a"), field("b"))) ! List(Filter("b", "val2"), Filter("a", "val1"))  ! Some("index:ab:val1:val2") |
    List(index("ab", field("a"), field("b")))                         ! List(Filter("b", "val2"), Filter("a", "val1"))  ! Some("index:ab:val1:val2") |
    List(index("a", field("a")))                                      ! List(Filter("A", "vAL1"))                       ! Some("index:a:val1")        |
    List(index("a", field("a")))                                      ! List(Filter("a", "val1"))                       ! Some("index:a:val1")        |> {
      (indexes, filters, key) =>
        new Indexes(indexes, redisKeyFactory).keyFor(Filters(filters)) must beEqualTo(key)
    }
  }

  "create key for matching index" in {
    val indexes = new Indexes(List(index("a", field("a"))), redisKeyFactory)
    val filters = Filters(List(Filter("a", "val1")))

    indexes.keyFor(filters, QueueName("q1"), Status.Waiting) must beEqualTo(Some("index:a:val1:q1:waiting"))
  }

  private def field(key: String) = IndexField(key, "")
  private def index(name: String, fields : IndexField*) = Index(name, fields.toList)
}
