package org.huwtl.penfold.domain.exceptions

case class AggregateConflictException(message: String) extends Exception(message)