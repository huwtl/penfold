package org.huwtl.penfold.app.support.hal

import scala.collection.JavaConversions._
import com.theoryinpractise.halbuilder.api.RepresentationFactory._
import org.joda.time.format.DateTimeFormat
import java.net.URI
import com.theoryinpractise.halbuilder.DefaultRepresentationFactory
import org.huwtl.penfold.query.JobRecord

class HalJobFormatter(selfLink: URI, triggeredJobLink: URI) {
  private val representationFactory = new DefaultRepresentationFactory

  private val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  def halFrom(job: JobRecord) = {
    val hal = representationFactory.newRepresentation(s"${selfLink.toString}/${job.id.value}")
      .withLink("triggeredFeed", s"${triggeredJobLink.toString}?type=${job.jobType.value}")
      .withProperty("jobType", job.jobType.value)
      .withProperty("status", job.status.name)
      .withProperty("triggerDate", dateFormatter.print(job.triggerDate))
      .withProperty("payload", deepConvertToJavaMap(job.payload.content))

    hal.toString(HAL_JSON)
  }

  private def deepConvertToJavaMap(scalaMap: Map[String, Any]): java.util.Map[String, Any] = {
    scalaMap.map {
      case (key, innerMap: Map[_, _]) => (key, mapAsJavaMap(deepConvertToJavaMap(innerMap.asInstanceOf[Map[String, Any]])))
      case notMap => notMap
    }
  }
}
