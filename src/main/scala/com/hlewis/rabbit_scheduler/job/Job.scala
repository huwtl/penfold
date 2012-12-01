package com.hlewis.rabbit_scheduler.job

import cronish.Cron

case class Job(header: Header, body: Body)

case class Header(reference: String, jobType: String, cron: Cron, dispatch: Map[String, String])

case class Body(data: Map[String, Any])
