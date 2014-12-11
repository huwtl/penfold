package org.huwtl.penfold.support

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit._

class RetryTest extends Specification with Mockito {

  "should not retry when result is Some(x) on first attempt" in {
    val service = mock[TestService]
    service.execute returns Some("ok")

    val result = Retry.retryUntilSome[String](2, FiniteDuration(3, MILLISECONDS)){service.execute}

    result must beSome("ok")
    there was one(service).execute
  }

  "should retry until result becomes Some(x)" in {
    val service = mock[TestService]
    service.execute returns None thenReturns Some("ok")

    val result = Retry.retryUntilSome[String](2, FiniteDuration(3, MILLISECONDS)){service.execute}

    result must beSome("ok")
    there was two(service).execute
  }

  "should return none after max retries waiting for result to become Some(x)" in {
    val service = mock[TestService]
    service.execute returns None

    val result = Retry.retryUntilSome[String](2, FiniteDuration(1, MILLISECONDS)){service.execute}

    result must beNone
    there was two(service).execute
  }

  trait TestService {
    def execute: Option[String]
  }
}
