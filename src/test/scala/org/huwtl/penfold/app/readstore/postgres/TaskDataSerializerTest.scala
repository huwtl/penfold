package org.huwtl.penfold.app.readstore.postgres

import org.specs2.mutable.Specification
import org.specs2.matcher.DataTables
import scala.io.Source._
import org.json4s.jackson.JsonMethods._
import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.joda.time.DateTime
import org.huwtl.penfold.domain.model.Status.{Ready, Started}
import org.huwtl.penfold.support.TestModel
import scala.Some

class TaskDataSerializerTest extends Specification with DataTables {

  val prevStatus = PreviousStatus(Started, new DateTime(2014, 2, 25, 13, 0, 0, 0))

  val taskDataMinimal = TaskData(TestModel.aggregateId,
                          TestModel.version,
                          TestModel.createdDate,
                          TestModel.queueId,
                          Ready,
                          new DateTime(2014, 2, 25, 13, 0, 1, 0),
                          None,
                          TestModel.triggerDate,
                          None,
                          TestModel.triggerDate.getMillis,
                          TestModel.triggerDate.getMillis,
                          TestModel.payload,
                          None,
                          None)

  val taskData = TaskData(TestModel.aggregateId,
                          TestModel.version,
                          TestModel.createdDate,
                          TestModel.queueId,
                          Ready,
                          new DateTime(2014, 2, 25, 13, 0, 1, 0),
                          Some(prevStatus),
                          TestModel.triggerDate,
                          Some(TestModel.assignee),
                          TestModel.triggerDate.getMillis,
                          TestModel.triggerDate.getMillis,
                          TestModel.payload,
                          Some("user2"),
                          Some("user3"))

  val serializer = new ObjectSerializer

  "deserialise task data" in {
    "jsonPath"             || "expected"                                              |
    "taskData.json"        !!  taskData       |
    "taskDataMinimal.json" !! taskDataMinimal |> {
      (jsonPath, expected) =>
        val json = fromInputStream(getClass.getClassLoader.getResourceAsStream(s"fixtures/readstore/postgres/$jsonPath")).mkString
        val actualTaskData = serializer.deserialize[TaskData](json)
        actualTaskData must beEqualTo(expected)
    }
  }

  "serialise task data" in {
    "task"            || "expected"             |
      taskData        !! "taskData.json"        |
      taskDataMinimal !! "taskDataMinimal.json" |> {
      (task, expected) =>
        val expectedJson = compact(parse(fromInputStream(getClass.getClassLoader.getResourceAsStream(s"fixtures/readstore/postgres/$expected")).mkString))
        val actualJson = serializer.serialize(task)
        actualJson must beEqualTo(expectedJson)
    }
  }
}
