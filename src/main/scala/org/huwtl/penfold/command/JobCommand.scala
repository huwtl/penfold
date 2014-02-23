package org.huwtl.penfold.command

import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.{Payload, JobType, Id}

sealed trait JobCommand extends Command

case class CreateJob(id: Id,
                     jobType: JobType,
                     payload: Payload) extends JobCommand


case class CreateFutureJob(id: Id,
                           jobType: JobType,
                           triggerDate: DateTime,
                           payload: Payload) extends JobCommand

case class TriggerJob(id: Id) extends JobCommand

case class StartJob(id: Id) extends JobCommand

case class CompleteJob(id: Id) extends JobCommand

case class CancelJob(id: Id) extends JobCommand
