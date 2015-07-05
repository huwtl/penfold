package org.huwtl.penfold.app.support.postgres

import com.github.tminglei.slickpg._

import scala.slick.driver.PostgresDriver

trait MyPostgresDriver extends PostgresDriver
with PgArraySupport
with PgDateSupport
with PgRangeSupport
with PgJsonSupport
with PgHStoreSupport
with PgSearchSupport {
  override lazy val Implicit = new ImplicitsPlus {}
  override val simple = new SimpleQLPlus {}

  trait ImplicitsPlus extends Implicits
  with ArrayImplicits
  with DateTimeImplicits
  with RangeImplicits
  with HStoreImplicits
  with JsonImplicits
  with SearchImplicits
  trait SimpleQLPlus extends SimpleQL
  with ImplicitsPlus
  with SearchAssistants
}

object MyPostgresDriver extends MyPostgresDriver{
  override def pgjson = "json"
}