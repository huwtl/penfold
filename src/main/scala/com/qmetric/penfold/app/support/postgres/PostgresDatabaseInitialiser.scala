package com.qmetric.penfold.app.support.postgres

import javax.sql.DataSource

import com.googlecode.flyway.core.Flyway

import scala.slick.driver.JdbcDriver.backend.Database

class PostgresDatabaseInitialiser(customDbMigrationPath: Option[CustomDbMigrationPath], flyway: Flyway = new Flyway) {
  val defaultLocations = List("db/migration/eventstore", "db/migration/readstore")

  def init(dataSource: DataSource) = {
    val locations = customDbMigrationPath match {
      case Some(customPath) => customPath.migrationPath :: defaultLocations
      case None => defaultLocations
    }

    flyway.setLocations(locations:_*)
    flyway.setDataSource(dataSource)
    flyway.migrate()

    Database.forDataSource(dataSource)
  }
}

case class CustomDbMigrationPath(private val path: String) {
  val migrationPath = s"filesystem:$path"
}