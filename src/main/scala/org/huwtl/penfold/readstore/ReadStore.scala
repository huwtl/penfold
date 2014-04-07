package org.huwtl.penfold.readstore

import org.huwtl.penfold.domain.model.Status
import org.huwtl.penfold.domain.model.QueueId
import org.huwtl.penfold.domain.model.AggregateId

trait ReadStore {
  def checkConnectivity: Either[Boolean, Exception]

  def retrieveBy(id: AggregateId): Option[JobRecord]

  def retrieveBy(filters: Filters, pageRequest: PageRequest): PageResult
  
  def retrieveByQueue(queueId: QueueId, status: Status, pageRequest: PageRequest, filters: Filters = Filters.empty): PageResult

  def retrieveByStatus(status: Status, pageRequest: PageRequest, filters: Filters = Filters.empty): PageResult

  def retrieveJobsToTrigger: Stream[JobRecordReference] = {
    val pageSize = 50

    def allPagesOfJobsToTrigger(pageRequest: PageRequest): Stream[List[JobRecordReference]] = {
      val page = retrieveNextPageOfJobsToTrigger(pageRequest)
      if (page.isEmpty) Stream.empty else page #:: allPagesOfJobsToTrigger(pageRequest.nextPage)
    }

    val allJobsToTrigger = for {
      pageOfJobsToTrigger <- allPagesOfJobsToTrigger(new PageRequest(0, pageSize))
      jobToTrigger <- pageOfJobsToTrigger
    } yield jobToTrigger

    allJobsToTrigger
  }

  protected def retrieveNextPageOfJobsToTrigger(pageRequest: PageRequest): List[JobRecordReference]
}
