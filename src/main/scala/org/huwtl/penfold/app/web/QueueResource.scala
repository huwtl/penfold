package org.huwtl.penfold.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import org.huwtl.penfold.app.support.hal.HalQueueFormatter
import org.huwtl.penfold.domain.model.{Status, AggregateId, QueueName}
import org.huwtl.penfold.query.{PageRequest, QueryRepository}
import org.huwtl.penfold.command.{CompleteJob, CommandDispatcher, StartJob}
import org.huwtl.penfold.app.support.json.ObjectSerializer

class QueueResource(queryRepository: QueryRepository,
                    commandDispatcher: CommandDispatcher,
                    jsonConverter: ObjectSerializer,
                    halFormatter: HalQueueFormatter) extends ScalatraServlet with FilterParamsProvider {

  private val pageSize = 10

  before() {
    contentType = HAL_JSON
  }

  get("/:queue/:status") {
    statusMatch {
      status => {
        val queue = QueueName(params("queue"))
        val page = PageRequest(params.getOrElse("page", "0").toInt, pageSize)
        val filters = parseFilters(params)
        Ok(halFormatter.halFrom(queue, status, queryRepository.retrieveBy(queue, status, page, filters), filters))
      }
    }
  }

  get("/:queue/:status/:id") {
    statusMatch {
      status => {
        queryRepository.retrieveBy(AggregateId(params("id"))) match {
          case Some(job) => Ok(halFormatter.halFrom(job))
          case _ => NotFound(s"$status job not found")
        }
      }
    }
  }

  post("/:queue/started") {
    val startJobCommand = jsonConverter.deserialize[StartJob](request.body)
    commandDispatcher.dispatch[StartJob](startJobCommand)
    Ok(halFormatter.halFrom(queryRepository.retrieveBy(startJobCommand.id).get))
  }

  post("/:queue/completed") {
    val completeJobCommand = jsonConverter.deserialize[CompleteJob](request.body)
    commandDispatcher.dispatch[CompleteJob](completeJobCommand)
    Ok(halFormatter.halFrom(queryRepository.retrieveBy(completeJobCommand.id).get))
  }

  private def statusMatch(func: Status => ActionResult) = {
    val statusValue = params("status")
    Status.from(statusValue) match {
      case Some(status) => func(status)
      case None => NotFound(s"unrecognised $statusValue type")
    }
  }
}
