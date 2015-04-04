package org.huwtl.penfold.app.readstore.postgres

import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.model.CloseResultType.Success
import org.huwtl.penfold.domain.model.Status.{Ready, Started}
import org.huwtl.penfold.support.{JsonFixtures, TestModel}
import org.joda.time.DateTime
import org.specs2.matcher.DataTables
import org.specs2.mutable.Specification

class TaskDataSerializerTest extends Specification with DataTables with JsonFixtures {

  val prevStatus = PreviousStatus(Started, new DateTime(2014, 2, 25, 13, 0, 0, 0).getMillis)

  val taskDataMinimal = TaskData(TestModel.aggregateId,
                                 TestModel.version,
                                 TestModel.createdDate.getMillis,
                                 TestModel.queueId,
                                 Ready,
                                 new DateTime(2014, 2, 25, 13, 0, 1, 0).getMillis,
                                 None,
                                 attempts = 0,
                                 TestModel.triggerDate.getMillis,
                                 None,
                                 TestModel.triggerDate.getMillis,
                                 TestModel.triggerDate.getMillis,
                                 TestModel.payload,
                                 None,
                                 None,
                                 None,
                                 None)

  val taskDataFull = TaskData(TestModel.aggregateId,
                              TestModel.version,
                              TestModel.createdDate.getMillis,
                              TestModel.queueId,
                              Ready,
                              new DateTime(2014, 2, 25, 13, 0, 1, 0).getMillis,
                              Some(prevStatus),
                              attempts = 0,
                              TestModel.triggerDate.getMillis,
                              Some(TestModel.assignee),
                              TestModel.triggerDate.getMillis,
                              TestModel.triggerDate.getMillis,
                              TestModel.payload,
                              Some("reason1"),
                              Some("reason2"),
                              Some("reason3"),
                              Some(Success))

  val serializer = new ObjectSerializer

  "deserialise task data for postgres" in {
    "jsonPath"             || "expected"                                              |
    "taskData.json"        !! taskDataFull       |
    "taskDataMinimal.json" !! taskDataMinimal |> {
      (jsonPath, expected) =>
        val json = jsonFixtureAsString(s"fixtures/readstore/postgres/$jsonPath")
        val actualTaskData = serializer.deserialize[TaskData](json)
        actualTaskData must beEqualTo(expected)
    }
  }

  "serialise task data for postgres" in {
    "task"            || "expected"             |
      taskDataFull    !! "taskData.json"        |
      taskDataMinimal !! "taskDataMinimal.json" |> {
      (task, expected) =>
        val expectedJson = jsonFixture(s"fixtures/readstore/postgres/$expected")
        val actualJson = asJson(serializer.serialize(task))
        actualJson must beEqualTo(expectedJson)
    }
  }
}
