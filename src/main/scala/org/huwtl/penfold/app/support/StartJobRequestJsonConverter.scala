package org.huwtl.penfold.app.support

import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods._
import org.huwtl.penfold.domain.StartJobRequest

class StartJobRequestJsonConverter {
  implicit val formats = DefaultFormats

  def from(json: String) = {
    parse(json).extract[StartJobRequest]
  }
}
