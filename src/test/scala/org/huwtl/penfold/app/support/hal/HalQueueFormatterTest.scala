package org.huwtl.penfold.app.support.hal

import java.net.URI
import scala.io.Source._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.specs2.mutable.Specification
import org.huwtl.penfold.domain.model._
import org.huwtl.penfold.readstore._
import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.readstore.Filter
import org.huwtl.penfold.domain.model.QueueId
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.readstore.JobRecord
import org.huwtl.penfold.readstore.PageResult

class HalQueueFormatterTest extends Specification {

  val createdDate = new DateTime(2014, 2, 25, 13, 0, 0, 0)

  val triggerDate = new DateTime(2014, 2, 25, 14, 0, 0, 0)

  val filters = Filters(List(Filter("data", "value")))

  val queueId = QueueId("abc")

  val status = Status.Ready

  val queueFormatter = new HalQueueFormatter(new URI("http://host/queues"), new HalJobFormatter(new URI("http://host/jobs"), new URI("http://host/queues")))

  "format queue as hal+json" in {
    val hal = queueFormatter.halFrom(queueId, status, PageResult(0, List(job("2"), job("1")), previousExists = false, nextExists = false))

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedQueue.json"))
  }

  "format queue as hal+json with pagination links" in {
    val hal = queueFormatter.halFrom(queueId, status, PageResult(1, List(job("2"), job("1")), previousExists = true, nextExists = true))

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedQueueWithPaginationLinks.json"))
  }

  "format filtered queue as hal+json" in {
    val hal = queueFormatter.halFrom(queueId, status, PageResult(1, List(job("2"), job("1", Binding(List(BoundQueue(queueId), BoundQueue(QueueId("def")))))), previousExists = false, nextExists = false), filters)

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredQueue.json"))
  }

  "format filtered queue as hal+json with encoded filter value" in {
    val filters = Filters(List(Filter("data", "zzz%^&*ee$")))
    val hal = queueFormatter.halFrom(queueId, status, PageResult(1, List(job("2"), job("1")), previousExists = false, nextExists = false), filters)
    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredQueueWithEncodedFilterValue.json"))
  }

  "format filtered queue as hal+json with pagination links" in {
    val hal = queueFormatter.halFrom(queueId, status, PageResult(1, List(job("2"), job("1")), previousExists = true, nextExists = true), filters)

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredQueueWithPaginationLinks.json"))
  }

  "format queue entry as hal+json" in {
    val hal = queueFormatter.halFrom(queueId, job("1"))

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedQueueEntry.json"))
  }

  def jsonFromFile(filePath: String) = {
    parse(fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath)).mkString)
  }

  def job(id: String, binding: Binding = Binding(List(BoundQueue(queueId)))) = {
    JobRecord(AggregateId(id), createdDate, binding, status, triggerDate, Payload.empty)
  }
}
