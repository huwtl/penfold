package com.qmetric.penfold.support

import scala.util.{Success, Try}
import scala.concurrent.duration.Duration

object Retry {
  @annotation.tailrec
  def retry[T](retries: Int)(func: => T): util.Try[T] = {
    Try { func } match {
      case x: Success[T] => x
      case _ if retries > 1 => retry(retries - 1)(func)
      case f => f
    }
  }

  @annotation.tailrec
  def retryUntilSome[T](retries: Int, interval: Duration)(func: => Option[T]): Option[T] = {
    func match {
      case None if retries > 1 => {
        sleep(interval.toMillis)
        retryUntilSome(retries - 1, interval)(func)
      }
      case None => None
      case some => some
    }
  }

  private def sleep(millis: Long)
  {
    try {
      Thread.sleep(millis)
    } catch {
      case e: InterruptedException => Thread.currentThread().interrupt()
    }
  }
}
