package org.huwtl.penfold.app.readstore.mongodb

import com.mongodb.casbah.Imports._
import org.huwtl.penfold.app.ServerConfiguration

class IndexWriter {

  def write(readStoreDatabase: MongoDB, indexes: Indexes, config: ServerConfiguration) {
    indexes.all.map(index => createReadStoreIndex(readStoreDatabase, index.fields.map(_.path)))

    if (config.taskArchiver.isDefined) {
      createReadStoreIndex(readStoreDatabase, config.taskArchiver.get.timeoutAttributePath)
    }
  }

  private def createReadStoreIndex(dbConnection: MongoDB, attributePath: String) {
    createReadStoreIndex(dbConnection, attributePath :: Nil)
  }

  private def createReadStoreIndex(dbConnection: MongoDB, attributePaths: List[String]) {
    dbConnection("tasks").ensureIndex(MongoDBObject(attributePaths.map(_ -> 1)), MongoDBObject("background" -> true))
  }
}
