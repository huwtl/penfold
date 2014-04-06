package org.huwtl.penfold.app.web.bean

import org.specs2.mutable.Specification
import org.huwtl.penfold.domain.model.{Binding, Payload}
import org.huwtl.penfold.command.{CreateFutureJob, CreateJob}
import org.joda.time.DateTime

class JobCreationRequestTest extends Specification {
  "convert to job creation command" in {
    val request = JobCreationRequest(None, Payload.empty, Binding(List()))
    request.toCommand must beEqualTo(CreateJob(Binding(List()), Payload.empty))
  }

  "convert to future job creation command" in {
    val triggerDate = DateTime.now
    val request = JobCreationRequest(Some(triggerDate), Payload.empty, Binding(List()))
    request.toCommand must beEqualTo(CreateFutureJob(Binding(List()), triggerDate, Payload.empty))
  }
}
