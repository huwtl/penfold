package org.huwtl.penfold.app.readstore.mongodb

import org.specs2.mutable.Specification

class IndexesTest extends Specification {
  "create indexes" in {
    val customIndexField = IndexField("idx1", "payload.idx1")
    val indexes = Indexes(List(Index(List(customIndexField))))

    indexes.all must beEqualTo(List(
      Index(List(IndexField("queue", "queue"), IndexField("status", "status"), IndexField("sort", "sort"), IndexField("_id", "_id"))),
      Index(List(IndexField("status", "status"), IndexField("sort", "sort"), IndexField("_id", "_id"))),
      Index(List(customIndexField, IndexField("sort", "sort"), IndexField("_id", "_id"))),
      Index(List(IndexField("queue", "queue"), IndexField("status", "status"), customIndexField, IndexField("sort", "sort"), IndexField("_id", "_id")))
    ))
  }
}
