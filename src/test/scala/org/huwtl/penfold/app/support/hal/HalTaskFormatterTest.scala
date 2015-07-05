package org.huwtl.penfold.app.support.hal

import java.net.URI

import org.huwtl.penfold.domain.model.Status.Ready
import org.huwtl.penfold.domain.model._
import org.huwtl.penfold.readstore.{TaskProjection, _}
import org.huwtl.penfold.support.TestModel._
import org.huwtl.penfold.support.TestModel.readModels._
import org.huwtl.penfold.support.{JsonFixtures, TestModel}
import org.specs2.mutable.SpecificationWithJUnit

class HalTaskFormatterTest extends SpecificationWithJUnit with JsonFixtures {

  val filters = Filters(List(EQ("data", "a value")))

  val pageRequest = PageRequest(10, Some(PageReference("1~1393336800000~1")))

  val taskFormatter = new HalTaskFormatter(new URI("http://host/tasks"), new URI("http://host/queues"))

  "format waiting task as hal+json" in {
    hal(task.copy(status = Status.Waiting)) must beEqualTo(jsonFixture("fixtures/hal/halFormattedWaitingTask.json"))
    hal(task.copy(status = Status.Waiting, assignee = Some(TestModel.assignee))) must beEqualTo(jsonFixture("fixtures/hal/halFormattedWaitingTaskWithAssignee.json"))
  }

  "format ready task as hal+json" in {
    hal(task.copy(status = Status.Ready)) must beEqualTo(jsonFixture("fixtures/hal/halFormattedReadyTask.json"))
    hal(task.copy(status = Status.Ready, assignee = Some(TestModel.assignee))) must beEqualTo(jsonFixture("fixtures/hal/halFormattedReadyTaskWithAssignee.json"))
  }

  "format started task as hal+json" in {
    hal(task.copy(status = Status.Started, previousStatus = Some(previousStatus.copy(status = Ready)), assignee = Some(assignee))) must beEqualTo(jsonFixture("fixtures/hal/halFormattedStartedTask.json"))
  }

  "format closed task as hal+json" in {
    hal(closedTask) must beEqualTo(jsonFixture("fixtures/hal/halFormattedClosedTask.json"))
  }

  "format cancelled task as hal+json" in {
    hal(cancelledTask) must beEqualTo(jsonFixture("fixtures/hal/halFormattedCancelledTask.json"))
  }

  "format filtered tasks hal+json" in {
    halTasks(filters) must beEqualTo(jsonFixture("fixtures/hal/halFormattedFilteredTasks.json"))
  }

  "format filtered tasks hal+json with encoded filter value" in {
    val filters = Filters(List(EQ("data", "zzz%^&*ee$")))
    halTasks(filters) must beEqualTo(jsonFixture("fixtures/hal/halFormattedFilteredTasksWithEncodedFilterValue.json"))
  }

  "format filtered tasks hal+json with pagination links" in {
    halTasks(filters, 1, Some(PageReference("1~1393336800000~0")), Some(PageReference("1~1393336800000~1"))) must beEqualTo(jsonFixture("fixtures/hal/halFormattedFilteredTasksWithPaginationLinks.json"))
  }

  "format task as hal+json with complex payload" in {
    val task = TestModel.readModels.task.copy(status = Status.Waiting, payload = complexPayload)
    hal(task) must beEqualTo(jsonFixture("fixtures/hal/halFormattedTaskWithComplexPayload.json"))
  }

  private def halTasks(filters: Filters, pageNumber: Int = 0, previousPage: Option[PageReference] = None, nextPage: Option[PageReference] = None) = {
    asJson(taskFormatter.halFrom(pageRequest,
      PageResult(List(TestModel.readModels.task.copy(status = Status.Waiting)), previousPage, nextPage), filters
    ))
  }

  private def hal(task: TaskProjection) = {
    asJson(taskFormatter.halFrom(task))
  }
}