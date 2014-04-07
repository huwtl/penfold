package org.huwtl.penfold.readstore

trait NextExpectedEventIdProvider {
  def nextExpectedEvent : EventSequenceId
}
