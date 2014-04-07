package org.huwtl.penfold.app.web

import net.iharder.Base64

trait WebAuthSpecification {
  def authHeader(username: String, password: String) = {
    Map("Authorization" -> s"Basic ${Base64.encodeBytes((username + ":" + password).getBytes)}")
  }
}
