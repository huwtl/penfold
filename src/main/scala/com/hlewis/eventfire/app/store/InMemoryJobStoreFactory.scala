package com.hlewis.eventfire.app.store

trait InMemoryJobStoreFactory extends JobStoreFactory {
  override def initJobStore() = {
    new InMemoryJobStore()
  }
}
