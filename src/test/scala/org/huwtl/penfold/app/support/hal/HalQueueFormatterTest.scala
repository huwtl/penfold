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
import org.huwtl.penfold.readstore.TaskRecord
import org.huwtl.penfold.readstore.PageResult

class HalQueueFormatterTest extends Specification {

  val createdDate = new DateTime(2014, 2, 25, 13, 0, 0, 0)

  val triggerDate = new DateTime(2014, 2, 25, 14, 0, 0, 0)

  val filters = Filters(List(Filter("data", Some("value"))))

  val pageRequest = PageRequest(10, Some(PageReference("3~1393336800000~0")))

  val queueId = QueueId("abc")

  val status = Status.Ready

  val queueFormatter = new HalQueueFormatter(new URI("http://host/queues"), new HalTaskFormatter(new URI("http://host/tasks"), new URI("http://host/queues")))

  "format queue as hal+json" in {
    val hal = queueFormatter.halFrom(queueId, status, pageRequest, PageResult(List(task("2"), task("1")), None, None))

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedQueue.json"))
  }

  "format queue as hal+json with pagination links" in {
    val hal = queueFormatter.halFrom(queueId, status, pageRequest, PageResult(List(task("2"), task("1")), Some(PageReference("2~1393336800000~0")), Some(PageReference("1~1393336800000~1"))))

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedQueueWithPaginationLinks.json"))
  }

  "format filtered queue as hal+json" in {
    val hal = queueFormatter.halFrom(queueId, status, pageRequest, PageResult(List(task("2"), task("1", QueueBinding(queueId))), None, None), filters)

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredQueue.json"))
  }

  "format filtered queue as hal+json with encoded filter value" in {
    val filters = Filters(List(Filter("data", Some("zzz%^&*ee$"))))
    val hal = queueFormatter.halFrom(queueId, status, pageRequest, PageResult(List(task("2"), task("1")), None, None), filters)
    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredQueueWithEncodedFilterValue.json"))
  }

  "format filtered queue as hal+json with pagination links" in {
    val hal = queueFormatter.halFrom(queueId, status, pageRequest, PageResult(List(task("2"), task("1")), Some(PageReference("2~1393336800000~0")), Some(PageReference("1~1393336800000~1"))), filters)

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredQueueWithPaginationLinks.json"))
  }

  "format queue entry as hal+json" in {
    val hal = queueFormatter.halFrom(queueId, task("1"))

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedQueueEntry.json"))
  }

  def jsonFromFile(filePath: String) = {
    parse(fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath)).mkString)
  }

  def task(id: String, binding: QueueBinding = QueueBinding(queueId)) = {
    TaskRecord(AggregateId(id), createdDate, binding, status, createdDate, triggerDate, triggerDate.getMillis, Payload.empty)
  }
}
