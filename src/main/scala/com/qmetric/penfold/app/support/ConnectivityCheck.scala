package com.qmetric.penfold.app.support

trait ConnectivityCheck {
  def checkConnectivity: Either[Boolean, Exception]
}
