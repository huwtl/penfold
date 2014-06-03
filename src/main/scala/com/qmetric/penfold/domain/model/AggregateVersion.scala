package com.qmetric.penfold.domain.model

object AggregateVersion {
  def init = AggregateVersion(1)
}

case class AggregateVersion(number: Int) {
  def previous = AggregateVersion(if (number <= AggregateVersion.init.number) number else number - 1)
  def next = AggregateVersion(number + 1)
}
