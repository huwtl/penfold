package com.qmetric.penfold.readstore

trait NextExpectedEventIdProvider {
  def nextExpectedEvent : EventSequenceId
}
