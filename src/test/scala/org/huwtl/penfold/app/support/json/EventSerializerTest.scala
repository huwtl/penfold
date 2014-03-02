package org.huwtl.penfold.app.support.json

import org.specs2.mutable.Specification
import scala.io.Source._
import org.json4s.jackson.JsonMethods._
import org.huwtl.penfold.domain.model.{Version, AggregateId, QueueName, Payload}
import org.huwtl.penfold.domain.event.JobCreated
import org.joda.time.DateTime

class EventSerializerTest extends Specification {
  val event = JobCreated(AggregateId("a1"), Version.init, QueueName("abc"), new DateTime(2014, 2, 3, 12, 47, 54), new DateTime(2014, 2, 3, 14, 30, 1), Payload(Map("stuff" -> "something", "nested" -> Map("inner" -> true))))

  val serializer = new EventSerializer

  "deserialise job created event" in {
    val json = fromInputStream(getClass.getClassLoader.getResourceAsStream("fixtures/job_created.json")).mkString

    val actualEvent = serializer.deserialize(json)

    actualEvent must beEqualTo(event)
  }

  "serialise job created event" in {
    val expectedJson = pretty(parse(fromInputStream(getClass.getClassLoader.getResourceAsStream("fixtures/job_created.json")).mkString))

    val json = serializer.serialize(event)

    json must beEqualTo(expectedJson)
  }
}
