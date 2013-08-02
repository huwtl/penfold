package com.hlewis.eventfire.app.support.hal

import com.theoryinpractise.halbuilder.api.RepresentationFactory
import scala.collection.JavaConversions._
import com.theoryinpractise.halbuilder.api.RepresentationFactory._
import com.hlewis.eventfire.domain.Job
import org.joda.time.format.DateTimeFormat
import java.net.URI
import com.theoryinpractise.halbuilder.DefaultRepresentationFactory

class HalJobFormatter(selfLink: URI, triggeredJobLink: URI) {
  private val representationFactory = new DefaultRepresentationFactory

  private val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  def halFrom(job: Job) = {
    val hal = representationFactory.newRepresentation(s"${selfLink.toString}/${job.id}")
      .withLink("triggeredFeed", s"${triggeredJobLink.toString}?type=${job.jobType}")
      .withProperty("jobType", job.jobType)
      .withProperty("status", job.status)
      .withProperty("triggerDate", dateFormatter.print(job.nextTriggerDate))
      .withProperty("payload", deepConvertToJavaMap(job.payload.content))

    if (job.cron.isDefined) {
      hal.withProperty("cron", job.cron.get.toString)
    }

    hal.toString(HAL_JSON)
  }

  private def deepConvertToJavaMap(scalaMap: Map[String, Any]): java.util.Map[String, Any] = {
    scalaMap.map(entry => entry match {
      case (key, innerMap: Map[_, _]) => (key, mapAsJavaMap(deepConvertToJavaMap(innerMap.asInstanceOf[Map[String, Any]])))
      case notMap => notMap
    })
  }
}
