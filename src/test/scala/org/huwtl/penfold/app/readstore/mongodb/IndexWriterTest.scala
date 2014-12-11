package org.huwtl.penfold.app.readstore.mongodb

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.mongodb.casbah.Imports._
import org.huwtl.penfold.app.{TaskAssignmentTimeoutConfiguration, ServerConfiguration, TaskArchiverConfiguration}
import scala.Some
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
    indexes.all returns List(Index(None, List(IndexField("test", "payload.test"))), Index(Some("indexName"), List(IndexField("test", "payload.test2"))))

    indexWriter.write(mongo, indexes, new ServerConfiguration(null, 0, None, null, null, null, null, 0, null, null, Some(TaskArchiverConfiguration("archiveTimeout")), Some(TaskAssignmentTimeoutConfiguration("assignmentTimeout"))))

    there was one(mongoCollection).ensureIndex(MongoDBObject("payload.test" -> 1), MongoDBObject("background" -> true))
    there was one(mongoCollection).ensureIndex(MongoDBObject("payload.test2" -> 1), MongoDBObject("background" -> true, "name" -> "indexName"))
    there was one(mongoCollection).ensureIndex(MongoDBObject("payload.archiveTimeout" -> 1), MongoDBObject("background" -> true))
    there was one(mongoCollection).ensureIndex(MongoDBObject("status" -> 1) ++ MongoDBObject("payload.assignmentTimeout" -> 1), MongoDBObject("background" -> true))
  }
}
