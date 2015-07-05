package org.huwtl.penfold.app.support

trait ConnectivityCheck {
  def checkConnectivity: Either[Boolean, Exception]
}
