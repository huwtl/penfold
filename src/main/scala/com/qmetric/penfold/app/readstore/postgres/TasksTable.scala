package com.qmetric.penfold.app.readstore.postgres

import com.qmetric.penfold.app.support.postgres.MyPostgresDriver

import scala.slick.jdbc.{StaticQuery => Q}
import com.github.tminglei.slickpg._

import MyPostgresDriver.simple._

import scala.slick.lifted

object TasksTable {
  case class TaskRow(id: String, json: JsonString)

  class TasksTable(tag: lifted.Tag) extends Table[TaskRow](tag, "tasks") {
    def id = column[String]("id", O.DBType("varchar(36)"), O.PrimaryKey)

    def data = column[JsonString]("data")

    def * = (id, data) <>(TaskRow.tupled, TaskRow.unapply)
  }

  val Tasks = TableQuery[TasksTable]
}
