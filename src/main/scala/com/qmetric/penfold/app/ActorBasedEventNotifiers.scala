package com.qmetric.penfold.app

import akka.actor.{Props, ActorSystem, Actor}
import akka.pattern.ask

import scala.concurrent.duration.FiniteDuration
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import scala.concurrent._
import com.qmetric.penfold.readstore.EventNotifiers
import akka.routing.RoundRobinRouter

class ActorBasedEventNotifiers(eventNotifiersDelegate: EventNotifiers, noOfWorkers: Int = 1) extends EventNotifiers {
  implicit val infiniteTimeout: Timeout = Timeout(FiniteDuration(36500, TimeUnit.DAYS))

  val system = ActorSystem("EventsNotificationSystem")

  val actor = system.actorOf(Props(new NewEventsNotificationActor(eventNotifiersDelegate)).withRouter(new RoundRobinRouter(noOfWorkers)), name = "eventsNotificationActor")

  override def notifyAllOfEvents() {
    Await.result(actor ? NotifyAll, infiniteTimeout.duration)
  }
}

private class NewEventsNotificationActor(eventNotifiers: EventNotifiers) extends Actor {
  def receive = {
    case NotifyAll => {
      eventNotifiers.notifyAllOfEvents()
      sender ! true
    }
    case _ => throw new IllegalArgumentException("unexpected message sent to event notifiers actor")
  }
}

private case object NotifyAll