package org.huwtl.penfold.command

import org.huwtl.penfold.domain.store.DomainRepository

class CommandDispatcherFactory(domainRepository: DomainRepository, aggregateIdFactory: AggregateIdFactory) {

  def create = {
    new CommandDispatcher(Map[Class[_ <: Command], CommandHandler[_ <: Command]](//
      classOf[CreateTask] -> new CreateTaskHandler(domainRepository, aggregateIdFactory), //
      classOf[CreateFutureTask] -> new CreateFutureTaskHandler(domainRepository, aggregateIdFactory), //
      classOf[TriggerTask] -> new TriggerTaskHandler(domainRepository), //
      classOf[StartTask] -> new StartTaskHandler(domainRepository), //
      classOf[RequeueTask] -> new RequeueTaskHandler(domainRepository), //
      classOf[CompleteTask] -> new CompleteTaskHandler(domainRepository), //
      classOf[CancelTask] -> new CancelTaskHandler(domainRepository), //
      classOf[UpdateTaskPayload] -> new UpdateTaskPayloadHandler(domainRepository), //
      classOf[ArchiveTask] -> new ArchiveTaskHandler(domainRepository) //
    ))
  }
}
