package com.qmetric.penfold.app.readstore.postgres.subscribers

import com.qmetric.penfold.app.support.json.ObjectSerializer
import com.qmetric.penfold.domain.event.{Event, TaskArchived}

import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.{StaticQuery => Q}

class TaskArchivedSubscriber extends Subscriber[TaskArchived] {

  override def applicable(event: Event) = event.isInstanceOf[TaskArchived]

  override def handleEvent(event: TaskArchived, objectSerializer: ObjectSerializer) {
    sqlu"""INSERT INTO archived (id, data) (SELECT t.id, t.data FROM tasks t WHERE t.id = ${event.aggregateId.value})""".execute
    sqlu"""DELETE FROM tasks WHERE id = ${event.aggregateId.value} AND (data->>'version')::bigint = ${event.aggregateVersion.previous.number}""".execute
  }
}
