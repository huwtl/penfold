package com.qmetric.penfold.app.schedule

import org.specs2.mutable.Specification
import com.qmetric.penfold.readstore.{TaskRecordReference, ReadStore}
import com.qmetric.penfold.command.{UnassignTask, CommandDispatcher}
import com.qmetric.penfold.app.TaskAssignmentTimeoutConfiguration
import com.qmetric.penfold.support.TestModel
import com.qmetric.penfold.domain.model.AggregateVersion
import org.specs2.mock.Mockito
import com.qmetric.penfold.domain.model.patch.{Remove, Patch}
import com.qmetric.penfold.domain.model.Status.Ready

class ReadyTaskAssignmentTimeoutSchedulerTest extends Specification with Mockito {

  "periodically timeout assigned ready tasks" in {
    val readStore = mock[ReadStore]
    val commandDispatcher = mock[CommandDispatcher]
    val config = TaskAssignmentTimeoutConfiguration("payload.assignmentTimeout")
    readStore.retrieveTasksToTimeout(config.timeoutAttributePath, Some(Ready)) returns List(TaskRecordReference(TestModel.aggregateId, AggregateVersion.init)).toIterator

    new ReadyTaskAssignmentTimeoutScheduler(readStore, commandDispatcher, config).process()

    there was one(commandDispatcher).dispatch(UnassignTask(TestModel.aggregateId, AggregateVersion.init, Some("TIMEOUT"), Some(Patch(List(Remove(config.timeoutAttributePath))))))
  }
}
