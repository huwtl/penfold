package org.huwtl.penfold.domain

import org.joda.time.DateTime

case class Cron(cronStr: String) {
  val cronParts = cronStr.split(' ')

  private val cron = cronish.Cron(cronParts(0), cronParts(1), cronParts(2), cronParts(3), cronParts(4), cronParts(5), cronParts(6))

  def nextExecutionDate = new DateTime(cron.nextTime.getTimeInMillis)

  override def toString = cron.full
}
