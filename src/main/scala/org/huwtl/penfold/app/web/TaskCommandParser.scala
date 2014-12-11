package org.huwtl.penfold.app.web

import org.huwtl.penfold.app.support.json.ObjectSerializer
import org.huwtl.penfold.domain.model.AggregateVersion
import org.huwtl.penfold.command.Command
import scala._
import org.huwtl.penfold.app.web.bean._
import org.huwtl.penfold.domain.model.AggregateId
import org.huwtl.penfold.app.web.bean.CreateFutureTaskRequest
import org.huwtl.penfold.app.web.bean.RequeueTaskRequest
import org.huwtl.penfold.app.web.bean.CreateTaskRequest
import org.huwtl.penfold.app.web.bean.StartTaskRequest

class TaskCommandParser(jsonConverter: ObjectSerializer) {

  def parse(commandType: String, json: String): Command = {
    commandType match {
      case "CreateTask" => jsonConverter.deserialize[CreateTaskRequest](json).toCommand
      case "CreateFutureTask" => jsonConverter.deserialize[CreateFutureTaskRequest](json).toCommand
      case _ => throwUnknownTypeException(commandType)
    }
  }

  def parse(commandType: String, id: AggregateId, version: AggregateVersion, json: String): Command = {
    commandType match {
      case "StartTask" => jsonConverter.deserialize[StartTaskRequest](json).toCommand(id, version)
      case "RequeueTask" => jsonConverter.deserialize[RequeueTaskRequest](json).toCommand(id, version)
      case "RescheduleTask" => jsonConverter.deserialize[RescheduleTaskRequest](json).toCommand(id, version)
      case "CloseTask" => jsonConverter.deserialize[CloseTaskRequest](json).toCommand(id, version)
      case "UnassignTask" => jsonConverter.deserialize[UnassignTaskRequest](json).toCommand(id, version)
      case "UpdateTaskPayload" => jsonConverter.deserialize[UpdateTaskPayloadRequest](json).toCommand(id, version)
      case _ => throwUnknownTypeException(commandType)
    }
  }

  private def throwUnknownTypeException(commandType: String) = throw new IllegalArgumentException(s"unknown command type $commandType")
}
