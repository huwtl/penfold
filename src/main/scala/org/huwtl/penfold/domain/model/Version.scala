package org.huwtl.penfold.domain.model

object Version {
  def init = Version(1)
}

case class Version(number: Int) {
  def next = Version(number + 1)
}
