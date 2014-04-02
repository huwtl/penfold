package org.huwtl.penfold.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import org.huwtl.penfold.app.support.hal.HalQueueFormatter
import org.huwtl.penfold.domain.model.{Status, AggregateId, QueueId}
import org.huwtl.penfold.query.{PageRequest, QueryRepository}
import org.huwtl.penfold.command.{CompleteJob, CommandDispatcher, StartJob}
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.app.web.bean.{CompleteJobRequest, StartJobRequest}

class QueueResource(queryRepository: QueryRepository,
                    commandDispatcher: CommandDispatcher,
                    jsonConverter: ObjectSerializer,
                    halFormatter: HalQueueFormatter) extends ScalatraServlet with FilterParamsProvider with ErrorHandling {

  private val pageSize = 10

  before() {
    contentType = HAL_JSON
  }

  get("/:queue/:status") {
    statusMatch {
      status => {
        val queue = QueueId(params("queue"))
        val page = PageRequest(params.getOrElse("page", "0").toInt, pageSize)
        val filters = parseFilters(params)
        Ok(halFormatter.halFrom(queue, status, queryRepository.retrieveByQueue(queue, status, page, filters), filters))
      }
    }
  }

  get("/:queue/:status/:id") {
    statusMatch {
      status => {
        queryRepository.retrieveBy(AggregateId(params("id"))) match {
          case Some(job) => Ok(halFormatter.halFrom(QueueId(queueIdParam), job))
          case _ => errorResponse(NotFound(s"$status job not found"))
        }
      }
    }
  }

  post("/:queue/started") {
    val queue = QueueId(queueIdParam)
    val startJobRequest = jsonConverter.deserialize[StartJobRequest](request.body)
    commandDispatcher.dispatch[StartJob](startJobRequest.toCommand(queue))
    Ok(halFormatter.halFrom(QueueId(queueIdParam), queryRepository.retrieveBy(startJobRequest.id).get))
  }

  post("/:queue/completed") {
    val queue = QueueId(queueIdParam)
    val completeJobRequest = jsonConverter.deserialize[CompleteJobRequest](request.body)
    commandDispatcher.dispatch[CompleteJob](completeJobRequest.toCommand(queue))
    Ok(halFormatter.halFrom(QueueId(queueIdParam), queryRepository.retrieveBy(completeJobRequest.id).get))
  }

  private def statusMatch(func: Status => ActionResult) = {
    val statusValue = params("status")
    Status.from(statusValue) match {
      case Some(status) => func(status)
      case None => errorResponse(BadRequest(s"unrecognised $statusValue status"))
    }
  }

  private def queueIdParam = params("queue")
}
