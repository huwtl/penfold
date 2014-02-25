package org.huwtl.penfold.app.support.hal

import java.net.URI
import scala.io.Source._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.specs2.mutable.Specification
import org.huwtl.penfold.domain.model.{Payload, QueueName, Status, Id}
import org.huwtl.penfold.query.JobRecord
import org.joda.time.DateTime

class HalQueueFormatterTest extends Specification {

  val createdDate = new DateTime(2014, 2, 25, 13, 0, 0, 0)

  val triggerDate = new DateTime(2014, 2, 25, 14, 0, 0, 0)

  val queueName = QueueName("abc")

  val status = Status.Triggered

  val queueFormatter = new HalQueueFormatter(new URI("http://host/queues"), new HalJobFormatter(new URI("http://host/jobs"), new URI("http://host/queues")))

  "format queue as hal+json" in {
    val job1 = JobRecord(Id("1"), createdDate, queueName, status, triggerDate, Payload(Map()))
    val job2 = JobRecord(Id("2"), createdDate, queueName, status, triggerDate, Payload(Map()))

    val hal = queueFormatter.halFrom(queueName, status, List(job2, job1))

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedQueue.json"))
  }

  "format queue entry as hal+json" in {
    val job1 = JobRecord(Id("1"), createdDate, queueName, status, triggerDate, Payload(Map()))

    val hal = queueFormatter.halFrom(job1)

    parse(hal) must beEqualTo(jsonFromFile("fixtures/hal/halFormattedQueueEntry.json"))
  }

  def jsonFromFile(filePath: String) = {
    parse(fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath)).mkString)
  }
}
