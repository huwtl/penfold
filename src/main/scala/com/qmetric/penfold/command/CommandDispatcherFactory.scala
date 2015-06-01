package com.qmetric.penfold.command

import com.qmetric.penfold.domain.store.DomainRepository

class CommandDispatcherFactory(domainRepository: DomainRepository, aggregateIdFactory: AggregateIdFactory) {

  def create = {
    new CommandDispatcher(Map[Class[_ <: Command], CommandHandler[_ <: Command]](//
      classOf[CreateTask] -> new CreateTaskHandler(domainRepository, aggregateIdFactory), //
      classOf[CreateFutureTask] -> new CreateFutureTaskHandler(domainRepository, aggregateIdFactory), //
      classOf[TriggerTask] -> new TriggerTaskHandler(domainRepository), //
      classOf[StartTask] -> new StartTaskHandler(domainRepository), //
      classOf[RequeueTask] -> new RequeueTaskHandler(domainRepository), //
      classOf[RescheduleTask] -> new RescheduleTaskHandler(domainRepository), //
      classOf[UnassignTask] -> new UnassignTaskHandler(domainRepository), //
      classOf[ReassignTask] -> new ReassignTaskHandler(domainRepository), //
      classOf[CloseTask] -> new CloseTaskHandler(domainRepository), //
      classOf[UpdateTaskPayload] -> new UpdateTaskPayloadHandler(domainRepository), //
      classOf[ArchiveTask] -> new ArchiveTaskHandler(domainRepository) //
    ))
  }
}
