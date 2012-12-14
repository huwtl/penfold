package com.hlewis.rabbit_scheduler.domain

import org.joda.time.DateTime

case class Cron(minute: String,
                hour: String,
                dayOfMonth: String,
                month: String,
                dayOfWeek: String,
                year: String = "*") {

  private val cron = cronish.Cron("0", minute, hour, dayOfMonth, month, dayOfWeek, year)

  def nextExecutionDate = new DateTime(cron.nextTime.getTimeInMillis)

  override def toString = List(minute, hour, dayOfMonth, month, dayOfWeek, year) mkString (" ")
}
