package org.huwtl.penfold.app.support.hal

import java.net.URI

import org.huwtl.penfold.domain.model.{AggregateId, _}
import org.huwtl.penfold.readstore.{PageResult, _}
import org.huwtl.penfold.support.{JsonFixtures, TestModel}
import org.specs2.mutable.SpecificationWithJUnit

class HalQueueFormatterTest extends SpecificationWithJUnit with JsonFixtures {

  val filters = Filters(List(EQ("data", "a value")))

  val pageRequest = PageRequest(10, Some(PageReference("3~1393336800000~0")))

  val queueId = TestModel.queueId

  val status = Status.Ready

  val task1 = TestModel.readModels.task.copy(id = AggregateId("1"))

  val task2 = TestModel.readModels.task.copy(id = AggregateId("2"))

  val queueFormatter = new HalQueueFormatter(new URI("http://host/queues"), new HalTaskFormatter(new URI("http://host/tasks"), new URI("http://host/queues")))

  "format queue as hal+json" in {
    val hal = queueFormatter.halFrom(queueId, status, pageRequest, PageResult(List(task2, task1), None, None))

    asJson(hal) must beEqualTo(jsonFixture("fixtures/hal/halFormattedQueue.json"))
  }

  "format queue as hal+json with pagination links" in {
    val hal = queueFormatter.halFrom(queueId, status, pageRequest, PageResult(List(task2, task1), Some(PageReference("2~1393336800000~0")), Some(PageReference("1~1393336800000~1"))))

    asJson(hal) must beEqualTo(jsonFixture("fixtures/hal/halFormattedQueueWithPaginationLinks.json"))
  }

  "format filtered queue as hal+json" in {
    val hal = queueFormatter.halFrom(queueId, status, pageRequest, PageResult(List(task2, task1), None, None), filters)

    asJson(hal) must beEqualTo(jsonFixture("fixtures/hal/halFormattedFilteredQueue.json"))
  }

  "format filtered queue as hal+json with encoded filter value" in {
    val filters = Filters(List(EQ("data", "zzz%^&*ee$")))
    val hal = queueFormatter.halFrom(queueId, status, pageRequest, PageResult(List(task2, task1), None, None), filters)
    asJson(hal) must beEqualTo(jsonFixture("fixtures/hal/halFormattedFilteredQueueWithEncodedFilterValue.json"))
  }

  "format filtered queue as hal+json with pagination links" in {
    val hal = queueFormatter.halFrom(queueId, status, pageRequest, PageResult(List(task2, task1), Some(PageReference("2~1393336800000~0")), Some(PageReference("1~1393336800000~1"))), filters)

    asJson(hal) must beEqualTo(jsonFixture("fixtures/hal/halFormattedFilteredQueueWithPaginationLinks.json"))
  }
}