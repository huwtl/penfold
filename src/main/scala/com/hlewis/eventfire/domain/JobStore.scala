package com.hlewis.eventfire.domain

trait JobStore {
  def add(key: String, value: String): Boolean
}
