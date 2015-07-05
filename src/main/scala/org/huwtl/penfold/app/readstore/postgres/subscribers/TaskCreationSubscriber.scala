package org.huwtl.penfold.app.readstore.postgres.subscribers

import org.huwtl.penfold.app.readstore.postgres.TaskData
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.event.Event

import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.{StaticQuery => Q}

trait TaskCreationSubscriber[E <: Event] extends Subscriber[E] {

  def createNewTask(task: TaskData, objectSerializer: ObjectSerializer) {
    val taskJson = objectSerializer.serialize(task)

    val alreadyExists = sql"""SELECT id FROM tasks WHERE id = ${task.id.value}""".as[String].firstOption
    if (!alreadyExists.isDefined) sqlu"""INSERT INTO tasks (id, data) VALUES (${task.id.value}, $taskJson::json)""".execute
  }
}
