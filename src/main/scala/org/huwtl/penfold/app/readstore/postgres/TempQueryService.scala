package org.huwtl.penfold.app.readstore.postgres

import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession

import scala.slick.jdbc.{StaticQuery => Q}
import com.github.tminglei.slickpg._

import MyPostgresDriver.simple._

import scala.slick.lifted

class TempQueryService(database: Database) {

  case class TaskBean(id: String, data: JsonString)

  class TasksTable(tag: lifted.Tag) extends Table[TaskBean](tag, "tasks") {
    def id = column[String]("id", O.DBType("varchar(36)"), O.PrimaryKey)

    def data = column[JsonString]("data")

    def * = (id, data) <>(TaskBean.tupled, TaskBean.unapply)
  }

  val Tasks = TableQuery[TasksTable]

  def get: (String, Option[String]) = {
    database.withDynSession {
      val results = Tasks.filter(_.id === "101".bind).map(_.data)

      val result = results.firstOption.map(_.value)
      (results.selectStatement, result)
    }
  }

  def get2: (String, Option[String]) = {
    database.withDynSession {

      val q = Tasks.filter(_.data.+>>("a").asColumnOf[Long] === 101.toLong.bind)

      val results = List("a", "b").foldLeft(q)((query, param) => {
        query.filter(row => (row.data.+>("a").+>>("b").asColumnOf[Long] === 101.toLong.bind) || (row.data.+>>("a").asColumnOf[Long] === 102.toLong.bind))
        //query.filter(_.data.+>>("a") === "101".bind)
      }).map(_.data)

      val result = results.firstOption.map(_.value)
      (results.selectStatement, result)
    }
  }
}