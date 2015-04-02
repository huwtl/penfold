package com.qmetric.penfold.app.support.hal

import java.net.URI
import scala.io.Source._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.specs2.mutable.Specification
import com.qmetric.penfold.domain.model._
import com.qmetric.penfold.readstore._
import com.qmetric.penfold.domain.model.AggregateId
import com.qmetric.penfold.readstore.PageResult
import com.qmetric.penfold.support.TestModel

class HalQueueFormatterTest extends Specification {

  val filters = Filters(List(EQ("data", "a value")))

  val pageRequest = PageRequest(10, Some(PageReference("3~1393336800000~0")))

  val queueId = TestModel.queueId

  val status = Status.Ready

  val task1 = TestModel.readModels.task.copy(id = AggregateId("1"))

  val task2 = TestModel.readModels.task.copy(id = AggregateId("2"))

  val queueFormatter = new HalQueueFormatter(new URI("http://host/queues"), new HalTaskFormatter(new URI("http://host/tasks"), new URI("http://host/queues")))

  "format queue as hal+json" in {
    val hal = queueFormatter.halFrom(queueId, status, pageRequest, PageResult(List(task2, task1), None, None))

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedQueue.json"))
  }

  "format queue as hal+json with pagination links" in {
    val hal = queueFormatter.halFrom(queueId, status, pageRequest, PageResult(List(task2, task1), Some(PageReference("2~1393336800000~0")), Some(PageReference("1~1393336800000~1"))))

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedQueueWithPaginationLinks.json"))
  }

  "format filtered queue as hal+json" in {
    val hal = queueFormatter.halFrom(queueId, status, pageRequest, PageResult(List(task2, task1), None, None), filters)

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredQueue.json"))
  }

  "format filtered queue as hal+json with encoded filter value" in {
    val filters = Filters(List(EQ("data", "zzz%^&*ee$")))
    val hal = queueFormatter.halFrom(queueId, status, pageRequest, PageResult(List(task2, task1), None, None), filters)
    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredQueueWithEncodedFilterValue.json"))
  }

  "format filtered queue as hal+json with pagination links" in {
    val hal = queueFormatter.halFrom(queueId, status, pageRequest, PageResult(List(task2, task1), Some(PageReference("2~1393336800000~0")), Some(PageReference("1~1393336800000~1"))), filters)

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredQueueWithPaginationLinks.json"))
  }

  def jsonFromFile(filePath: String) = {
    parse(fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath)).mkString)
  }
}
