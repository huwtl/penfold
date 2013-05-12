package com.hlewis.eventfire.app.web

import org.scalatra._
import scalate.ScalateSupport
import com.hlewis.eventfire.domain._
import scala.collection.mutable
import com.theoryinpractise.halbuilder.DefaultRepresentationFactory
import com.theoryinpractise.halbuilder.api.RepresentationFactory
import scala.collection.JavaConversions._
import net.hamnaberg.json._
import net.hamnaberg.json.util.{Optional, ListOps}
import java.util.Collections
import java.util
import java.net.URI
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

  get("/pending-jobs2") {
    val collectionUri = URI.create("http://localhost:8080/feed/pending-jobs")
    val items = new util.ArrayList[Item]()
    jobs.values.foreach(job => {
      items.add(Item.create(collectionUri.resolve("/feed/pending-jobs/" + job.header.reference), ListOps.of(Property.value("id", Optional.none(), ValueFactory.createOptionalValue(job.header.reference))), Collections.emptyList()))
    })
    val collection = Collection.builder(collectionUri).addItems(items).build().asJson()
    collection.toString
  }

  get("/pending-jobs2/:jobId") {
    val job = jobs.get(params("jobId")).get
    val collectionUri = URI.create("http://localhost:8080/feed/pending-jobs")
    val items = new util.ArrayList[Item]()
    items.add(Item.create(collectionUri.resolve("/feed/pending-jobs/" + job.header.reference), ListOps.of(
      Property.value("id", Optional.none(), ValueFactory.createOptionalValue(job.header.reference)),
      Property.value("cron", Optional.none(), ValueFactory.createOptionalValue(job.header.cron.toString)),
      Property.value("payload", Optional.none(), ValueFactory.createOptionalValue(job.body.data.toString())))
      ,Collections.singletonList(Link.create(URI.create("/feed/pending-jobs/" + job.header.reference + "/edit"), "edit")))
    )
    val collection = Collection.builder(collectionUri).addItems(items).build().asJson()
    collection.toString
  }

}
