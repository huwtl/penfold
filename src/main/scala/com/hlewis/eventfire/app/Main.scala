package com.hlewis.eventfire.app

import javax.servlet.ServletContext
import org.scalatra.LifeCycle
import com.hlewis.eventfire.app.support._
import com.hlewis.eventfire.app.web._
import com.hlewis.eventfire.app.store.{DbInMemoryJobStore, InMemoryJobStore}
import java.net.URI
import com.hlewis.eventfire.app.support.hal.{HalTriggeredJobFeedFormatter, HalStartedJobFormatter, HalJobFormatter, HalCompletedJobFormatter}
import com.hlewis.eventfire.usecases._
import java.util.concurrent.Executors._
import java.util.concurrent.TimeUnit._
import com.googlecode.flyway.core.Flyway
import com.mchange.v2.c3p0.ComboPooledDataSource
import scala.slick.session.Database
import com.hlewis.eventfire.domain.{Payload, Status, Job}
import org.joda.time.DateTime

class Main extends LifeCycle {
  override def init(context: ServletContext) {
    val jsonJobConverter = new JobJsonConverter
    val jsonStartJobRequestConverter = new StartJobRequestJsonConverter
    val jsonCompleteJobRequestConverter = new CompleteJobRequestJsonConverter

    val dataSource = new ComboPooledDataSource
    dataSource.setDriverClass("org.hsqldb.jdbcDriver")
    dataSource.setJdbcUrl("jdbc:hsqldb:mem:penfold;sql.syntax_mys=true")
    dataSource.setUser("sa")
    dataSource.setPassword("")

    val flyway = new Flyway
    flyway.setDataSource(dataSource)
    flyway.migrate()

    val database = Database.forDataSource(dataSource)

    val storeTest = new DbInMemoryJobStore(database, jsonJobConverter)
    storeTest.add(new Job("1", "type2", None, Some(new DateTime()), Status.Waiting, Payload(Map("val" -> "2"))))
    println(storeTest.retrieveBy("1"))
    println(storeTest.retrieveBy("2"))

    val jobStore = new InMemoryJobStore()

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
