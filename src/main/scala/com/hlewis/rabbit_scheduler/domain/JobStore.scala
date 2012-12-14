package com.hlewis.rabbit_scheduler.domain

trait JobStore {
  def add(key: String, value: String): Boolean
}
