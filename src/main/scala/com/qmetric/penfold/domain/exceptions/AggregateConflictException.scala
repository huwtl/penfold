package com.qmetric.penfold.domain.exceptions

case class AggregateConflictException(message: String) extends Exception(message)