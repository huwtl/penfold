package com.hlewis.eventfire.app.web

import org.scalatra._
import scalate.ScalateSupport
import com.hlewis.eventfire.domain._
import scala.collection.mutable
import com.theoryinpractise.halbuilder.DefaultRepresentationFactory
import com.theoryinpractise.halbuilder.api.RepresentationFactory
import scala.collection.JavaConversions._
import com.hlewis.eventfire.domain.Header
import com.hlewis.eventfire.domain.Body
import com.hlewis.eventfire.domain.Job
import com.hlewis.eventfire.domain.Cron

class JobsFeedController(jobstore: JobStore) extends ScalatraServlet with ScalateSupport {

  private val jobs = mutable.Map[String, Job]("job1" -> Job(Header("job1", "test", Cron("1", "10", "*", "*", "*"), Map()), Body(Map("data" -> "value"))),
    "job2" -> Job(Header("job2", "test", Cron("1", "10", "*", "*", "*"), Map()), Body(Map("data" -> "value"))))

  before() {
    contentType = "application/hal+json"
  }

  get("/pending-jobs") {
    val representationFactory = new DefaultRepresentationFactory()

    val root = representationFactory.newRepresentation("http://localhost:8080/feed/pending-jobs")

    jobs.values.foreach(job => {
      val rep = representationFactory.newRepresentation("/feed/pending-jobs/" + job.header.reference)
        .withProperty("id", job.header.reference)
      root.withRepresentation("jobs", rep)
    })

    root.toString(RepresentationFactory.HAL_JSON)
  }

  get("/pending-jobs/:jobId") {
    val job = jobs.get(params("jobId")).get

    val representationFactory = new DefaultRepresentationFactory()

    val root = representationFactory.newRepresentation("http://localhost:8080/feed/pending-jobs/" + job.header.reference)
      .withNamespace("ex", "http://localhost:8080/api-docs/pending-jobs/{rel}.html")
      .withLink("ex:edit", "/feed/pending-jobs/" + job.header.reference + "/edit")
      .withProperty("created", "2010-01-16 00:00:00")
      .withProperty("updated", "2010-02-21 00:00:00")
      .withProperty("id", job.header.reference)
      .withProperty("cron", job.header.cron.toString)
      .withProperty("payload", mapAsJavaMap(job.body.data))

    root.toString(RepresentationFactory.HAL_JSON)
  }

}
