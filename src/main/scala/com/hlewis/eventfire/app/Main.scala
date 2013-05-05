package com.hlewis.eventfire.app

import javax.servlet.ServletContext
import org.scalatra.LifeCycle
import com.hlewis.eventfire.usecases.DispatchPendingJobsFromJobStoreToJobExchange

class Main extends LifeCycle with RabbitJobExchangeFactory with RedisJobStoreFactory {
  private val PENDING_CHECK_PERIOD = 10000

  private val pendingDispatchCheckQueue = new PendingJobDispatchQueue(new DispatchPendingJobsFromJobStoreToJobExchange)

  private val periodicPendingJobDispatchTrigger = new PeriodicPendingJobDispatchTrigger(PENDING_CHECK_PERIOD, pendingDispatchCheckQueue)

  override def init(context: ServletContext) {
    val jobExchange = createJobExchange()

    val jobStore = createJobStore()

    pendingDispatchCheckQueue.start()

    periodicPendingJobDispatchTrigger.start()


    context mount(new JobstoreController(jobStore, jobExchange), "/*")
  }

  override def destroy(context: ServletContext) {
    pendingDispatchCheckQueue ! Quit
  }
}
