package org.huwtl.penfold.app.schedule

import org.specs2.mutable.Specification
import org.huwtl.penfold.readstore.{TaskProjectionReference, ReadStore}
import org.huwtl.penfold.command.{UnassignTask, CommandDispatcher}
import org.huwtl.penfold.app.TaskAssignmentTimeoutConfiguration
import org.huwtl.penfold.support.TestModel
import org.huwtl.penfold.domain.model.AggregateVersion
import org.specs2.mock.Mockito
import org.huwtl.penfold.domain.model.patch.{Remove, Patch}
import org.huwtl.penfold.domain.model.Status.Ready

class ReadyTaskAssignmentTimeoutSchedulerTest extends Specification with Mockito {

  "periodically timeout assigned ready tasks" in {
    val readStore = mock[ReadStore]
    val commandDispatcher = mock[CommandDispatcher]
    val config = TaskAssignmentTimeoutConfiguration("assignmentTimeout")
    //readStore.retrieveTasksToTimeout("payload.assignmentTimeout", Some(Ready)) returns List(TaskProjectionReference(TestModel.aggregateId, AggregateVersion.init)).toIterator

    new ReadyTaskAssignmentTimeoutScheduler(readStore, commandDispatcher, config).process()

    there was one(commandDispatcher).dispatch(UnassignTask(TestModel.aggregateId, AggregateVersion.init, Some("TIMEOUT"), Some(Patch(List(Remove(config.timeoutPayloadPath))))))
  }
}
