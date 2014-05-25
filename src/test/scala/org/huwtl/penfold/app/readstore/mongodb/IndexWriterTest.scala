package org.huwtl.penfold.app.readstore.mongodb

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.mongodb.casbah.Imports._
import org.huwtl.penfold.app.ServerConfiguration
import org.huwtl.penfold.app.TaskArchiverConfiguration
import org.specs2.specification.Scope

class IndexWriterTest extends Specification with Mockito {

  class context extends Scope {
    val indexWriter = new IndexWriter()
    val mongo = mock[MongoDB]
    val mongoCollection = mock[MongoCollection]
    mongo("tasks") returns mongoCollection
  }

  "created indexes" in new context {
    val indexes = mock[Indexes]
    indexes.all returns List(Index(List(IndexField("test", "payload.test"))))

    indexWriter.write(mongo, indexes, new ServerConfiguration(null, 0, None, null, null, null, 0, null, Some(TaskArchiverConfiguration("payload.timeout", null))))

    there was one(mongoCollection).ensureIndex(MongoDBObject("payload.test" -> 1), MongoDBObject("background" -> true))
    there was one(mongoCollection).ensureIndex(MongoDBObject("payload.timeout" -> 1), MongoDBObject("background" -> true))
  }
}
