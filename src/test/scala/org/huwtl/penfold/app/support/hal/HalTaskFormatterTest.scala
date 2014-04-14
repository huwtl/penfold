package org.huwtl.penfold.app.support.hal

import java.net.URI
import scala.io.Source._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.joda.time.DateTime
import org.specs2.mutable.Specification
import org.huwtl.penfold.domain.model._
import org.huwtl.penfold.readstore._
import org.huwtl.penfold.domain.model.Payload
import org.huwtl.penfold.domain.model.QueueBinding
import org.huwtl.penfold.readstore.Filter
import org.huwtl.penfold.domain.model.QueueId
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.readstore.TaskRecord
import scala.Some
import org.huwtl.penfold.readstore.NavigationDirection.Forward

class HalTaskFormatterTest extends Specification {

  val id = AggregateId("1")

  val created = new DateTime(2014, 2, 14, 12, 0, 0, 0)

  val triggerDate = new DateTime(2014, 2, 25, 14, 0, 0, 0)

  val filters = Filters(List(Filter("data", "value")))

  val pageRequest = PageRequest(10, Some(LastKnownPageDetails(id, triggerDate.getMillis, Forward)))

  val queueId = QueueId("abc")

  val payload = Payload(Map("data" -> "value", "inner" -> Map("bool" -> true)))

  val taskFormatter = new HalTaskFormatter(new URI("http://host/tasks"), new URI("http://host/queues"))

  "format waiting task as hal+json" in {
    hal(Status.Waiting) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedWaitingTask.json"))
  }

  "format ready task as hal+json" in {
    hal(Status.Ready, QueueBinding(QueueId("abc"))) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedReadyTask.json"))
  }

  "format started task as hal+json" in {
    hal(Status.Started) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedStartedTask.json"))
  }

  "format completed task as hal+json" in {
    hal(Status.Completed) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedCompletedTask.json"))
  }

  "format cancelled task as hal+json" in {
    hal(Status.Cancelled) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedCancelledTask.json"))
  }

  "format filtered tasks hal+json" in {
    halTasks(filters) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredTasks.json"))
  }

  "format filtered tasks hal+json with encoded filter value" in {
    val filters = Filters(List(Filter("data", "zzz%^&*ee$")))
    halTasks(filters) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredTasksWithEncodedFilterValue.json"))
  }

  "format filtered tasks hal+json with pagination links" in {
    halTasks(filters, 1, previousPage = true, nextPage = true) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedFilteredTasksWithPaginationLinks.json"))
  }

  "format task as hal+json with complex payload" in {
    val complexPayload = Payload(
      Map("data" -> "value", "inner" -> Map("bool" -> true, "inner2" -> List(Map("a" -> "1", "b" -> 1), Map("a" -> "2", "b" -> 2)))))
    val task = TaskRecord(id, created, QueueBinding(queueId), Status.Waiting, triggerDate, triggerDate.getMillis, complexPayload)
    hal(task) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedTaskWithComplexPayload.json"))
  }

  private def halTasks(filters: Filters, pageNumber: Int = 0, previousPage: Boolean = false, nextPage: Boolean = false) = {
    parse(taskFormatter.halFrom(pageRequest,
      PageResult(List(TaskRecord(id, created, QueueBinding(queueId), Status.Waiting, triggerDate, triggerDate.getMillis, payload)), previousExists = previousPage, nextExists = nextPage), filters
    ))
  }

  private def hal(status: Status, binding: QueueBinding = QueueBinding(queueId)) = {
    parse(taskFormatter.halFrom(TaskRecord(id, created, binding, status, triggerDate, triggerDate.getMillis, payload)))
  }

  private def hal(task: TaskRecord) = {
    parse(taskFormatter.halFrom(task))
  }

  private def jsonFromFile(filePath: String) = {
    parse(fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath)).mkString)
  }
}
