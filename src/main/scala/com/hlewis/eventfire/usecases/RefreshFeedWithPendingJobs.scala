package com.hlewis.eventfire.usecases

import akka.actor.ActorRef
import com.hlewis.eventfire.domain.Refresh

class RefreshFeedWithPendingJobs(pendingJobsFeed: ActorRef)  {
  def refresh() {
    pendingJobsFeed ! Refresh
  }
}
