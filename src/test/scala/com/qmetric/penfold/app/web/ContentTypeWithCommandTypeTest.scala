package com.qmetric.penfold.app.web

import org.specs2.mutable.SpecificationWithJUnit

class ContentTypeWithCommandTypeTest extends SpecificationWithJUnit {

  "extract command type from content type header value" in {
    ((contentType: String) => ContentTypeWithCommandType(Some(contentType)).extractedCommandType must beEqualTo("StartTask")) forall Seq(
      "application/json;domain-command=StartTask",
      "application/json; domain-command=StartTask",
      "application/json; otherParm=abc; domain-command=StartTask",
      "application/json; otherParm=abc; domain-command=StartTask; otherParam2=def",
      "application/json; otherParm=abc; domain-command=StartTask ; otherParam2=def"
    )
  }

  "throw exception when invalid content type" in {
    ((contentType: Option[String]) => ContentTypeWithCommandType(contentType).extractedCommandType must throwA[IllegalArgumentException]) forall Seq(
      None,
      Some("text/plain; domain-command=StartTask"),
      Some("application/json; command=StartTask")
    )
  }
}
