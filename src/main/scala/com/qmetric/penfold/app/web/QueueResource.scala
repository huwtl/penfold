package com.qmetric.penfold.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import com.qmetric.penfold.app.support.hal.HalQueueFormatter
import com.qmetric.penfold.domain.model.{Status, AggregateId, QueueId}
import com.qmetric.penfold.readstore.ReadStore
import com.qmetric.penfold.command.CommandDispatcher
import com.qmetric.penfold.app.support.json.ObjectSerializer
import com.qmetric.penfold.app.web.bean.{RequeueTaskRequest, CloseTaskRequest, StartTaskRequest}
import com.qmetric.penfold.app.support.auth.BasicAuthenticationSupport
import com.qmetric.penfold.app.AuthenticationCredentials

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
    commandDispatcher.dispatch(startTaskRequest.toCommand)
    Created(halFormatter.halFrom(QueueId(queueIdParam), readStore.retrieveBy(startTaskRequest.id).get))
  }

  post("/:queue/ready") {
    val requeueTaskRequest = jsonConverter.deserialize[RequeueTaskRequest](request.body)
    commandDispatcher.dispatch(requeueTaskRequest.toCommand)
    Created(halFormatter.halFrom(QueueId(queueIdParam), readStore.retrieveBy(requeueTaskRequest.id).get))
  }

  post("/:queue/closed") {
    val closeTaskRequest = jsonConverter.deserialize[CloseTaskRequest](request.body)
    commandDispatcher.dispatch(closeTaskRequest.toCommand)
    Created(halFormatter.halFrom(QueueId(queueIdParam), readStore.retrieveBy(closeTaskRequest.id).get))
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
