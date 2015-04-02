package com.qmetric.penfold.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import com.qmetric.penfold.app.support.hal.HalQueueFormatter
import com.qmetric.penfold.domain.model.{Status, AggregateId, QueueId}
import com.qmetric.penfold.readstore.{SortOrderMapping, ReadStore}
import com.qmetric.penfold.app.support.auth.BasicAuthenticationSupport
import com.qmetric.penfold.app.AuthenticationCredentials

class QueueResource(readStore: ReadStore,
                    halFormatter: HalQueueFormatter,
                    sortOrderMapping: SortOrderMapping,
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
        val sortOrder = sortOrderMapping.sortOrderFor(status)
        Ok(halFormatter.halFrom(queue, status, page, readStore.retrieveByQueue(queue, status, page, sortOrder, filters), filters))
      }
    }
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
