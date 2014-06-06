package com.qmetric.penfold.support

import scala.util.{Success, Try}

object Retry {
  @annotation.tailrec
  def retry[T](n: Int)(fn: => T): util.Try[T] = {
    Try { fn } match {
      case x: Success[T] => x
      case _ if n > 1 => retry(n - 1)(fn)
      case f => f
    }
  }
}
