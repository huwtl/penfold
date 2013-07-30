package com.hlewis.eventfire.domain

import org.joda.time.DateTime

case class Cron(second: String,
                minute: String,
                hour: String,
                dayOfMonth: String,
                month: String,
                dayOfWeek: String,
                year: String = "*") {

  private val cron = cronish.Cron(second, minute, hour, dayOfMonth, month, dayOfWeek, year)

  def nextExecutionDate = new DateTime(cron.nextTime.getTimeInMillis)

  override def toString = List(second, minute, hour, dayOfMonth, month, dayOfWeek, year) mkString " "
}
