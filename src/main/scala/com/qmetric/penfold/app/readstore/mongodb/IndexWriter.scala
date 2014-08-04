package com.qmetric.penfold.app.readstore.mongodb

import com.mongodb.casbah.Imports._
import com.qmetric.penfold.app.ServerConfiguration

class IndexWriter {

  def write(readStoreDatabase: MongoDB, indexes: Indexes, config: ServerConfiguration) {
    indexes.all.foreach(index => createReadStoreIndex(readStoreDatabase, index.name, index.fields.map(_.path)))

    if (config.taskArchiver.isDefined) {
      createReadStoreIndex(readStoreDatabase, s"payload.${config.taskArchiver.get.timeoutPayloadPath}")
    }

    if (config.readyTaskAssignmentTimeout.isDefined) {
      createReadStoreIndex(readStoreDatabase, None, List("status", s"payload.${config.readyTaskAssignmentTimeout.get.timeoutPayloadPath}"))
    }
  }

  private def createReadStoreIndex(dbConnection: MongoDB, attributePath: String) {
    createReadStoreIndex(dbConnection, None, attributePath :: Nil)
  }

  private def createReadStoreIndex(dbConnection: MongoDB, name: Option[String], attributePaths: List[String]) {
    val options = MongoDBObject("background" -> true) ++ (if (name.isDefined) MongoDBObject("name" -> name.get) else MongoDBObject())
    dbConnection("tasks").ensureIndex(MongoDBObject(attributePaths.map(_ -> 1)), options)
  }
}
