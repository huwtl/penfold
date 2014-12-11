package org.huwtl.penfold.app.readstore.mongodb

import com.mongodb.casbah.Imports._
import org.huwtl.penfold.domain.model._
import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.readstore.{TaskRecordReference, TaskRecord, PreviousStatus}
import org.huwtl.penfold.domain.model.User
import scala.Some
import org.huwtl.penfold.domain.model.QueueBinding
import org.huwtl.penfold.domain.model.QueueId
import com.mongodb.util.JSON
import org.huwtl.penfold.app.support.json.ObjectSerializer

class MongoTaskMapper(objectSerializer: ObjectSerializer) {

  def mapDocumentToTask(document: MongoDBObject) = {
    def parsePreviousStatus = document.getAs[Map[String, Any]]("previousStatus") match {
      case Some(previousStatus) => objectSerializer.deserialize[Option[PreviousStatus]](objectSerializer.serialize(previousStatus))
      case None => None
    }

    TaskRecord(
      idFrom(document),
      versionFrom(document),
      document.as[DateTime]("created"),
      QueueBinding(QueueId(document.as[String]("queue"))),
      Status.from(document.as[String]("status")).get,
      document.as[DateTime]("statusLastModified"),
      parsePreviousStatus,
      document.getAs[String]("assignee").map(User),
      document.as[DateTime]("triggerDate"),
      document.as[Long]("score"),
      document.as[Long]("sort"),
      objectSerializer.deserialize[Payload](JSON.serialize(document("payload"))),
      document.getAs[String]("rescheduleType"),
      document.getAs[String]("conclusionType")
    )
  }

  def mapDocumentToTaskReference(document: MongoDBObject) = {
    TaskRecordReference(idFrom(document), versionFrom(document))
  }

  private def idFrom(document: MongoDBObject) = AggregateId(document.as[String]("_id"))

  private def versionFrom(document: MongoDBObject) = AggregateVersion(document.as[Int]("version"))
}
