package org.huwtl.penfold.app.support.hal

import java.net.URI
import scala.io.Source._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.specs2.mutable.Specification
import org.huwtl.penfold.domain.model._
import org.huwtl.penfold.readstore._
import org.huwtl.penfold.readstore.TaskProjection
import scala.Some
import org.huwtl.penfold.support.TestModel._
import org.huwtl.penfold.support.TestModel.readModels._
import org.huwtl.penfold.support.TestModel
import org.huwtl.penfold.domain.model.Status.Ready

class HalTaskFormatterTest extends Specification {

  val filters = Filters(List(EQ("data", "a value")))

  val pageRequest = PageRequest(10, Some(PageReference("1~1393336800000~1")))

  val taskFormatter = new HalTaskFormatter(new URI("http://host/tasks"), new URI("http://host/queues"))

  "format waiting task as hal+json" in {
    hal(task.copy(status = Status.Waiting)) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedWaitingTask.json"))
    hal(task.copy(status = Status.Waiting, assignee = Some(TestModel.assignee))) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedWaitingTaskWithAssignee.json"))
  }

  "format ready task as hal+json" in {
    hal(task.copy(status = Status.Ready)) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedReadyTask.json"))
    hal(task.copy(status = Status.Ready, assignee = Some(TestModel.assignee))) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedReadyTaskWithAssignee.json"))
  }

  "format started task as hal+json" in {
    hal(task.copy(status = Status.Started, previousStatus = Some(previousStatus.copy(status = Ready)), assignee = Some(assignee))) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedStartedTask.json"))
  }

  "format closed task as hal+json" in {
    hal(closedTask) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedClosedTask.json"))
  }

  "format cancelled task as hal+json" in {
    hal(cancelledTask) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedCancelledTask.json"))
  }

  "format filtered tasks hal+json" in {
    halTasks(filters) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredTasks.json"))
  }

  "format filtered tasks hal+json with encoded filter value" in {
    val filters = Filters(List(EQ("data", "zzz%^&*ee$")))
    halTasks(filters) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredTasksWithEncodedFilterValue.json"))
  }

  "format filtered tasks hal+json with pagination links" in {
    halTasks(filters, 1, Some(PageReference("1~1393336800000~0")), Some(PageReference("1~1393336800000~1"))) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredTasksWithPaginationLinks.json"))
  }

  "format task as hal+json with complex payload" in {
    val task = TestModel.readModels.task.copy(status = Status.Waiting, payload = complexPayload)
    hal(task) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedTaskWithComplexPayload.json"))
  }

  private def halTasks(filters: Filters, pageNumber: Int = 0, previousPage: Option[PageReference] = None, nextPage: Option[PageReference] = None) = {
    parse(taskFormatter.halFrom(pageRequest,
      PageResult(List(TestModel.readModels.task.copy(status = Status.Waiting)), previousPage, nextPage), filters
    ))
  }

  private def hal(task: TaskProjection) = {
    parse(taskFormatter.halFrom(task))
  }

  private def jsonFromFile(filePath: String) = {
    parse(fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath)).mkString)
  }
}
