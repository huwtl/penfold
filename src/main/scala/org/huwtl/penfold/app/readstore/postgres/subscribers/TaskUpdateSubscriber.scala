package org.huwtl.penfold.app.readstore.postgres.subscribers

import org.huwtl.penfold.app.readstore.postgres.{PreviousStatus, TaskData}
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.event.Event
import org.huwtl.penfold.domain.model.patch.Patch
import org.huwtl.penfold.domain.model.{AggregateId, AggregateVersion, Payload}

import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.{StaticQuery => Q}

trait TaskUpdateSubscriber[E <: Event] extends Subscriber[E] {
  override def handleEvent(event: E, objectSerializer: ObjectSerializer) = {
    existing(event.aggregateId, event.aggregateVersion, objectSerializer) match {
      case Some(task) =>
        val defaultsApplied = task.copy(version = event.aggregateVersion, rescheduleReason = None, closeReason = None, closeResultType = None, cancelReason = None)
        val updatedTaskJson = objectSerializer.serialize(handleUpdateEvent(event, defaultsApplied))
        sqlu"""UPDATE tasks SET data = $updatedTaskJson::json WHERE id = ${event.aggregateId.value} AND (data->>'version')::bigint = ${event.aggregateVersion.previous.number}""".execute
      case None =>
    }
  }

  def handleUpdateEvent(event: E, existingTask: TaskData): TaskData

  def updatePreviousStatus(existingTask: TaskData) = {
    PreviousStatus(existingTask.status, existingTask.statusLastModified)
  }

  def patchPayloadIfExists(existingTask: TaskData, payloadUpdate: Option[Patch]): Payload = {
    payloadUpdate.map(update => Payload(update.exec(existingTask.payload.content))).getOrElse(existingTask.payload)
  }

  private def existing(id: AggregateId, version: AggregateVersion, objectSerializer: ObjectSerializer) = {
    val json = sql"""SELECT data FROM tasks WHERE id = ${id.value} AND (data->>'version')::bigint = ${version.previous.number}""".as[String].firstOption
    json.map(objectSerializer.deserialize[TaskData])
  }
}
