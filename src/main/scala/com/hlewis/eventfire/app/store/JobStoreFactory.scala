package com.hlewis.eventfire.app.store

import com.hlewis.eventfire.domain.JobStore

trait JobStoreFactory {
  def initJobStore(): JobStore
}
