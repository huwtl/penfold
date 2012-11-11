package com.hlewis.rabbit_scheduler.jobstore

trait Jobstore {
  def add(key: String, value: String): Boolean
}
