package com.qmetric.penfold.app.schedule

import java.util.concurrent.Executors._
import grizzled.slf4j.Logger
import scala.concurrent.duration.FiniteDuration

trait Scheduler {
  private lazy val logger = Logger(getClass)

  private val executorService = newSingleThreadScheduledExecutor

  def frequency: FiniteDuration

  def name: String

  def process()

  def start() = {
    executorService.scheduleAtFixedRate(new Runnable() {
      def run() {
        try {
          logger.debug(s"scheduled $name started")

          process()

          logger.debug(s"scheduled $name completed")
        } catch {
          case e: Throwable => logger.error(s"error during scheduled $name", e)
        }
      }
    }, 0, frequency.length, frequency.unit)
  }

  def shutdown() = {
    executorService.shutdown()
  }
}
