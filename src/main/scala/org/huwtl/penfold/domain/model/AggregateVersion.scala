package org.huwtl.penfold.domain.model

object AggregateVersion {
  def init = AggregateVersion(1)
}

case class AggregateVersion(number: Int) {
  def next = AggregateVersion(number + 1)
}
