package org.huwtl.penfold.app.support.hal

import scala.collection.JavaConversions._
import com.theoryinpractise.halbuilder.api.RepresentationFactory._
import org.joda.time.format.DateTimeFormat
import java.net.URI
import com.theoryinpractise.halbuilder.DefaultRepresentationFactory
import org.huwtl.penfold.query.JobRecord
import org.huwtl.penfold.domain.model.Status._
import com.theoryinpractise.halbuilder.api.Representation

class HalJobFormatter(baseJobLink: URI, baseQueueLink: URI) {
  private val representationFactory = new DefaultRepresentationFactory

  private val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  def halRepresentationFrom(job: JobRecord) = {
    val representation: Representation = representationFactory.newRepresentation(s"${baseJobLink.toString}/${job.id.value}")
      .withProperty("queueName", job.queueName.value)
      .withProperty("status", job.status.name)
      .withProperty("triggerDate", dateFormatter.print(job.triggerDate))
      .withProperty("payload", deepConvertToJavaMap(job.payload.content))
      .withLink("currentQueue", s"${baseQueueLink.toString}/${job.queueName.value}/${job.status.name}")

    job.status match {
      case Waiting => {
        representation.withLink("triggeredQueue", s"${baseQueueLink.toString}/${job.queueName.value}/${Triggered.name}")
      }
      case Triggered => {
        representation.withLink("start", s"${baseQueueLink.toString}/${job.queueName.value}/${Started.name}")
      }
      case Started => {
        representation.withLink("complete", s"${baseQueueLink.toString}/${job.queueName.value}/${Completed.name}")
      }
      case _ =>
    }

    representation
  }

  def halFrom(job: JobRecord) = {
    halRepresentationFrom(job).toString(HAL_JSON)
  }

  private def deepConvertToJavaMap(scalaMap: Map[String, Any]): java.util.Map[String, Any] = {
    scalaMap.map {
      case (key, innerMap: Map[_, _]) => (key, mapAsJavaMap(deepConvertToJavaMap(innerMap.asInstanceOf[Map[String, Any]])))
      case notMap => notMap
    }
  }
}
