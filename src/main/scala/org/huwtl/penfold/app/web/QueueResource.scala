package org.huwtl.penfold.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import org.huwtl.penfold.app.support.hal.HalQueueFormatter
import org.huwtl.penfold.domain.model.{Status, AggregateId, QueueId}
import org.huwtl.penfold.readstore.ReadStore
import org.huwtl.penfold.command.{CompleteTask, CommandDispatcher, StartTask}
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.app.web.bean.{CompleteTaskRequest, StartTaskRequest}
import org.huwtl.penfold.app.support.auth.BasicAuthenticationSupport
import org.huwtl.penfold.app.AuthenticationCredentials

class QueueResource(readStore: ReadStore,
                    commandDispatcher: CommandDispatcher,
                    jsonConverter: ObjectSerializer,
                    halFormatter: HalQueueFormatter,
                    pageSize: Int,
                    authenticationCredentials: Option[AuthenticationCredentials]) extends ScalatraServlet with FilterParamsProvider with PageRequestProvider with ErrorHandling with BasicAuthenticationSupport {

  before() {
    contentType = HAL_JSON
  }

  get("/:queue/:status") {
    statusMatch {
      status => {
        val queue = QueueId(params("queue"))
        val page = parsePageRequestParams(params, pageSize)
        val filters = parseFilters(multiParams)
        Ok(halFormatter.halFrom(queue, status, page, readStore.retrieveByQueue(queue, status, page, filters), filters))
      }
    }
  }

  get("/:queue/:status/:id") {
    statusMatch {
      status => {
        readStore.retrieveBy(AggregateId(params("id"))) match {
          case Some(task) => Ok(halFormatter.halFrom(QueueId(queueIdParam), task))
          case None => errorResponse(NotFound(s"$status task not found"))
        }
      }
    }
  }

  post("/:queue/started") {
    val startTaskRequest = jsonConverter.deserialize[StartTaskRequest](request.body)
    commandDispatcher.dispatch[StartTask](startTaskRequest.toCommand)
    Ok(halFormatter.halFrom(QueueId(queueIdParam), readStore.retrieveBy(startTaskRequest.id).get))
  }

  post("/:queue/completed") {
    val completeTaskRequest = jsonConverter.deserialize[CompleteTaskRequest](request.body)
    commandDispatcher.dispatch[CompleteTask](completeTaskRequest.toCommand)
    Ok(halFormatter.halFrom(QueueId(queueIdParam), readStore.retrieveBy(completeTaskRequest.id).get))
  }

  override protected def validCredentials: Option[AuthenticationCredentials] = authenticationCredentials

  private def statusMatch(func: Status => ActionResult) = {
    val statusValue = params("status")
    Status.from(statusValue) match {
      case Some(status) => func(status)
      case None => errorResponse(BadRequest(s"unrecognised $statusValue status"))
    }
  }

  private def queueIdParam = params("queue")
}
