package com.hlewis.eventfire.app

import javax.servlet.ServletContext
import org.scalatra.LifeCycle
import com.hlewis.eventfire.app.support._
import com.hlewis.eventfire.app.web._
import scala.language.postfixOps
import com.hlewis.eventfire.app.store.InMemoryJobStore
import java.net.URI
import com.hlewis.eventfire.app.support.hal.{HalTriggeredJobFeedFormatter, HalStartedJobFormatter, HalJobFormatter, HalCompletedJobFormatter}
import com.hlewis.eventfire.usecases._
import java.util.concurrent.Executors._
import java.util.concurrent.TimeUnit._

class Main extends LifeCycle {

  override def init(context: ServletContext) {
    val jobStore = new InMemoryJobStore()

    val jsonJobConverter = new JobJsonConverter
    val jsonStartJobRequestConverter = new StartJobRequestJsonConverter
    val jsonCompleteJobRequestConverter = new CompleteJobRequestJsonConverter

    val baseUrl = new URI("http://localhost:8080")

    val jobLink = new URI(s"${baseUrl.toString}/jobs")

    val triggeredJobLink = new URI(s"${baseUrl.toString}/feed/triggered")

    val startedJobLink = new URI(s"${baseUrl.toString}/feed/started")

    val completedJobLink = new URI(s"${baseUrl.toString}/feed/completed")

    val jobFormatter = new HalJobFormatter(jobLink, triggeredJobLink)

    val triggeredJobFeedFormatter = new HalTriggeredJobFeedFormatter(triggeredJobLink, jobLink, startedJobLink)

    val startedJobFormatter = new HalStartedJobFormatter(startedJobLink, jobLink, completedJobLink)

    val completedJobFormatter = new HalCompletedJobFormatter(completedJobLink, jobLink)

    val createJob = new CreateJob(jobStore)
    val retrieveJobById = new RetrieveJobById(jobStore)

    val retrieveTriggeredJob = new RetrieveTriggeredJob(jobStore)
    val retrieveTriggeredJobs = new RetrieveTriggeredJobs(jobStore)
    val retrieveTriggeredJobsByType = new RetrieveTriggeredJobsByType(jobStore)

    val startJob = new StartJob(jobStore)
    val retrieveStartedJob = new RetrieveStartedJob(jobStore)
    val retrieveStartedJobs = new RetrieveStartedJobs(jobStore)

    val completeJob = new CompleteJob(jobStore)
    val retrieveCompletedJob = new RetrieveCompletedJob(jobStore)
    val retrieveCompletedJobs = new RetrieveCompletedJobs(jobStore)

    val triggerPendingJobs = new TriggerPendingJobs(jobStore)

    context mount(new JobsResource(retrieveJobById, createJob, jsonJobConverter, jobFormatter), "/jobs/*")
    context mount(new TriggeredJobFeedResource(retrieveTriggeredJob, retrieveTriggeredJobs, retrieveTriggeredJobsByType, jsonJobConverter, triggeredJobFeedFormatter), "/feed/triggered/*")
    context mount(new StartedJobFeedResource(startJob, retrieveStartedJob, retrieveStartedJobs, jsonStartJobRequestConverter, startedJobFormatter), "/feed/started/*")
    context mount(new CompletedJobFeedResource(completeJob, retrieveCompletedJob, retrieveCompletedJobs, jsonCompleteJobRequestConverter, completedJobFormatter), "/feed/completed/*")

    newSingleThreadScheduledExecutor.scheduleAtFixedRate(new Runnable() {
      def run() {
        try {
          triggerPendingJobs.triggerPending()
        }
        catch {
          case e: Exception => println(e)
        }
      }
    }, 0, 30, SECONDS)
  }
}
