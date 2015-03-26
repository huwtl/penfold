package org.huwtl.penfold.app.web

import org.scalatra._
import com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import org.huwtl.penfold.app.support.hal.HalQueueFormatter
import org.huwtl.penfold.domain.model.{Status, AggregateId, QueueId}
import org.huwtl.penfold.readstore.{SortOrderMapping, ReadStore}
import org.huwtl.penfold.app.support.auth.BasicAuthenticationSupport
import org.huwtl.penfold.app.AuthenticationCredentials

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
