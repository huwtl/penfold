package org.huwtl.penfold.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import org.huwtl.penfold.app.support.hal.HalQueueFormatter
import org.huwtl.penfold.domain.model.{Status, Id, QueueName}
import org.huwtl.penfold.query.QueryRepository
import org.huwtl.penfold.command.{CompleteJob, CommandDispatcher, StartJob}
import org.huwtl.penfold.app.support.json.ObjectSerializer

class QueueResource(queryRepository: QueryRepository,
                    commandDispatcher: CommandDispatcher,
                    jsonConverter: ObjectSerializer,
                    halFormatter: HalQueueFormatter) extends ScalatraServlet {

  before() {
    contentType = HAL_JSON
  }

  get("/queues/:queue/:status") {
    statusMatch {
      status => {
        val queue = QueueName(params.get("queue").get)
        Ok(halFormatter.halFrom(queue, status, queryRepository.retrieveBy(status, queue)
        ))
      }
    }
  }

  get("/queues/:queue/:status/:id") {
    statusMatch {
      status => {
        queryRepository.retrieveBy(Id(params("id"))) match {
          case Some(job) => Ok(halFormatter.halFrom(job))
          case _ => NotFound(s"$status job not found")
        }
      }
    }
  }

  post("/queues/:queue/started") {
    val startJobCommand = jsonConverter.deserialize[StartJob](request.body)
    commandDispatcher.dispatch[StartJob](startJobCommand)
    Created(halFormatter.halFrom(queryRepository.retrieveBy(startJobCommand.id).get))
  }

  post("/queues/:queue/completed") {
    val completeJobCommand = jsonConverter.deserialize[CompleteJob](request.body)
    commandDispatcher.dispatch[CompleteJob](completeJobCommand)
    Created(halFormatter.halFrom(queryRepository.retrieveBy(completeJobCommand.id).get))
  }

  private def statusMatch(func: Status => ActionResult) = {
    val statusValue = params.get("status").get
    Status.from(statusValue) match {
      case Some(status) => func(status)
      case None => NotFound(s"unrecognised $statusValue type")
    }
  }
}
